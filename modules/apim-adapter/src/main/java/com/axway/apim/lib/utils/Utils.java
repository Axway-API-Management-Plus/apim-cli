package com.axway.apim.lib.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.CustomPropertiesEntity;
import com.axway.apim.api.model.CustomProperty;
import com.axway.apim.api.model.CustomProperty.Option;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.CustomPropertiesFilter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
	

	private static Logger LOG = LoggerFactory.getLogger(Utils.class);

	public static enum FedKeyType {
		FilterCircuit("<key type='FilterCircuit'>"), 
		OAuthAppProfile("<key type='OAuthAppProfile'>");
		
		private String keyType;
		
		FedKeyType(String keyType) {
			this.keyType = keyType;
		}

		public String getKeyType() {
			return keyType;
		}
	}
	
	public static String getAPIDefinitionUriFromFile(String pathToAPIDefinition) throws AppException {
		String uriToAPIDefinition = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pathToAPIDefinition));
			uriToAPIDefinition = br.readLine();
			return uriToAPIDefinition;
		} catch (Exception e) {
			throw new AppException("Can't load file:" + pathToAPIDefinition, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
		} finally {
			try {
				br.close();
			} catch (Exception ignore) {}
		}
	}
	
	public static void progressPercentage(int remain, int total, String prefix) {
	    if (remain > total) {
	        throw new IllegalArgumentException();
	    }
	    int maxBareSize = 10; // 10unit for 100%
	    int remainProcent = ((100 * remain) / total) / maxBareSize;
	    char defaultChar = '-';
	    String icon = "*";
	    String bare = new String(new char[maxBareSize]).replace('\0', defaultChar) + "]";
	    StringBuilder bareDone = new StringBuilder();
	    bareDone.append(prefix + " [");
	    for (int i = 0; i < remainProcent; i++) {
	        bareDone.append(icon);
	    }
	    String bareRemain = bare.substring(remainProcent, bare.length());
	    System.out.print("\r" + bareDone + bareRemain + " " + remainProcent * 10 + "%");
	    if (remain == total) {
	        System.out.print("\n");
	    }
	}
	
	public static boolean askYesNo(String question) {
        return Utils.askYesNo(question, "[Y]", "[N]");
    }
	
    public static boolean askYesNo(String question, String positive, String negative) {
        Scanner input = new Scanner(System.in);
        // Convert everything to upper case for simplicity...
        positive = positive.toUpperCase();
        negative = negative.toUpperCase();
        String answer;
        do {
            System.out.print(question+ " ");
            answer = input.next().trim().toUpperCase();
        } while (!answer.matches(positive) && !answer.matches(negative));
        input.close();
        // Assess if we match a positive response
        return answer.matches(positive);
    }
    
    public static String getExternalPolicyName(String policy) {
    	return getExternalPolicyName(policy, null);
    }
    
	public static String getExternalPolicyName(String policy, FedKeyType keyType) {
		if(keyType==null) keyType = FedKeyType.FilterCircuit;
		if(policy.startsWith("<key")) {
			policy = policy.substring(policy.indexOf(keyType.getKeyType()));
			policy = policy.substring(policy.indexOf("value='")+7, policy.lastIndexOf("'/></key>"));
		}
		return policy;
	}
	
	/**
	 * This method is replacing variables such as ${TokenEndpoint} in the given file 
	 * with declared variables coming from either the Environment-Variables or 
	 * from system-properties.
	 * @param inputFile The API-Config file to be replaced and returned as String
	 * @return a String representation of the API-Config-File
	 * @throws IOException if the file can't be found
	 */
	public static String substitueVariables(File inputFile) throws IOException {
		String givenConfig = new String(Files.readAllBytes(inputFile.toPath()), StandardCharsets.UTF_8);
		givenConfig = StringSubstitutor.replace(givenConfig, System.getenv());
		if(CoreParameters.getInstance().getProperties()==null) return givenConfig;
		StringSubstitutor substitutor = new StringSubstitutor(CoreParameters.getInstance().getProperties());
		return substitutor.replace(givenConfig);
	}
	
	/**
	 * @param stage defines a stage which identifies a prod or qa environment.
	 * @param stageConfig can be given to define the stage config file to be used
	 * @param baseConfigFile the base configuration
	 * @return the staged config file
	 */
	public static File getStageConfig(String stage, String stageConfig, File baseConfigFile) {
		if(stage == null && stageConfig == null) return null;
		if(stage!=null && new File(stage).exists()) { // This is to support testing with dynamically created files!
			return new File(stage);
		}
		String baseConfig;
		try {
			baseConfig = baseConfigFile.getCanonicalPath();
		} catch (IOException e) {
			LOG.error("Error reading stage config file based on the config file.", e);
			return null;
		}
		// If a stageConfig is given it used with preference over the stage
		if(stageConfig!=null) {
			// Supporting a StageConfigFile with an absolute path
			if(new File(stageConfig).exists()) return new File(stageConfig);
			// Perhaps the stageConfigFile is relative to the main base config
			File stageFile = new File(baseConfigFile.getParent() + File.separator + stageConfig);
			if(stageFile.exists()) return stageFile;
			LOG.warn("No stage configuration file found with name: '"+stageConfig+"'. It must be either absolute or relative to the main config file.");
		} else {
			if(!stage.equals("NOT_SET")) {
				File stageFile = new File(baseConfig.substring(0, baseConfig.lastIndexOf(".")+1) + stage + baseConfig.substring(baseConfig.lastIndexOf(".")));
				File subDirStageFile = new File(stageFile.getParentFile()+"/"+stage+"/"+stageFile.getName());
				if(stageFile.exists()) {
					return stageFile;
				} else if(subDirStageFile.exists()) {
					return subDirStageFile;
				} else {
					return null;
				}

			}
		}
		LOG.debug("No stage provided");
		return null;
	}
	
	public static File locateConfigFile(String configFileName) throws AppException {
		try {
			configFileName = URLDecoder.decode(configFileName, "UTF-8");
			File configFile = new File(configFileName);
			if(configFile.exists()) return configFile;
			// This is mainly to load the samples sitting inside the package!
			String installFolder = getInstallFolder();
			configFile = new File(installFolder + File.separator + configFileName);
			if(configFile.exists()) return configFile;
			throw new AppException("Unable to find given Config-File: '"+configFileName+"'", ErrorCode.CANT_READ_CONFIG_FILE);
		} catch (Exception e) {
			throw new AppException("Unable to find given Config-File: '"+configFileName+"'", ErrorCode.CANT_READ_CONFIG_FILE, e);
		}
	}
	
	public static String getInstallFolder() throws URISyntaxException {
		return new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getParent();
	}
	
	public static void validateCustomProperties(Map<String, String> customProperties, Type type) throws AppException {
		Map<String, CustomProperty> configuredCustomProperties = APIManagerAdapter.getInstance().customPropertiesAdapter.getCustomProperties(type);
		Map<String, CustomProperty> requiredConfiguredCustomProperties = APIManagerAdapter.getInstance().customPropertiesAdapter.getRequiredCustomProperties(type);
		if(customProperties!=null) {
			Iterator<String> desiredCustomProps = customProperties.keySet().iterator();
			while(desiredCustomProps.hasNext()) {
				String desiredCustomProperty = desiredCustomProps.next();
				String desiredCustomPropertyValue = customProperties.get(desiredCustomProperty);
				CustomProperty configuredCustomProperty = configuredCustomProperties.get(desiredCustomProperty);
				if(configuredCustomProperty==null) {
					throw new AppException("The custom-property: '" + desiredCustomProperty + "' is not configured in API-Manager.", ErrorCode.CANT_READ_CONFIG_FILE);
				}
				if(configuredCustomProperty.getType()!=null && ( configuredCustomProperty.getType().equals("select") || configuredCustomProperty.getType().equals("switch") )) {
					boolean valueFound = false;
					List<Option> knownOptions = configuredCustomProperty.getOptions();
					if(knownOptions==null) {
						LOG.warn("Skipping custom property validation, as the custom-property: '" + desiredCustomProperty + "' with type: " + configuredCustomProperty.getType() + " has no options configured. Please check your custom properties configuration.");
						break;
					}
					for(Option knownOption : knownOptions) {
						if(knownOption.getValue().equals(desiredCustomPropertyValue)) {
							valueFound = true;
							break;
						}
					}
					if(!valueFound) {
						throw new AppException("The value: '" + desiredCustomPropertyValue + "' is not a valid option for custom property: '" + desiredCustomProperty + "'", ErrorCode.CANT_READ_CONFIG_FILE);
					}
				}
				// Remove 
				requiredConfiguredCustomProperties.remove(desiredCustomProperty);
			}
		}
		// Finally check, if missing custom properties are left
		if(requiredConfiguredCustomProperties!=null && requiredConfiguredCustomProperties.size()==0) return;
		for(String propName : requiredConfiguredCustomProperties.keySet()) {
			throw new AppException("Missing required custom property: '"+propName+"'", ErrorCode.CANT_READ_CONFIG_FILE);
		}
	}
	
	public static void addCustomPropertiesForEntity(List<? extends CustomPropertiesEntity> entities, String json, CustomPropertiesFilter filter) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// Custom-Properties will be added depending on the given Properties in the filter
		if(filter.getCustomProperties() == null || entities.size() == 0) {
			return;
		}
		Map<String, JsonNode> enitityAsJsonMappedWithId = new HashMap<String, JsonNode>();
		JsonNode jsonPayload = mapper.readTree(json);
		// Create a map based on the API-ID containing the original JSON-Payload received from API-Manager
		for(JsonNode node : jsonPayload) {
			String apiId = node.get("id").asText();
			enitityAsJsonMappedWithId.put(apiId, node);
		}
		Map<String, String> customProperties = new LinkedHashMap<String, String>();
		// Iterate over all APIs (at this point not yet having the custom-properties serialized)
		for(CustomPropertiesEntity entity : entities) {
			// Get the original JSON-Payload for the current API fetched from API-Manager
			JsonNode node = enitityAsJsonMappedWithId.get(entity.getId());
			// Iterate over all requested Custom-Properties that should be returned 
			for(String customPropKey : filter.getCustomProperties()) {
				// Try to get the value for that custom property from the JSON-Payload
				JsonNode value = node.get(customPropKey);
				// If there is nothing found - skip it.
				if(value == null) continue;
				// Add it to the map of custom properties that will be attached to the API
				customProperties.put(customPropKey, value.asText());
			}
			entity.setCustomProperties((customProperties.size()==0) ? null : customProperties);
		}
	}
	
	public static Long getParsedDate(String date) throws AppException {
		List<String> dateFormats = Arrays.asList("dd.MM.yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy");
		SimpleDateFormat format;
		Date retDate = null;
		for (String dateFormat : dateFormats) {
			format = new SimpleDateFormat(dateFormat, Locale.US);
			format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
			try {
				retDate = format.parse(date);
			} catch (ParseException e) { }
			if(retDate!=null && retDate.after(new Date())) {
				LOG.info("Parsed retirementDate: '"+date+"' using format: '"+dateFormat+"' to: '"+retDate+"'");
				break;
			}
		}
		if(retDate==null || retDate.before(new Date())) {
			throw new AppException("Unable to parse the given retirementDate using the following formats: " + dateFormats + ". Please note the retirementDate must be in the future.", ErrorCode.CANT_READ_CONFIG_FILE);
		}
		return retDate.getTime();
	}
	
	public static String getAPILogString(API api) {
		if(api==null) return "N/A";
		return api.getName() + " " + api.getVersion() + " ("+api.getVersion()+")";
	}
	
	public static CloseableHttpClient createHttpClient(String uri, String username, String password) throws AppException {
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		try {
			addBasicAuthCredential(uri, username, password, httpClientBuilder);
			addSSLContext(uri, httpClientBuilder);
			return httpClientBuilder.build();
		} catch (Exception e) {
			throw new AppException("Error during create http client for retrieving ...", ErrorCode.CANT_CREATE_HTTP_CLIENT);
		}
	}

	private static void addSSLContext(String uri, HttpClientBuilder httpClientBuilder) throws KeyManagementException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException {
		if (isHttpsUri(uri)) {
			SSLConnectionSocketFactory sslCtx = createSSLContext();
			if (sslCtx!=null) {
				httpClientBuilder.setSSLSocketFactory(sslCtx);
			}
		}
	}

	private static void addBasicAuthCredential(String uri, String username, String password,
			HttpClientBuilder httpClientBuilder) {
		//if(this.apiConfig instanceof DesiredTestOnlyAPI) return; // Don't do that when unit-testing
		if(username!=null) {
			LOG.info("Loading API-Definition from: " + uri + " ("+username+")");
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
		            new AuthScope(AuthScope.ANY),
		            new UsernamePasswordCredentials(username, password));
			httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
		} else {
			LOG.info("Loading API-Definition from: " + uri);
		}
	}
	
	public static boolean isHttpUri(String pathToAPIDefinition) {
		String httpUri = pathToAPIDefinition.substring(pathToAPIDefinition.indexOf("@")+1);
		return( httpUri.startsWith("http://") || httpUri.startsWith("https://"));
	}
	
	public static boolean isHttpsUri(String uri) {
		return( uri.startsWith("https://") );
	}
	
	private static SSLConnectionSocketFactory createSSLContext() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException {
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustAllStrategy());
		
		String keyStorePath=System.getProperty("javax.net.ssl.keyStore","");
		if (StringUtils.isNotEmpty(keyStorePath)) {
			String keyStorePassword=System.getProperty("javax.net.ssl.keyStorePassword","");
			if (StringUtils.isNotEmpty(keyStorePassword)) {
				String keystoreType=System.getProperty("javax.net.ssl.keyStoreType",KeyStore.getDefaultType());
				LOG.debug("Reading keystore from {}",keyStorePath);
				KeyStore ks = KeyStore.getInstance(keystoreType);
				ks.load(new FileInputStream(new File(keyStorePath)), keyStorePassword.toCharArray());				
				builder.loadKeyMaterial(ks,keyStorePassword.toCharArray());
			}
		} else {
			LOG.debug("NO javax.net.ssl.keyStore property.");
		}
		String [] tlsProts = getAcceptedTLSProtocols();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				builder.build(),
                tlsProts,
                null,
                new NoopHostnameVerifier());
		return sslsf;
	}

	private static String[] getAcceptedTLSProtocols() {
		String protocols = System.getProperty("https.protocols","TLSv1.2"); //default TLSv1.2
		LOG.debug("https protocols: {}",protocols);
		return protocols.split(",");
	}
}
