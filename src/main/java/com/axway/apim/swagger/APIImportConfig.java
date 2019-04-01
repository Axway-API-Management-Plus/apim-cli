package com.axway.apim.swagger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.APIImportDefinition;
import com.axway.apim.swagger.api.AbstractAPIDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.properties.cacerts.CaCert;
import com.axway.apim.swagger.api.properties.corsprofiles.CorsProfile;
import com.axway.apim.swagger.api.properties.organization.Organization;
import com.axway.apim.swagger.api.properties.quota.APIQuota;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityDevice;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;

/**
 * The APIContract reflects the given API-Configuration plus the Swagger-Definition.
 * This class will read the API-Configuration plus the optional set stage and the Swagger-File.
 * 
 * @author cwiechmann
 */
public class APIImportConfig {
	
	private static Logger LOG = LoggerFactory.getLogger(APIImportConfig.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private String pathToSwagger;
	
	private String apiConfig;
	
	private String stage;
	
	private String wsdlURL;

	/**
	 * Constructs the APIImportConfig 
	 * @param apiContract
	 * @param stage
	 * @param pathToSwagger
	 * @throws AppException
	 */
	public APIImportConfig(String apiContract, String stage, String pathToSwagger,String wsdlURL) throws AppException {
		super();
		this.apiConfig = apiContract;
		this.stage = stage;
		this.pathToSwagger = pathToSwagger;
		this.wsdlURL=wsdlURL;
	}
	
	/**
	 * Returns the IAPIDefintion that returns the desired state of the API. In this method:<br>
	 * <li>the API-Contract is read</li>
	 * <li>the API-Contract is merged with the override</li>
	 * <li>the Swagger-File is read</li>
	 * <li>Additionally some validations & completions are made here</li>
	 * <li>in the future: This is the place to do some default handling.
	 * 
	 * @return IAPIDefintion with the desired state of the API. This state will be 
	 * the input to create the APIChangeState.
	 * 
	 * @throws AppException if the state can't be created.
	 * @see {@link IAPIDefinition}, {@link AbstractAPIDefinition}
	 */
	public IAPIDefinition getImportAPIDefinition() throws AppException {
		IAPIDefinition stagedConfig;
		try {
			IAPIDefinition baseConfig = mapper.readValue(new File(apiConfig), APIImportDefinition.class);
			ObjectReader updater = mapper.readerForUpdating(baseConfig);
			if(getStageContract(stage, apiConfig)!=null) {
				LOG.info("Overriding configuration from: " + getStageContract(stage, apiConfig));
				stagedConfig = updater.readValue(new File(getStageContract(stage, apiConfig)));
			} else {
				stagedConfig = baseConfig;
			}
			addDefaultPassthroughSecurityProfile(stagedConfig);
			if (this.wsdlURL!=null) {
				stagedConfig.setWsdlURL(this.wsdlURL);
				//we use the pathToSwagger even if it's a wsdl url
				pathToSwagger=this.wsdlURL;
				stagedConfig.setSwaggerDefinition(new APISwaggerDefinion(getSwaggerContent()));
			} else {
				stagedConfig.setSwaggerDefinition(new APISwaggerDefinion(getSwaggerContent()));
			}
			addImageContent(stagedConfig);
			validateCustomProperties(stagedConfig);
			validateDescription(stagedConfig);
			validateCorsConfig(stagedConfig);
			validateOutboundAuthN(stagedConfig);
			completeCaCerts(stagedConfig);
			addQuotaConfiguration(stagedConfig);
			handleAllOrganizations(stagedConfig);
			completeClientApplications(stagedConfig);
			return stagedConfig;
		} catch (Exception e) {
			if(e.getCause() instanceof AppException) {
				throw (AppException)e.getCause();
			}
			throw new AppException("Cant parse JSON-Config file(s)", ErrorCode.CANT_READ_CONFIG_FILE, e);
		}
	}
	
	private void handleAllOrganizations(IAPIDefinition apiConfig) throws AppException {
		if(apiConfig.getClientOrganizations()==null) return;
		List<String> allDesiredOrgs = new ArrayList<String>();
		if(apiConfig.getClientOrganizations().contains("ALL")) {
			List<Organization> allOrgs = APIManagerAdapter.getAllOrgs();
			for(Organization org : allOrgs) {
				allDesiredOrgs.add(org.getName());
			}
			apiConfig.getClientOrganizations().clear();
			apiConfig.getClientOrganizations().addAll(allDesiredOrgs);
			((APIImportDefinition)apiConfig).setRequestForAllOrgs(true);
		} else {
			// As the API-Manager internally handles the owning organization in the same way, 
			// we have to add the Owning-Org as a desired org
			if(!apiConfig.getClientOrganizations().contains(apiConfig.getOrganization())) {
				apiConfig.getClientOrganizations().add(apiConfig.getOrganization());
			}
		}
	}
	
	private void addQuotaConfiguration(IAPIDefinition apiConfig) {
		APIImportDefinition importAPI = (APIImportDefinition)apiConfig;
		initQuota(importAPI.getSystemQuota());
		initQuota(importAPI.getApplicationQuota());
	}
	
	private void initQuota(APIQuota quotaConfig) {
		if(quotaConfig==null) return;
		if(quotaConfig.getType().equals("APPLICATION")) {
			quotaConfig.setName("Application Default");
			quotaConfig.setDescription("Maximum message rates per application. Applied to each application unless an Application-Specific quota is configured");
		} else {
			quotaConfig.setName("System Default");
			quotaConfig.setDescription(".....");			
		}
	}
	
	private void validateDescription(IAPIDefinition apiConfig) throws AppException {
		if(apiConfig.getDescriptionType()==null || apiConfig.getDescriptionType().equals("original")) return;
		String descriptionType = apiConfig.getDescriptionType();
		if(descriptionType.equals("manual")) {
			if(apiConfig.getDescriptionManual()==null) {
				throw new AppException("descriptionManual can't be null with descriptionType set to 'manual'", ErrorCode.CANT_READ_CONFIG_FILE);
			}
		} else if(descriptionType.equals("url")) {
			if(apiConfig.getDescriptionUrl()==null) {
				throw new AppException("descriptionUrl can't be null with descriptionType set to 'url'", ErrorCode.CANT_READ_CONFIG_FILE);
			}
		} else if(descriptionType.equals("markdown")) {
			if(apiConfig.getDescriptionMarkdown()==null) {
				throw new AppException("descriptionMarkdown can't be null with descriptionType set to 'markdown'", ErrorCode.CANT_READ_CONFIG_FILE);
			}
			if(!apiConfig.getDescriptionMarkdown().startsWith("${env.")) {
				throw new AppException("descriptionMarkdown must start with an environment variable", ErrorCode.CANT_READ_CONFIG_FILE);
			}
		} else if(descriptionType.equals("original")) {
			return;
		} else {
			throw new AppException("Unknown descriptionType: '"+descriptionType.equals("manual")+"'", ErrorCode.CANT_READ_CONFIG_FILE);
		}
	}
	
	private void validateCorsConfig(IAPIDefinition apiConfig) throws AppException {
		if(apiConfig.getCorsProfiles()==null || apiConfig.getCorsProfiles().size()==0) return;
		// Check if there is a default cors profile declared otherwise create one internally
		boolean defaultCorsFound = false;
		for(CorsProfile profile : apiConfig.getCorsProfiles()) {
			if(profile.getName().equals("_default")) {
				defaultCorsFound = true;
				break;
			}
		}
		if(!defaultCorsFound) {
			CorsProfile defaultCors = new CorsProfile();
			defaultCors.setName("_default");
			defaultCors.setIsDefault("true");
			defaultCors.setOrigins(new String[] {"*"});
			defaultCors.setAllowedHeaders(new String[] {});
			defaultCors.setExposedHeaders(new String[] {"X-CorrelationID"});
			defaultCors.setMaxAgeSeconds("0");
			defaultCors.setSupportCredentials("false");
			apiConfig.getCorsProfiles().add(defaultCors);
		}
	}
	
	/**
	 * Purpose of this method is to load the actual existing applications from API-Manager 
	 * based on the provided criteria (App-Name, API-Key, OAuth-ClientId or Ext-ClientId). 
	 * Or, if the APP doesn't exists remove it from the list and log a warning message.
	 * Additionally, for each application it's check, that the organization has access 
	 * to this API, otherwise it will be removed from the list as well and a warning message is logged.
	 * @param apiConfig
	 * @throws AppException
	 */
	private void completeClientApplications(IAPIDefinition apiConfig) throws AppException {
		if(CommandParameters.getInstance().isIgnoreClientApps()) return;
		ClientApplication loadedApp = null;
		ClientApplication app;
		if(apiConfig.getApplications()!=null) {
			LOG.info("Handling configured client-applications.");
			ListIterator<ClientApplication> it = apiConfig.getApplications().listIterator();
			while(it.hasNext()) {
				app = it.next();
				if(app.getName()!=null) {
					loadedApp = APIManagerAdapter.getApplication(app.getName());
					if(loadedApp==null) {
						LOG.warn("Unknown application with name: '" + app.getName() + "' configured. Ignoring this application.");
						it.remove();
						continue;
					}
					LOG.info("Found existing application: '"+app.getName()+"' based on given name '"+app.getName()+"'");
				} else if(app.getApiKey()!=null) {
					loadedApp = getAppForCredential(app.getApiKey(), APIManagerAdapter.CREDENTIAL_TYPE_API_KEY);
					if(loadedApp==null) {
						it.remove();
						continue;
					} 
				} else if(app.getOauthClientId()!=null) {
					loadedApp = getAppForCredential(app.getOauthClientId(), APIManagerAdapter.CREDENTIAL_TYPE_OAUTH);
					if(loadedApp==null) {
						it.remove();
						continue;
					} 
				} else if(app.getExtClientId()!=null) {
					loadedApp = getAppForCredential(app.getExtClientId(), APIManagerAdapter.CREDENTIAL_TYPE_EXT_CLIENTID);
					if(loadedApp==null) {
						it.remove();
						continue;
					} 
				}
				it.set(loadedApp); // Replace the incoming app, with the App loaded from API-Manager
			}
		}
	}
	
	private static ClientApplication getAppForCredential(String credential, String type) throws AppException {
		LOG.debug("Searching application with configured credential (Type: "+type+"): '"+credential+"'");
		ClientApplication app = APIManagerAdapter.getAppIdForCredential(credential, type);
		if(app==null) {
			LOG.warn("Unknown application with ("+type+"): '" + credential + "' configured. Ignoring this application.");
			return null;
		}
		return app;
	}
	
	private void completeCaCerts(IAPIDefinition apiConfig) throws AppException {
		if(apiConfig.getCaCerts()!=null) {
			List<CaCert> completedCaCerts = new ArrayList<CaCert>();
			for(CaCert cert :apiConfig.getCaCerts()) {
				if(cert.getCertBlob()==null) {
					JsonNode certInfo = APIManagerAdapter.getCertInfo(getInputStreamForCertFile(cert), cert);
					try {
						CaCert completedCert = mapper.readValue(certInfo.get(0).toString(), CaCert.class);
						completedCaCerts.add(completedCert);
					} catch (Exception e) {
						throw new AppException("Can't initialize given certificate.", ErrorCode.CANT_READ_CONFIG_FILE, e);
					}
				}
			}
			apiConfig.getCaCerts().clear();
			apiConfig.getCaCerts().addAll(completedCaCerts);
		}
	}
	
	private InputStream getInputStreamForCertFile(CaCert cert) throws AppException {
		InputStream is;
		File file;
		// Certificates might be stored somewhere else, so try to load them directly
		file = new File(cert.getCertFile());
		if(file.exists()) { 
			try {
				is = new FileInputStream(file);
				return is;
			} catch (FileNotFoundException e) {
				throw new AppException("Cant read given certificate file", ErrorCode.CANT_READ_CONFIG_FILE);
			}
		}
		String baseDir;
		try {
			baseDir = new File(this.apiConfig).getCanonicalFile().getParent();
		} catch (IOException e1) {
			throw new AppException("Can't read certificate file.", ErrorCode.CANT_READ_CONFIG_FILE, e1, true);
		}
		file = new File(baseDir + File.separator + cert.getCertFile());
		if(file.exists()) { 
			try {
				is = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new AppException("Cant read given certificate file", ErrorCode.CANT_READ_CONFIG_FILE);
			}
		} else {
			LOG.debug("Can't read certifiate from file-location: " + file.toString() + ". Now trying to read it from the classpath.");
			// Try to read it from classpath
			is = APIManagerAdapter.class.getResourceAsStream(cert.getCertFile()); 
		}
		if(is==null) {
			LOG.error("Can't read certificate: "+cert.getCertFile()+" from file or classpath.");
			LOG.error("Certificates in filesystem are either expected relative to the API-Config-File or as an absolute path.");
			LOG.error("In the same directory. 		Example: \"myCertFile.crt\"");
			LOG.error("Relative to it.         		Example: \"../../allMyCertsAreHere/myCertFile.crt\"");
			LOG.error("With an absolute path   		Example: \"/another/location/with/allMyCerts/myCertFile.crt\"");
			throw new AppException("Can't read certificate: "+cert.getCertFile()+" from file or classpath.", ErrorCode.CANT_READ_CONFIG_FILE);
		}
		return is;
	}
	
	private void validateCustomProperties(IAPIDefinition apiConfig) throws AppException {
		if(apiConfig.getCustomProperties()!=null) {
			JsonNode configuredProps = APIManagerAdapter.getCustomPropertiesConfig();
			Iterator<String> props = apiConfig.getCustomProperties().keySet().iterator();
			while(props.hasNext()) {
				String propertyKey = props.next();
				String propertyValue = apiConfig.getCustomProperties().get(propertyKey);
				JsonNode configuredProp = configuredProps.at("/api/"+propertyKey);
				if(configuredProp instanceof MissingNode) {
					throw new AppException("The custom-property: '" + propertyKey + "' is not configured in API-Manager.", ErrorCode.CANT_READ_CONFIG_FILE);
				}
				JsonNode propType = configuredProp.get("type");
				if(propType!=null && ( propType.asText().equals("select") || propType.asText().equals("switch") )) {
					boolean valueFound = false;
					ArrayNode selectOptions = (ArrayNode)configuredProp.get("options");
					for(JsonNode option : selectOptions) {
						if(option.at("/value").asText().equals(propertyValue)) {
							valueFound = true;
							break;
						}
					}
					if(!valueFound) {
						throw new AppException("The value: '" + propertyValue + "' isn't configured for custom property: '" + propertyKey + "'", ErrorCode.CANT_READ_CONFIG_FILE);
					}
				}
			}
		}
	}
	
	private byte[] getSwaggerContent() throws AppException {
		try {
			return IOUtils.toByteArray(getSwaggerAsStream());
		} catch (IOException e) {
			throw new AppException("Can't read swagger-file from file", ErrorCode.CANT_READ_SWAGGER_FILE, e);
		}
	}
	
	/**
	 * To make testing easier we allow reading test-files from classpath as well
	 * @throws AppException when the import Swagger-File can't be read.
	 * @return The import Swagger-File as an InputStream
	 */
	public InputStream getSwaggerAsStream() throws AppException {
		InputStream is = null;
		if(pathToSwagger.endsWith(".url")) {
			return getSwaggerFromURL(getSwaggerUriFromFile(pathToSwagger));
		} else if(isHttpUri(pathToSwagger)) {
			return getSwaggerFromURL(pathToSwagger);
		} else {
			File inputFile = new File(pathToSwagger);
			try {
				if(inputFile.exists()) { 
					is = new FileInputStream(pathToSwagger);
				} else {
					is = this.getClass().getResourceAsStream(pathToSwagger);
				}
				if(is == null) {
					throw new AppException("Unable to read swagger file from: " + pathToSwagger, ErrorCode.CANT_READ_SWAGGER_FILE);
				}
				
			} catch (Exception e) {
				throw new AppException("Unable to read swagger file from: " + pathToSwagger, ErrorCode.CANT_READ_SWAGGER_FILE, e);
			}
			
		}
		return is;
	}
	
	private InputStream getSwaggerFromURL(String urlToSwagger) throws AppException {
		String uri = null;
		String username = null;
		String password = null;
		String[] temp = urlToSwagger.split("@");
		if(temp.length==1) {
			uri = temp[0];
		} else if(temp.length==2) {
			username = temp[0].substring(0, temp[0].indexOf("/"));
			password = temp[0].substring(temp[0].indexOf("/")+1);
			uri = temp[1];
		} else {
			throw new AppException("Swagger-URL has an invalid format. ", ErrorCode.CANT_READ_SWAGGER_FILE);
		}
		CloseableHttpClient httpclient = null;
		try {
			LOG.info("Loading Swagger-File from: " + uri);
			if(username!=null) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(
		                new AuthScope(AuthScope.ANY),
		                new UsernamePasswordCredentials(username, password));
				httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
			} else {
				httpclient = HttpClients.createDefault();
			}
			HttpGet httpGet = new HttpGet(uri);
			
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpGet, responseHandler);
            return new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new AppException("Cannot load Swagger-File from URI: "+uri, ErrorCode.CANT_READ_SWAGGER_FILE, e);
		} finally {
			try {
				httpclient.close();
			} catch (Exception ignore) {}
		}
	}
	
	public static boolean isHttpUri(String pathToSwagger) {
		String httpUri = pathToSwagger.substring(pathToSwagger.indexOf("@")+1);
		return( httpUri.startsWith("http://") || httpUri.startsWith("https://"));
	}
	
	private static String getSwaggerUriFromFile(String pathToSwagger) throws AppException {
		String uriToSwagger = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pathToSwagger));
			uriToSwagger = br.readLine();
			return uriToSwagger;
		} catch (Exception e) {
			throw new AppException("Can't load file:" + pathToSwagger, ErrorCode.CANT_READ_SWAGGER_FILE, e);
		} finally {
			try {
				br.close();
			} catch (Exception ignore) {}
		}
	}
	
	private String getStageContract(String stage, String apiContract) {
		if(stage == null) return null;
		File stageFile = new File(stage);
		if(stageFile.exists()) { // This is to support testing with dynamic created files!
			return stageFile.getAbsolutePath();
		}
		if(stage!=null && !stage.equals("NOT_SET")) {
			return apiContract.substring(0, apiContract.lastIndexOf(".")+1) + stage + apiContract.substring(apiContract.lastIndexOf("."));
		}
		LOG.debug("No stage provided");
		return null;
	}
	
	private IAPIDefinition addDefaultPassthroughSecurityProfile(IAPIDefinition importApi) throws AppException {
		if(importApi.getSecurityProfiles()==null || importApi.getSecurityProfiles().size()==0) {
			SecurityProfile passthroughProfile = new SecurityProfile();
			passthroughProfile.setName("_default");
			passthroughProfile.setIsDefault("true");
			SecurityDevice passthroughDevice = new SecurityDevice();
			passthroughDevice.setName("Pass Through");
			passthroughDevice.setType("passThrough");
			passthroughDevice.setOrder("0");
			passthroughDevice.getProperties().put("subjectIdFieldName", "Pass Through");
			passthroughDevice.getProperties().put("removeCredentialsOnSuccess", "true");
			passthroughProfile.getDevices().add(passthroughDevice);
			
			importApi.setSecurityProfiles(new ArrayList<SecurityProfile>());
			importApi.getSecurityProfiles().add(passthroughProfile);
		}
		return importApi;
	}
	
	private void validateOutboundAuthN(IAPIDefinition importApi) throws AppException {
		// Request to use some specific Outbound-AuthN for this API
		if(importApi.getAuthenticationProfiles()!=null && importApi.getAuthenticationProfiles().size()!=0) {
			// For now, we only support one DEFAULT, hence it must be configured as such
			if(importApi.getAuthenticationProfiles().size()>1) {
				throw new AppException("Only one AuthenticationProfile supported.", ErrorCode.CANT_READ_CONFIG_FILE);
			}
			importApi.getAuthenticationProfiles().get(0).setIsDefault(true);
			importApi.getAuthenticationProfiles().get(0).setName("_default"); // Otherwise it wont be considered as default by the API-Mgr.
		}
		
	}
	
	private IAPIDefinition addImageContent(IAPIDefinition importApi) throws AppException {
		if(importApi.getImage()!=null) { // An image is declared
			try {
				String baseDir = new File(this.apiConfig).getParent();
				File file = new File(baseDir + "/" + importApi.getImage().getFilename());
				importApi.getImage().setBaseFilename(file.getName());
				if(file.exists()) { 
					importApi.getImage().setImageContent(IOUtils.toByteArray(new FileInputStream(file)));
				} else {
					// Try to read it from classpath
					importApi.getImage().setImageContent(IOUtils.toByteArray(
							this.getClass().getResourceAsStream(importApi.getImage().getFilename())));
				}
			} catch (Exception e) {
				throw new AppException("Can't read image-file: "+importApi.getImage().getFilename()+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR, e);
			}
		}
		return importApi;
	}
}
