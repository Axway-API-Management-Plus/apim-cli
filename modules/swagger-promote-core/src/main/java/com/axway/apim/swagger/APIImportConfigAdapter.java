package com.axway.apim.swagger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import com.axway.apim.lib.ErrorState;
import com.axway.apim.lib.URLParser;
import com.axway.apim.lib.Utils;
import com.axway.apim.swagger.api.properties.APIDefintion;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.properties.authenticationProfiles.AuthType;
import com.axway.apim.swagger.api.properties.authenticationProfiles.AuthenticationProfile;
import com.axway.apim.swagger.api.properties.cacerts.CaCert;
import com.axway.apim.swagger.api.properties.corsprofiles.CorsProfile;
import com.axway.apim.swagger.api.properties.organization.Organization;
import com.axway.apim.swagger.api.properties.quota.APIQuota;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityDevice;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.axway.apim.swagger.api.state.DesiredAPI;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;

/**
 * The APIConfig reflects the given API-Configuration plus the API-Definition, which is either a 
 * Swagger-File or a WSDL.
 * This class will read the API-Configuration plus the optional set stage and the API-Definition.
 * 
 * @author cwiechmann
 */
public class APIImportConfigAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIImportConfigAdapter.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	

	/** This is the given path to WSDL or Swagger using -a parameter */
	private String pathToAPIDefinition;
	
	/** The API-Config-File given by the user with -c parameter */
	private String apiConfigFile;
	
	/** The APIConfig instance created by the APIConfigImporter */
	private IAPI apiConfig;
	
	/** If true, an OrgAdminUser is used to start the tool */
	private boolean usingOrgAdmin;
	
	private ErrorState error = ErrorState.getInstance();


	/**
	 * Constructs the APIImportConfig 
	 * @param apiConfigFile the API-Config given by the user
	 * @param stage an optional stage used to load overrides and stage specific environment properties
	 * @param pathToAPIDefinition an optional path to the API-Definition (Swagger / WSDL), can be in the config-file as well.
	 * @param usingOrgAdmin access to API-Manager should be limited to the Org-Admin account
	 * @throws AppException if the config-file can't be parsed for some reason
	 */
	public APIImportConfigAdapter(String apiConfigFile, String stage, String pathToAPIDefinition, boolean usingOrgAdmin) throws AppException {
		super();
		this.apiConfigFile = apiConfigFile;
		this.pathToAPIDefinition = pathToAPIDefinition;
		this.usingOrgAdmin = usingOrgAdmin;
		IAPI baseConfig;
		try {
			baseConfig = mapper.readValue(new File(apiConfigFile), DesiredAPI.class);
			ObjectReader updater = mapper.readerForUpdating(baseConfig);
			if(getStageConfig(stage, apiConfigFile)!=null) {
				try {
					apiConfig = updater.readValue(new File(getStageConfig(stage, apiConfigFile)));
					LOG.info("Loaded stage API-Config from file: " + getStageConfig(stage, apiConfigFile));
				} catch (FileNotFoundException e) {
					LOG.debug("No config file found for stage: '"+stage+"'");
					apiConfig = baseConfig;
				}
			} else {
				apiConfig = baseConfig;
			}
		} catch (Exception e) {
			error.setError("Cant parse JSON-Config file(s)", ErrorCode.CANT_READ_CONFIG_FILE);
			throw new AppException("Cant parse JSON-Config file(s)", ErrorCode.CANT_READ_CONFIG_FILE, e);
		}
	}

	public IAPI getApiConfig() {
		return apiConfig;
	}

	/**
	 * Returns the IAPIDefintion that returns the desired state of the API. In this method:<br>
	 * - the API-Config is read
	 * - the API-Config is merged with the override
	 * - the API-Definition is read
	 * - Additionally some validations and completions are made here
	 * - in the future: This is the place to do some default handling.
	 * 
	 * @return IAPIDefintion with the desired state of the API. This state will be 
	 * the input to create the APIChangeState.
	 * 
	 * @throws AppException if the state can't be created.
	 */
	public IAPI getDesiredAPI() throws AppException {
		try {
			validateExposurePath(apiConfig);
			validateOrganization(apiConfig);
			checkForAPIDefinitionInConfiguration(apiConfig);
			addDefaultPassthroughSecurityProfile(apiConfig);
			APIDefintion apiDefinition = new APIDefintion();
			apiDefinition.setAPIDefinitionFile(this.pathToAPIDefinition);
			apiDefinition.setAPIDefinitionContent(getAPIDefinitionContent(), (DesiredAPI)apiConfig);
			apiConfig.setAPIDefinition(apiDefinition);
			addImageContent(apiConfig);
			validateCustomProperties(apiConfig);
			validateDescription(apiConfig);
			validateCorsConfig(apiConfig);
			validateOutboundAuthN(apiConfig);
			validateHasQueryStringKey(apiConfig);
			completeCaCerts(apiConfig);
			addQuotaConfiguration(apiConfig);
			handleAllOrganizations(apiConfig);
			completeClientApplications(apiConfig);
			return apiConfig;
		} catch (Exception e) {
			if(e.getCause() instanceof AppException) {
				throw (AppException)e.getCause();
			}
			throw new AppException("Cannot validate/fulfill configuration file.", ErrorCode.CANT_READ_CONFIG_FILE, e);
		}
	}
	
	private void validateExposurePath(IAPI apiConfig) throws AppException {
		if(apiConfig.getPath()==null) {
			ErrorState.getInstance().setError("Config-Parameter: 'path' is not given", ErrorCode.CANT_READ_CONFIG_FILE, false);
			throw new AppException("Path is invalid.", ErrorCode.CANT_READ_CONFIG_FILE);
		}
		if(!apiConfig.getPath().startsWith("/")) {
			ErrorState.getInstance().setError("Config-Parameter: 'path' must start with a \"/\" following by a valid API-Path (e.g. /api/v1/customer).", ErrorCode.CANT_READ_CONFIG_FILE, false);
			throw new AppException("Path is invalid.", ErrorCode.CANT_READ_CONFIG_FILE);
		}
	}
	
	private void validateOrganization(IAPI apiConfig) throws AppException {
		if(usingOrgAdmin) { // Hardcode the orgId to the organization of the used OrgAdmin
			apiConfig.setOrgId(APIManagerAdapter.getCurrentUser(false).getOrganizationId());
		} else {
			String desiredOrgId = APIManagerAdapter.getInstance().getOrgId(apiConfig.getOrganization(), true);
			if(desiredOrgId==null) {
				error.setError("The given organization: '"+apiConfig.getOrganization()+"' is either unknown or hasn't the Development flag.", ErrorCode.UNKNOWN_ORGANIZATION, false);
				throw new AppException("The given organization: '"+apiConfig.getOrganization()+"' is either unknown or hasn't the Development flag.", ErrorCode.UNKNOWN_ORGANIZATION);
			}
			apiConfig.setOrgId(desiredOrgId);
		}
	}

	private void checkForAPIDefinitionInConfiguration(IAPI apiConfig) throws AppException {
		String path = getCurrentPath();
		LOG.debug("Current path={}",path);
		if (StringUtils.isEmpty(this.pathToAPIDefinition)) {
			if (StringUtils.isNotEmpty(apiConfig.getApiDefinitionImport())) {
				this.pathToAPIDefinition=apiConfig.getApiDefinitionImport();
				LOG.debug("Reading API Definition from configuration file");
			} else {
				throw new AppException("No API Definition configured", ErrorCode.NO_API_DEFINITION_CONFIGURED);
			}
		}
	}

	private String getCurrentPath() {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		return s;
	}
	
	private void handleAllOrganizations(IAPI apiConfig) throws AppException {
		if(apiConfig.getClientOrganizations()==null) return;
		if(apiConfig.getState().equals(IAPI.STATE_UNPUBLISHED)) {
			apiConfig.setClientOrganizations(null); // Making sure, orgs are not considered as a changed property
			return;
		}
		List<String> allDesiredOrgs = new ArrayList<String>();
		if(apiConfig.getClientOrganizations().contains("ALL")) {
			List<Organization> allOrgs = APIManagerAdapter.getInstance().getAllOrgs();
			for(Organization org : allOrgs) {
				allDesiredOrgs.add(org.getName());
			}
			apiConfig.getClientOrganizations().clear();
			apiConfig.getClientOrganizations().addAll(allDesiredOrgs);
			((DesiredAPI)apiConfig).setRequestForAllOrgs(true);
		} else {
			// As the API-Manager internally handles the owning organization in the same way, 
			// we have to add the Owning-Org as a desired org
			if(!apiConfig.getClientOrganizations().contains(apiConfig.getOrganization())) {
				apiConfig.getClientOrganizations().add(apiConfig.getOrganization());
			}
		}
	}
	
	private void addQuotaConfiguration(IAPI apiConfig) throws AppException {
		if(apiConfig.getState()==IAPI.STATE_UNPUBLISHED) return;
		DesiredAPI importAPI = (DesiredAPI)apiConfig;
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
	
	private void validateDescription(IAPI apiConfig) throws AppException {
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
	
	private void validateCorsConfig(IAPI apiConfig) throws AppException {
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
	private void completeClientApplications(IAPI apiConfig) throws AppException {
		if(CommandParameters.getInstance().isIgnoreClientApps()) return;
		if(apiConfig.getState()==IAPI.STATE_UNPUBLISHED) return;
		ClientApplication loadedApp = null;
		ClientApplication app;
		if(apiConfig.getApplications()!=null) {
			LOG.info("Handling configured client-applications.");
			ListIterator<ClientApplication> it = apiConfig.getApplications().listIterator();
			while(it.hasNext()) {
				app = it.next();
				if(app.getName()!=null) {
					loadedApp = APIManagerAdapter.getInstance().getApplication(app.getName());
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
		ClientApplication app = APIManagerAdapter.getInstance().getAppIdForCredential(credential, type);
		if(app==null) {
			LOG.warn("Unknown application with ("+type+"): '" + credential + "' configured. Ignoring this application.");
			return null;
		}
		return app;
	}
	
	private void completeCaCerts(IAPI apiConfig) throws AppException {
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
			baseDir = new File(this.apiConfigFile).getCanonicalFile().getParent();
		} catch (IOException e1) {
			error.setError("Can't read certificate file.", ErrorCode.CANT_READ_CONFIG_FILE);
			throw new AppException("Can't read certificate file.", ErrorCode.CANT_READ_CONFIG_FILE, e1);
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
	
	private void validateCustomProperties(IAPI apiConfig) throws AppException {
		if(apiConfig.getCustomProperties()!=null) {
			JsonNode configuredProps = APIManagerAdapter.getCustomPropertiesConfig();
			Iterator<String> props = apiConfig.getCustomProperties().keySet().iterator();
			while(props.hasNext()) {
				String propertyKey = props.next();
				String propertyValue = apiConfig.getCustomProperties().get(propertyKey);
				JsonNode configuredProp = configuredProps.at("/api/"+propertyKey);
				if(configuredProp instanceof MissingNode) {
					ErrorState.getInstance().setError("The custom-property: '" + propertyKey + "' is not configured in API-Manager.", ErrorCode.CANT_READ_CONFIG_FILE, false);
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
						ErrorState.getInstance().setError("The value: '" + propertyValue + "' isn't configured for custom property: '" + propertyKey + "'", ErrorCode.CANT_READ_CONFIG_FILE, false);
						throw new AppException("The value: '" + propertyValue + "' isn't configured for custom property: '" + propertyKey + "'", ErrorCode.CANT_READ_CONFIG_FILE);
					}
				}
			}
		}
	}
	
	private byte[] getAPIDefinitionContent() throws AppException {
		try {
			InputStream stream = getAPIDefinitionAsStream();
			Reader reader = new InputStreamReader(stream,StandardCharsets.UTF_8);
			return IOUtils.toByteArray(reader,StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new AppException("Can't read API-Definition from file", ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
		}
	}
	
	/**
	 * To make testing easier we allow reading test-files from classpath as well
	 * @throws AppException when the import Swagger-File can't be read.
	 * @return The import Swagger-File as an InputStream
	 */
	public InputStream getAPIDefinitionAsStream() throws AppException {
		InputStream is = null;
		if(pathToAPIDefinition.endsWith(".url")) {
			return getAPIDefinitionFromURL(Utils.getAPIDefinitionUriFromFile(pathToAPIDefinition));
		} else if(isHttpUri(pathToAPIDefinition)) {
			return getAPIDefinitionFromURL(pathToAPIDefinition);
		} else {
			try {
				File inputFile = new File(pathToAPIDefinition);
				if(inputFile.exists()) { 
					LOG.info("Reading API-Definition (Swagger/WSDL) from file: '" + pathToAPIDefinition + "' (relative path)");
					is = new FileInputStream(pathToAPIDefinition);
				} else {
					String baseDir = new File(this.apiConfigFile).getCanonicalFile().getParent();
					inputFile= new File(baseDir + File.separator + this.pathToAPIDefinition);
					LOG.info("Reading API-Definition (Swagger/WSDL) from file: '" + inputFile.getCanonicalFile() + "' (absolute path)"); 
					if(inputFile.exists()) { 
						is = new FileInputStream(inputFile);
					} else {
						is = this.getClass().getResourceAsStream(pathToAPIDefinition);
					}
				}
				if(is == null) {
					throw new AppException("Unable to read Swagger/WSDL file from: " + pathToAPIDefinition, ErrorCode.CANT_READ_API_DEFINITION_FILE);
				}
			} catch (Exception e) {
				throw new AppException("Unable to read Swagger/WSDL file from: " + pathToAPIDefinition, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
			}
			
		}
		return is;
	}
	
	private InputStream getAPIDefinitionFromURL(String urlToAPIDefinition) throws AppException {
		URLParser url = new URLParser(urlToAPIDefinition);
		String uri = url.getUri();
		String username = url.getUsername();
		String password = url.getPassword();
		CloseableHttpClient httpclient = null;
		try {
			if(username!=null) {
				LOG.info("Loading API-Definition from: " + uri + " ("+username+")");
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(
		                new AuthScope(AuthScope.ANY),
		                new UsernamePasswordCredentials(username, password));
				httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
			} else {
				LOG.info("Loading API-Definition from: " + uri);
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
                        return entity != null ? EntityUtils.toString(entity,StandardCharsets.UTF_8) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpclient.execute(httpGet, responseHandler);
            return new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new AppException("Cannot load API-Definition from URI: "+uri, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
		} finally {
			try {
				httpclient.close();
			} catch (Exception ignore) {}
		}
	}
	
	public static boolean isHttpUri(String pathToAPIDefinition) {
		String httpUri = pathToAPIDefinition.substring(pathToAPIDefinition.indexOf("@")+1);
		return( httpUri.startsWith("http://") || httpUri.startsWith("https://"));
	}
	
	private String getStageConfig(String stage, String apiConfig) {
		if(stage == null) return null;
		File stageFile = new File(stage);
		if(stageFile.exists()) { // This is to support testing with dynamic created files!
			return stageFile.getAbsolutePath();
		}
		if(stage!=null && !stage.equals("NOT_SET")) {
			return apiConfig.substring(0, apiConfig.lastIndexOf(".")+1) + stage + apiConfig.substring(apiConfig.lastIndexOf("."));
		}
		LOG.debug("No stage provided");
		return null;
	}
	
	private IAPI addDefaultPassthroughSecurityProfile(IAPI importApi) throws AppException {
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
	
	private void validateOutboundAuthN(IAPI importApi) throws AppException {
		// Request to use some specific Outbound-AuthN for this API
		if(importApi.getAuthenticationProfiles()!=null && importApi.getAuthenticationProfiles().size()!=0) {
			// For now, we only support one DEFAULT, hence it must be configured as such
			if(importApi.getAuthenticationProfiles().size()>1) {
				throw new AppException("Only one AuthenticationProfile supported.", ErrorCode.CANT_READ_CONFIG_FILE);
			}
			if(importApi.getAuthenticationProfiles().get(0).getType().equals(AuthType.ssl)) 
				handleOutboundSSLAuthN(importApi.getAuthenticationProfiles().get(0));
			importApi.getAuthenticationProfiles().get(0).setIsDefault(true);
			importApi.getAuthenticationProfiles().get(0).setName("_default"); // Otherwise it wont be considered as default by the API-Mgr.
		}
		
	}
	
	private void handleOutboundSSLAuthN(AuthenticationProfile authnProfile) throws AppException {
		if(!authnProfile.getType().equals(AuthType.ssl)) return;
		String clientCert = authnProfile.getParameters().getProperty("certFile");
		String password = authnProfile.getParameters().getProperty("password");
		File clientCertFile = new File(clientCert);
		InputStream is;
		try {
			if(!clientCertFile.exists()) {
				// Try to find file using a relative path to the config file
				String baseDir = new File(this.apiConfigFile).getCanonicalFile().getParent();
				clientCertFile = new File(baseDir + "/" + clientCert);
			}
			if(clientCertFile.exists()) {
				is = new FileInputStream(clientCertFile);
			} else {
				// If not found absolute & relative - Try to load it from ClassPath
				LOG.debug("Trying to load Client-Certificate from classpath");
				is = this.getClass().getResourceAsStream(clientCert);
			}
			if(is==null) {
				throw new AppException("Can't read Client-Cert-File: "+clientCert+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR);
			}
			is.mark(0);
			KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
			try {
				store.load(is, password.toCharArray());
			} catch (IOException e) {
				if(e.getMessage().toLowerCase().contains("keystore password was incorrect")) {
					ErrorState.getInstance().setError("Unable to configure Outbound SSL-Config as password for keystore: '"+clientCertFile+"' is incorrect.", ErrorCode.WRONG_KEYSTORE_PASSWORD, false);
					throw e;
				}
			}
			Enumeration<String> e = store.aliases();
			while (e.hasMoreElements()) {
				String alias = e.nextElement();
				X509Certificate certificate = (X509Certificate) store.getCertificate(alias);
				certificate.getEncoded();
			}
			is.reset();
			JsonNode node = APIManagerAdapter.getFileData(is, clientCert);
			String data = node.get("data").asText();
			authnProfile.getParameters().setProperty("pfx", data);
			authnProfile.getParameters().remove("certFile");
		} catch (Exception e) {
			throw new AppException("Can't read Client-Cert-File: "+clientCert+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR, e);
		} 
	}
	
	private void validateHasQueryStringKey(IAPI importApi) throws AppException {
		if(APIManagerAdapter.getApiManagerVersion().startsWith("7.5")) return; // QueryStringRouting isn't supported
		if(APIManagerAdapter.getInstance().hasAdminAccount()) {
			String apiRoutingKeyEnabled = APIManagerAdapter.getApiManagerConfig("apiRoutingKeyEnabled");
			if(apiRoutingKeyEnabled.equals("true")) {
				if(importApi.getApiRoutingKey()==null) {
					ErrorState.getInstance().setError("API-Manager configured for Query-String option, but API doesn' declare it.", ErrorCode.API_CONFIG_REQUIRES_QUERY_STRING, false);
					throw new AppException("API-Manager configured for Query-String option, but API doesn' declare it.", ErrorCode.API_CONFIG_REQUIRES_QUERY_STRING);
				}
			}
		} else {
			LOG.debug("Can't check if QueryString for API is needed without Admin-Account.");
		}
	}
	
	
	
	private IAPI addImageContent(IAPI importApi) throws AppException {
		File file = null;
		if(importApi.getImage()!=null) { // An image is declared
			try {
				file = new File(importApi.getImage().getFilename());
				if(!file.exists()) { // The image isn't provided with an absolute path, try to read it relativ to the config file
					String baseDir = new File(this.apiConfigFile).getCanonicalFile().getParent();
					file = new File(baseDir + "/" + importApi.getImage().getFilename());
				}
				importApi.getImage().setBaseFilename(file.getName());
				InputStream is = this.getClass().getResourceAsStream(importApi.getImage().getFilename());
				if(file.exists()) {
					LOG.debug("Loading image from: '"+file.getCanonicalFile()+"'");
					importApi.getImage().setImageContent(IOUtils.toByteArray(new FileInputStream(file)));
					return importApi;
				} else if(is!=null) {
					LOG.debug("Trying to load image from classpath");
					// Try to read it from classpath
					importApi.getImage().setImageContent(IOUtils.toByteArray(is));
					return importApi;
				} else {
					throw new AppException("Image not found in filesystem ('"+file+"') or Classpath.", ErrorCode.UNXPECTED_ERROR);
				}
			} catch (Exception e) {
				throw new AppException("Can't read image-file: "+importApi.getImage().getFilename()+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR, e);
			}
		}
		return importApi;
	}

	public String getPathToAPIDefinition() {
		return pathToAPIDefinition;
	}

	public void setPathToAPIDefinition(String pathToAPIDefinition) {
		this.pathToAPIDefinition = pathToAPIDefinition;
	}
	
	
}
