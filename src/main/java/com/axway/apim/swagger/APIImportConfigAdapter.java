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
import java.util.ArrayList;
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
import com.axway.apim.lib.Utils;
import com.axway.apim.swagger.api.properties.APIDefintion;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.properties.cacerts.CaCert;
import com.axway.apim.swagger.api.properties.corsprofiles.CorsProfile;
import com.axway.apim.swagger.api.properties.organization.Organization;
import com.axway.apim.swagger.api.properties.quota.APIQuota;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityDevice;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.axway.apim.swagger.api.state.AbstractAPI;
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
	

	private String pathToAPIDefinition;
	
	private String apiConfigFile;
	
	private IAPI apiConfig;
	
	private boolean hasAdminAccount;
	
	private boolean usingOrgAdmin;
	
	private ErrorState error = ErrorState.getInstance();


	/**
	 * Constructs the APIImportConfig 
	 * @param apiConfig
	 * @param stage
	 * @param pathToAPIDefinition
	 * @param hasAdminAccount - set to true, if an AdminAccount is available.
	 * @throws AppException
	 */
	public APIImportConfigAdapter(String apiConfigFile, String stage, String pathToAPIDefinition, boolean usingOrgAdmin, boolean hasAdminAccount) throws AppException {
		super();
		this.apiConfigFile = apiConfigFile;
		this.pathToAPIDefinition = pathToAPIDefinition;
		this.usingOrgAdmin = usingOrgAdmin;
		this.hasAdminAccount = hasAdminAccount;
		IAPI baseConfig;
		try {
			baseConfig = mapper.readValue(new File(apiConfigFile), DesiredAPI.class);
			ObjectReader updater = mapper.readerForUpdating(baseConfig);
			if(getStageConfig(stage, apiConfigFile)!=null) {
				LOG.info("Overriding configuration from: " + getStageConfig(stage, apiConfigFile));
				apiConfig = updater.readValue(new File(getStageConfig(stage, apiConfigFile)));
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
	 * <li>the API-Config is read</li>
	 * <li>the API-Config is merged with the override</li>
	 * <li>the API-Definition is read</li>
	 * <li>Additionally some validations & completions are made here</li>
	 * <li>in the future: This is the place to do some default handling.
	 * 
	 * @return IAPIDefintion with the desired state of the API. This state will be 
	 * the input to create the APIChangeState.
	 * 
	 * @throws AppException if the state can't be created.
	 * @see {@link IAPI}, {@link AbstractAPI}
	 */
	public IAPI getDesiredAPI() throws AppException {
		try {
			validateOrganization(apiConfig);
			addDefaultPassthroughSecurityProfile(apiConfig);
			APIDefintion apiDefinition = new APIDefintion(getAPIDefinitionContent());
			apiDefinition.setAPIDefinitionFile(this.pathToAPIDefinition);
			apiConfig.setAPIDefinition(apiDefinition);
			addImageContent(apiConfig);
			validateCustomProperties(apiConfig);
			validateDescription(apiConfig);
			validateCorsConfig(apiConfig);
			validateOutboundAuthN(apiConfig);
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
	
	private void validateOrganization(IAPI apiConfig) throws AppException {
		if(usingOrgAdmin) { // Hardcode the orgId to the organization of the used OrgAdmin
			apiConfig.setOrgId(APIManagerAdapter.getCurrentUser(false).getOrganizationId());
		}
	}

	private void checkForAPIDefinitionInConfiguration(IAPI stagedConfig, IAPI baseConfig) throws AppException {
		String path = getCurrentPath();
		LOG.info("path={}",path);
		if (StringUtils.isEmpty(this.pathToAPIDefinition)) {
			if (StringUtils.isNotEmpty(stagedConfig.getApiDefinitionImport())) {
				this.pathToAPIDefinition=baseConfig.getApiDefinitionImport();
				LOG.info("Reading API Definition from configuration file");
			} else {
				throw new AppException("No API Definition configured", ErrorCode.NO_API_DEFINITION_CONFIGURED,false);
			}
		}
		LOG.info("API Definition={}",this.pathToAPIDefinition);
	}

	private String getCurrentPath() {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		return s;
	}
	
	private void handleAllOrganizations(IAPI apiConfig) throws AppException {
		if(apiConfig.getClientOrganizations()==null) return;
		if(apiConfig.getState()==IAPI.STATE_UNPUBLISHED) return;
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
			File inputFile = new File(pathToAPIDefinition);
			try {
				if(inputFile.exists()) { 
					is = new FileInputStream(pathToAPIDefinition);
				} else {
					String baseDir = new File(this.apiConfig).getCanonicalFile().getParent();
					inputFile= new File(baseDir + File.separator + this.pathToAPIDefinition);
					if(inputFile.exists()) { 
						is = new FileInputStream(inputFile);
					} else {
						is = this.getClass().getResourceAsStream(pathToAPIDefinition);
					}
					if(is == null) {
						throw new AppException("Unable to read swagger file from: " + pathToAPIDefinition, ErrorCode.CANT_READ_API_DEFINITION_FILE);
					}
				}
				
			} catch (Exception e) {
				throw new AppException("Unable to read swagger file from: " + pathToAPIDefinition, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
			}
			
		}
		return is;
	}
	
	private InputStream getAPIDefinitionFromURL(String urlToAPIDefinition) throws AppException {
		String uri = null;
		String username = null;
		String password = null;
		String[] temp = urlToAPIDefinition.split("@");
		if(temp.length==1) {
			uri = temp[0];
		} else if(temp.length==2) {
			username = temp[0].substring(0, temp[0].indexOf("/"));
			password = temp[0].substring(temp[0].indexOf("/")+1);
			uri = temp[1];
		} else {
			throw new AppException("API-Definition URL has an invalid format. ", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
		CloseableHttpClient httpclient = null;
		try {
			LOG.info("Loading API-Definition from: " + uri);
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
			importApi.getAuthenticationProfiles().get(0).setIsDefault(true);
			importApi.getAuthenticationProfiles().get(0).setName("_default"); // Otherwise it wont be considered as default by the API-Mgr.
		}
		
	}
	
	private IAPI addImageContent(IAPI importApi) throws AppException {
		if(importApi.getImage()!=null) { // An image is declared
			try {
				String baseDir = new File(this.apiConfigFile).getParent();
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

	public String getPathToAPIDefinition() {
		return pathToAPIDefinition;
	}

	public void setPathToAPIDefinition(String pathToAPIDefinition) {
		this.pathToAPIDefinition = pathToAPIDefinition;
	}
	
	
}
