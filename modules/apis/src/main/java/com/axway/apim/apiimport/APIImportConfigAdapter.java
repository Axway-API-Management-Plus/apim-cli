package com.axway.apim.apiimport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.definition.APISpecificationFactory;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.AuthType;
import com.axway.apim.api.model.AuthenticationProfile;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.CorsProfile;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.DeviceType;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.OAuthClientProfile;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictionDeserializer;
import com.axway.apim.api.model.SecurityDevice;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.URLParser;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
	private File apiConfigFile;
	
	/** The APIConfig instance created by the APIConfigImporter */
	private API apiConfig;
	
	/** If true, an OrgAdminUser is used to start the tool */
	private boolean usingOrgAdmin;
	

	/**
	 * Constructor just for testing. Don't use it!
	 * @param apiConfig the desired API to test with
	 * @throws AppException if the API import configuration cannot be loaded/initialized
	 */
	public APIImportConfigAdapter(API apiConfig, File apiConfigFile) throws AppException {
		this.apiConfig = apiConfig;
		this.apiConfigFile = apiConfigFile;
	}
	
	public APIImportConfigAdapter(APIImportParams params) throws AppException {
		this(params.getConfig(), params.getStage(), params.getApiDefintion(), APIManagerAdapter.hasOrgAdmin(), params.getStageConfig());
	}
	
	/**
	 * Constructs the APIImportConfig 
	 * @param apiConfigFileName the API-Config given by the user
	 * @param stage an optional stage used to load overrides and stage specific environment properties
	 * @param pathToAPIDefinition an optional path to the API-Definition (Swagger / WSDL), can be in the config-file as well.
	 * @param usingOrgAdmin access to API-Manager should be limited to the Org-Admin account
	 * @throws AppException if the config-file can't be parsed for some reason
	 */
	public APIImportConfigAdapter(String apiConfigFileName, String stage, String pathToAPIDefinition, boolean usingOrgAdmin, String stageConfig) throws AppException {
		super();
		SimpleModule module = new SimpleModule();
		module.addDeserializer(QuotaRestriction.class, new QuotaRestrictionDeserializer());
		// We would like to get back the original AppExcepption instead of a JsonMappingException
		mapper.disable(DeserializationFeature.WRAP_EXCEPTIONS);
		mapper.registerModule(module);
		
		API baseConfig;
		try {
			this.pathToAPIDefinition = pathToAPIDefinition;
			this.usingOrgAdmin = usingOrgAdmin;
			this.apiConfigFile = Utils.locateConfigFile(apiConfigFileName);
			File stageConfigFile = Utils.getStageConfig(stage, stageConfig, this.apiConfigFile);
			// Validate organization for the base config, if no staged-config is given
			boolean validateOrganization = (stageConfigFile==null) ? true : false;
			ObjectReader reader = mapper.reader();
			baseConfig = reader.withAttribute("validateOrganization", validateOrganization).forType(DesiredAPI.class).readValue(Utils.substitueVariables(this.apiConfigFile));
			if(stageConfigFile!=null) {
				try {
					// If the baseConfig doesn't have a valid organization, the stage config must
					validateOrganization = (baseConfig.getOrganization()==null) ? true : false;
					ObjectReader updater = mapper.readerForUpdating(baseConfig).withAttribute("validateOrganization", validateOrganization);
					// Organization must be valid in staged configuration
					apiConfig = updater.withAttribute("validateOrganization", true).readValue(Utils.substitueVariables(stageConfigFile));
					LOG.info("Loaded stage API-Config from file: " + stageConfigFile);
				} catch (FileNotFoundException e) {
					LOG.warn("No config file found for stage: '"+stage+"'");
					apiConfig = baseConfig;
				}
			} else {
				apiConfig = baseConfig;
			}
		} catch (Exception e) {
			throw new AppException("Cant parse JSON-Config file(s)", ErrorCode.CANT_READ_CONFIG_FILE, e);
		}
	}

	public API getApiConfig() {
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
	public API getDesiredAPI() throws AppException {
		try {
			validateExposurePath(apiConfig);
			validateOrganization(apiConfig);
			checkForAPIDefinitionInConfiguration(apiConfig);
			addDefaultPassthroughSecurityProfile(apiConfig);
			addDefaultAuthenticationProfile(apiConfig);
			validateOutboundProfile(apiConfig);
			validateInboundProfile(apiConfig);
			APISpecification apiSpecification = APISpecificationFactory.getAPISpecification(getAPIDefinitionContent(), this.pathToAPIDefinition, apiConfig.getName());
			apiSpecification.configureBasepath(((DesiredAPI)apiConfig).getBackendBasepath());
			apiConfig.setApiDefinition(apiSpecification);
			addImageContent(apiConfig);
			Utils.validateCustomProperties(apiConfig.getCustomProperties(), Type.api);
			validateDescription(apiConfig);
			validateOutboundAuthN(apiConfig);
			addDefaultCorsProfile(apiConfig);
			validateHasQueryStringKey(apiConfig);
			completeCaCerts(apiConfig);
			addQuotaConfiguration(apiConfig);
			handleAllOrganizations(apiConfig);
			completeClientApplications(apiConfig);
			handleVhost(apiConfig);
			return apiConfig;
		} catch (Exception e) {
			if(e.getCause() instanceof AppException) {
				throw (AppException)e.getCause();
			}
			throw new AppException("Cannot validate/fulfill configuration file.", ErrorCode.CANT_READ_CONFIG_FILE, e);
		}
	}
	
	private void validateExposurePath(API apiConfig) throws AppException {
		if(apiConfig.getPath()==null) {
			throw new AppException("Config-Parameter: 'path' is not given", ErrorCode.CANT_READ_CONFIG_FILE);
		}
		if(!apiConfig.getPath().startsWith("/")) {
			throw new AppException("Config-Parameter: 'path' must start with a \"/\" following by a valid API-Path (e.g. /api/v1/customer).", ErrorCode.CANT_READ_CONFIG_FILE);
		}
	}
	
	private void validateOrganization(API apiConfig) throws AppException {
		if(apiConfig instanceof DesiredTestOnlyAPI) return;
		if(apiConfig.getOrganization()==null || !apiConfig.getOrganization().isDevelopment()) {
			throw new AppException("The given organization: '"+apiConfig.getOrganization()+"' is either unknown or hasn't the Development flag.", ErrorCode.UNKNOWN_ORGANIZATION);
		}
		if(usingOrgAdmin) { // Hardcode the orgId to the organization of the used OrgAdmin
			apiConfig.getOrganization().setId(APIManagerAdapter.getCurrentUser(false).getOrganization().getId());
		}
	}

	private void checkForAPIDefinitionInConfiguration(API apiConfig) throws AppException {
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
	
	private void handleAllOrganizations(API apiConfig) throws AppException {
		if(apiConfig.getClientOrganizations()==null) return;
		if(apiConfig.getState().equals(API.STATE_UNPUBLISHED)) {
			apiConfig.setClientOrganizations(null); // Making sure, orgs are not considered as a changed property
			return;
		}
		if(apiConfig.getClientOrganizations().contains(new Organization.Builder().hasName("ALL").build())) {
			List<Organization> allOrgs =  APIManagerAdapter.getInstance().orgAdapter.getAllOrgs();
			apiConfig.getClientOrganizations().clear();
			apiConfig.getClientOrganizations().addAll(allOrgs);
			((DesiredAPI)apiConfig).setRequestForAllOrgs(true);
		} else {
			// As the API-Manager internally handles the owning organization in the same way, 
			// we have to add the Owning-Org as a desired org
			if(!apiConfig.getClientOrganizations().contains(apiConfig.getOrganization())) {
				apiConfig.getClientOrganizations().add(apiConfig.getOrganization());
			}
			// And validate each configured organization really exists in the API-Manager
			Iterator<Organization> it = apiConfig.getClientOrganizations().iterator();
			String invalidClientOrgs = null;
			List<Organization> foundOrgs = new ArrayList<Organization>();
			while(it.hasNext()) {
				Organization desiredOrg = it.next();
				Organization org = APIManagerAdapter.getInstance().orgAdapter.getOrgForName(desiredOrg.getName());
				if(org==null) {
					LOG.warn("Unknown organization with name: '" + desiredOrg.getName() + "' configured. Ignoring this organization.");
					invalidClientOrgs = invalidClientOrgs==null ? desiredOrg.getName() : invalidClientOrgs + ", "+desiredOrg.getName();
					APIPropertiesExport.getInstance().setProperty(ErrorCode.INVALID_CLIENT_ORGANIZATIONS.name(), invalidClientOrgs);
					it.remove();
					continue;
				}
				it.remove();
				foundOrgs.add(org);
			}
			apiConfig.getClientOrganizations().addAll(foundOrgs);
		}
	}
	
	private void addQuotaConfiguration(API apiConfig) throws AppException {
		if(apiConfig.getState()==API.STATE_UNPUBLISHED) return;
		API importAPI = apiConfig;
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
	
	private void validateDescription(API apiConfig) throws AppException {
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
		} else if(descriptionType.equals("markdownLocal")) {
			if(apiConfig.getMarkdownLocal()==null) {
				throw new AppException("markdownLocal can't be null with descriptionType set to 'markdownLocal'", ErrorCode.CANT_READ_CONFIG_FILE);
			}
			try {
				File markdownFile = new File(apiConfig.getMarkdownLocal());
				if(!markdownFile.exists()) { // The image isn't provided with an absolute path, try to read it relative to the config file
					LOG.trace("Error reading markdown description file (absolute): '" + markdownFile.getCanonicalPath() + "'");
					String baseDir = this.apiConfigFile.getCanonicalFile().getParent();
					markdownFile = new File(baseDir + "/" + apiConfig.getMarkdownLocal());
				}
				if(!markdownFile.exists()) {
					LOG.trace("Error reading markdown description file (relative): '" + markdownFile.getCanonicalPath() + "'");
					throw new AppException("Error reading markdown description file: " + apiConfig.getMarkdownLocal(), ErrorCode.CANT_READ_CONFIG_FILE);
				}
				LOG.debug("Reading local markdown description file: " + markdownFile.getPath());
				String markdownDescription = new String(Files.readAllBytes(markdownFile.toPath()), StandardCharsets.UTF_8);
				apiConfig.setDescriptionManual(markdownDescription);
				apiConfig.setDescriptionType("manual");
			} catch (IOException e) {
				throw new AppException("Error reading markdown description file: " + apiConfig.getMarkdownLocal(), ErrorCode.CANT_READ_CONFIG_FILE, e);
			}
		} else if(descriptionType.equals("original")) {
			return;
		} else {
			throw new AppException("Unknown descriptionType: '"+descriptionType+"'", ErrorCode.CANT_READ_CONFIG_FILE);
		}
	}
	
	private void addDefaultCorsProfile(API apiConfig) throws AppException {
		if(apiConfig.getCorsProfiles()==null) {
			((API)apiConfig).setCorsProfiles(new ArrayList<CorsProfile>());
		}
		// Check if there is a default cors profile declared otherwise create one internally
		boolean defaultCorsFound = false;
		for(CorsProfile profile : apiConfig.getCorsProfiles()) {
			if(profile.getName().equals("_default")) {
				defaultCorsFound = true;
				break;
			}
		}
		if(apiConfig.getCorsProfiles().size()==1) { // Make this CORS-Profile default, even if it's not named default
			apiConfig.getInboundProfiles().get("_default").setCorsProfile(apiConfig.getCorsProfiles().get(0).getName());
		}
		if(!defaultCorsFound) {
			apiConfig.getCorsProfiles().add(CorsProfile.getDefaultCorsProfile());
		}
	}
	
	/**
	 * Purpose of this method is to load the actual existing applications from API-Manager 
	 * based on the provided criteria (App-Name, API-Key, OAuth-ClientId or Ext-ClientId). 
	 * Or, if the APP doesn't exists remove it from the list and log a warning message.
	 * Additionally, for each application it's checked, that the organization has access 
	 * to this API, otherwise it will be removed from the list as well and a warning message is logged.
	 * @param apiConfig
	 * @throws AppException
	 */
	private void completeClientApplications(API apiConfig) throws AppException {
		if(CoreParameters.getInstance().isIgnoreClientApps()) return;
		if(apiConfig.getState()==API.STATE_UNPUBLISHED) return;
		ClientApplication loadedApp = null;
		ClientApplication app;
		if(apiConfig.getApplications()!=null) {
			LOG.info("Handling configured client-applications.");
			ListIterator<ClientApplication> it = apiConfig.getApplications().listIterator();
			String invalidClientApps = null;
			while(it.hasNext()) {
				app = it.next();
				if(app.getName()!=null) {
					ClientAppFilter filter = new ClientAppFilter.Builder().hasName(app.getName()).build();
					loadedApp =  APIManagerAdapter.getInstance().appAdapter.getApplication(filter);
					if(loadedApp==null) {
						LOG.warn("Unknown application with name: '" + filter.getApplicationName() + "' configured. Ignoring this application.");
						invalidClientApps = invalidClientApps==null ? app.getName() : invalidClientApps + ", "+app.getName();
						APIPropertiesExport.getInstance().setProperty(ErrorCode.INVALID_CLIENT_APPLICATIONS.name(), invalidClientApps);
						it.remove();
						continue;
					}
					LOG.info("Found existing application: '"+app.getName()+"' ("+app.getId()+") based on given name '"+app.getName()+"'");
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
				if(!APIManagerAdapter.hasAdminAccount()) {
					if(!apiConfig.getOrganization().equals(loadedApp.getOrganization())) {
						LOG.warn("OrgAdmin can't handle application: '"+loadedApp.getName()+"' belonging to a different organization. Ignoring this application.");
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
		ClientApplication app =  APIManagerAdapter.getInstance().getAppIdForCredential(credential, type);
		if(app==null) {
			LOG.warn("Unknown application with ("+type+"): '" + credential + "' configured. Ignoring this application.");
			return null;
		}
		return app;
	}
	
	private void completeCaCerts(API apiConfig) throws AppException {
		if(apiConfig.getCaCerts()!=null) {
			List<CaCert> completedCaCerts = new ArrayList<CaCert>();
			for(CaCert cert :apiConfig.getCaCerts()) {
				if(cert.getCertBlob()==null) {
					try(InputStream is = getInputStreamForCertFile(cert)) {
						JsonNode certInfo = APIManagerAdapter.getCertInfo(is, null, cert);
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
			baseDir = this.apiConfigFile.getCanonicalFile().getParent();
		} catch (IOException e1) {
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
	
	private byte[] getAPIDefinitionContent() throws AppException {
		try(InputStream stream = getAPIDefinitionAsStream()) {
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
					String baseDir = this.apiConfigFile.getCanonicalFile().getParent();
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
		CloseableHttpClient httpclient = createHttpClient(uri, username, password);
		try {
			RequestConfig config = RequestConfig.custom()
					.setRelativeRedirectsAllowed(true)
					.setCircularRedirectsAllowed(true)
					.build();
			HttpGet httpGet = new HttpGet(uri);
			httpGet.setConfig(config);
			
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

	private CloseableHttpClient createHttpClient(String uri, String username, String password) throws AppException {
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		try {
			addBasicAuthCredential(uri, username, password, httpClientBuilder);
			addSSLContext(uri, httpClientBuilder);
			return httpClientBuilder.build();
		} catch (Exception e) {
			throw new AppException("Error during create http client for retrieving ...", ErrorCode.CANT_CREATE_HTTP_CLIENT);
		}
	}

	private void addSSLContext(String uri, HttpClientBuilder httpClientBuilder) throws KeyManagementException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException {
		if (isHttpsUri(uri)) {
			SSLConnectionSocketFactory sslCtx = createSSLContext();
			if (sslCtx!=null) {
				httpClientBuilder.setSSLSocketFactory(sslCtx);
			}
		}
	}

	private void addBasicAuthCredential(String uri, String username, String password,
			HttpClientBuilder httpClientBuilder) {
		if(this.apiConfig instanceof DesiredTestOnlyAPI) return; // Don't do that when unit-testing
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
	
	private API validateInboundProfile(API importApi) throws AppException {
		if(importApi.getInboundProfiles()==null || importApi.getInboundProfiles().size()==0) {
			Map<String, InboundProfile> def = new HashMap<String, InboundProfile>();
			def.put("_default", InboundProfile.getDefaultInboundProfile());
			importApi.setInboundProfiles(def);
			return importApi;
		}
		Iterator<String> it = importApi.getInboundProfiles().keySet().iterator();
		// Check if a default inbound profile is given
		boolean defaultProfileFound = false;
		while(it.hasNext()) {
			String profileName = it.next();
			if(profileName.equals("_default")) { 
				defaultProfileFound = true;
				continue; // No need to check for the default profile
			}
			// Check the referenced profiles are valid
			InboundProfile profile = importApi.getInboundProfiles().get(profileName);
			if(profile.getCorsProfile()!=null && getCorsProfile(importApi, profile.getCorsProfile())==null) {
				throw new AppException("Inbound profile is referencing a unknown CorsProfile: '"+profile.getCorsProfile()+"'", ErrorCode.REFERENCED_PROFILE_INVALID);
			}
			if(profile.getSecurityProfile()!=null && getSecurityProfile(importApi, profile.getSecurityProfile())==null) {
				throw new AppException("Inbound profile is referencing a unknown SecurityProfile: '"+profile.getSecurityProfile()+"'", ErrorCode.REFERENCED_PROFILE_INVALID);
			}
		}
		/// If not, create a PassThrough!
		if(!defaultProfileFound) {
			InboundProfile defaultProfile = new InboundProfile();
			defaultProfile.setSecurityProfile("_default");
			defaultProfile.setCorsProfile("_default");
			defaultProfile.setMonitorAPI(true);
			defaultProfile.setMonitorSubject("authentication.subject.id");
			importApi.getInboundProfiles().put("_default", defaultProfile);
		}
		return importApi;
	}
	
	private API addDefaultPassthroughSecurityProfile(API importApi) throws AppException {
		boolean hasDefaultProfile = false;
		if(importApi.getSecurityProfiles()==null) importApi.setSecurityProfiles(new ArrayList<SecurityProfile>());
		List<SecurityProfile> profiles = importApi.getSecurityProfiles();
		for(SecurityProfile profile : importApi.getSecurityProfiles()) {
			if(profile.getIsDefault() || profile.getName().equals("_default")) {
				if(hasDefaultProfile) {
					throw new AppException("You can have only one _default SecurityProfile.", ErrorCode.CANT_READ_CONFIG_FILE);
				}
				hasDefaultProfile=true;
				// If the name is _default or flagged as default make it consistent!
				profile.setName("_default");
				profile.setIsDefault(true); 
			}
		}
		if(profiles==null || profiles.size()==0 || !hasDefaultProfile) {
			SecurityProfile passthroughProfile = new SecurityProfile();
			passthroughProfile.setName("_default");
			passthroughProfile.setIsDefault(true);
			SecurityDevice passthroughDevice = new SecurityDevice();
			passthroughDevice.setName("Pass Through");
			passthroughDevice.setType(DeviceType.passThrough);
			passthroughDevice.setOrder(0);
			passthroughDevice.getProperties().put("subjectIdFieldName", "Pass Through");
			passthroughDevice.getProperties().put("removeCredentialsOnSuccess", "true");
			passthroughProfile.getDevices().add(passthroughDevice);
			
			profiles.add(passthroughProfile);
		}
		return importApi;
	}
	
	private API addDefaultAuthenticationProfile(API importApi) throws AppException {
		if(importApi.getAuthenticationProfiles()==null) return importApi; // Nothing to add (no default is needed, as we don't send any Authn-Profile)
		boolean hasDefaultProfile = false;
		List<AuthenticationProfile> profiles = importApi.getAuthenticationProfiles();
		for(AuthenticationProfile profile : profiles) {
			if(profile.getIsDefault() || profile.getName().equals("_default")) {
				if(hasDefaultProfile) {
					throw new AppException("You can have only one AuthenticationProfile configured as default", ErrorCode.CANT_READ_CONFIG_FILE);
				}
				hasDefaultProfile=true;
				// If the name is _default or flagged as default make it consistent!
				profile.setName("_default");
				profile.setIsDefault(true); 
			}
		}
		if(!hasDefaultProfile) {
			LOG.warn("THERE IS NO DEFAULT authenticationProfile CONFIGURED. Auto-Creating a No-Authentication outbound profile as default!");
			AuthenticationProfile noAuthNProfile = new AuthenticationProfile();
			noAuthNProfile.setName("_default");
			noAuthNProfile.setIsDefault(true);
			noAuthNProfile.setType(AuthType.none);
			profiles.add(noAuthNProfile);
		}
		return importApi;
	}
	
	private API validateOutboundProfile(API importApi) throws AppException {
		if(importApi.getOutboundProfiles()==null || importApi.getOutboundProfiles().size()==0) return importApi;
		Iterator<String> it = importApi.getOutboundProfiles().keySet().iterator();
		boolean defaultProfileFound = false;
		while(it.hasNext()) {
			String profileName = it.next();
			OutboundProfile profile = importApi.getOutboundProfiles().get(profileName);
			if(profileName.equals("_default")) {
				defaultProfileFound = true;
				// Validate the _default Outbound-Profile has an AuthN-Profile, otherwise we must add (See issue #133)
				
				if(profile.getAuthenticationProfile()==null) {
					LOG.warn("Provided default outboundProfile doesn't contain AuthN-Profile - Setting it to default");
					profile.setAuthenticationProfile("_default");
				}
				continue;
			}
			// Check the referenced authentication profile is valid
			if(!profile.getAuthenticationProfile().equals("_default")) {
				if(profile.getAuthenticationProfile()!=null && getAuthNProfile(importApi, profile.getAuthenticationProfile())==null) {
					throw new AppException("OutboundProfile is referencing a unknown AuthenticationProfile: '"+profile.getAuthenticationProfile()+"'", ErrorCode.REFERENCED_PROFILE_INVALID);
				}
			}
		}
		if(!defaultProfileFound) {
			OutboundProfile defaultProfile = new OutboundProfile();
			defaultProfile.setAuthenticationProfile("_default");
			defaultProfile.setRouteType("proxy");
			importApi.getOutboundProfiles().put("_default", defaultProfile);
		}
		return importApi;
	}
	
	private void validateOutboundAuthN(API importApi) throws AppException {
		// Request to use some specific Outbound-AuthN for this API
		if(importApi.getAuthenticationProfiles()!=null && importApi.getAuthenticationProfiles().size()!=0) {
			if(importApi.getAuthenticationProfiles().get(0).getType().equals(AuthType.ssl)) { 
				handleOutboundSSLAuthN(importApi.getAuthenticationProfiles().get(0));
			} else if(importApi.getAuthenticationProfiles().get(0).getType().equals(AuthType.oauth)) {
				handleOutboundOAuthAuthN(importApi.getAuthenticationProfiles().get(0));
			}
		}
		
	}
	
	private void handleOutboundOAuthAuthN(AuthenticationProfile authnProfile) throws AppException {
		if(!authnProfile.getType().equals(AuthType.oauth)) return;
		String providerProfile = (String)authnProfile.getParameters().get("providerProfile");
		if(providerProfile!=null && providerProfile.startsWith("<key")) return;
		OAuthClientProfile clientProfile = APIManagerAdapter.getInstance().oauthClientAdapter.getOAuthClientProfile(providerProfile);
		if(clientProfile==null) {
			throw new AppException("The OAuth provider profile is unkown: '"+providerProfile+"'", ErrorCode.REFERENCED_PROFILE_INVALID);
		}
		authnProfile.getParameters().put("providerProfile", clientProfile.getId());
	}
	
	private void handleOutboundSSLAuthN(AuthenticationProfile authnProfile) throws AppException {
		if(!authnProfile.getType().equals(AuthType.ssl)) return;
		String keystore = (String)authnProfile.getParameters().get("certFile");
		String password = (String)authnProfile.getParameters().get("password");
		if(keystore.contains(":")) {
			LOG.warn("Keystore format: <keystorename>:<type> is deprecated. Please remove the keystore type.");
			keystore = keystore.split(":")[0];
		}
		File clientCertFile = new File(keystore);
		try {
			if(!clientCertFile.exists()) {
				// Try to find file using a relative path to the config file
				String baseDir = this.apiConfigFile.getCanonicalFile().getParent();
				clientCertFile = new File(baseDir + "/" + keystore);
			}
			if(!clientCertFile.exists()) {
				// If not found absolute & relative - Try to load it from ClassPath
				LOG.debug("Trying to load Client-Certificate from classpath");
				if(this.getClass().getResource(keystore)==null) {
					throw new AppException("Can't read Client-Certificate-Keystore: "+keystore+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR);
				}
				clientCertFile = new File(this.getClass().getResource(keystore).getFile());
			}
			if(this.apiConfig instanceof DesiredTestOnlyAPI) return; // Skip here when testing
			JsonNode fileData = null;
			try(InputStream is = new FileInputStream(clientCertFile)) {
				fileData = APIManagerAdapter.getFileData(IOUtils.toByteArray(new FileInputStream(clientCertFile)), keystore, ContentType.create("application/x-pkcs12"));
			}
			CaCert cert = new CaCert();
			cert.setCertFile(clientCertFile.getName());
			cert.setInbound("false");
			cert.setOutbound("true");
			// This call is to validate the given password, keystore is valid
			APIManagerAdapter.getCertInfo(new FileInputStream(clientCertFile), password, cert);
			String data = fileData.get("data").asText();
			authnProfile.getParameters().put("pfx", data);
			authnProfile.getParameters().remove("certFile");
		} catch (Exception e) {
			throw new AppException("Can't read Client-Cert-File: "+keystore+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR, e);
		} 
	}
	
	private void validateHasQueryStringKey(API importApi) throws AppException {
		if(importApi.getApiRoutingKey()==null) return; // Nothing to check
		if(importApi instanceof DesiredTestOnlyAPI) return; // Do nothing when unit-testing
		if(APIManagerAdapter.hasAdminAccount()) {
			Boolean apiRoutingKeyEnabled = APIManagerAdapter.getInstance().configAdapter.getConfig(true).getApiRoutingKeyEnabled();
			if(!apiRoutingKeyEnabled) {
				throw new AppException("API-Manager Query-String Routing option is disabled. Please turn it on to use apiRoutingKey.", ErrorCode.QUERY_STRING_ROUTING_DISABLED);
			}
		} else {
			LOG.debug("Can't check if QueryString for API is needed without Admin-Account.");
		}
	}
	
	private API addImageContent(API importApi) throws AppException {
		// No image declared do nothing
		if(importApi.getImage()==null) return importApi;
		File file = null;
		try {
			file = new File(importApi.getImage().getFilename());
			if(!file.exists()) { // The image isn't provided with an absolute path, try to read it relative to the config file
				String baseDir = this.apiConfigFile.getCanonicalFile().getParent();
				file = new File(baseDir + "/" + importApi.getImage().getFilename());
			}
			importApi.getImage().setBaseFilename(file.getName());
			if(file.exists()) {
				LOG.info("Loading image from: '"+file.getCanonicalFile()+"'");
				try(InputStream is = new FileInputStream(file)) {
					importApi.getImage().setImageContent(IOUtils.toByteArray(is));
					return importApi;
				}
			}
			// Try to read it from classpath
			try(InputStream is = this.getClass().getResourceAsStream(importApi.getImage().getFilename())) {
				if(is!=null) {
					LOG.debug("Trying to load image from classpath");
					importApi.getImage().setImageContent(IOUtils.toByteArray(is));
					return importApi;
				}
			}
			// An image is configured, but not found
			throw new AppException("Configured image: '"+importApi.getImage().getFilename()+"' not found in filesystem (Relative/Absolute) or classpath.", ErrorCode.UNXPECTED_ERROR);
		} catch (Exception e) {
			throw new AppException("Can't read configured image-file: "+importApi.getImage().getFilename()+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	public String getPathToAPIDefinition() {
		return pathToAPIDefinition;
	}

	public void setPathToAPIDefinition(String pathToAPIDefinition) {
		this.pathToAPIDefinition = pathToAPIDefinition;
	}
	
	private SSLConnectionSocketFactory createSSLContext() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException {
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

	private String[] getAcceptedTLSProtocols() {
		String protocols = System.getProperty("https.protocols","TLSv1.2"); //default TLSv1.2
		LOG.debug("https protocols: {}",protocols);
		return protocols.split(",");
	}
	
	/*
	 * Refactor the following three method a Generic one
	 */
	
	private CorsProfile getCorsProfile(API api, String profileName) {
		if(api.getCorsProfiles()==null || api.getCorsProfiles().size()==0) return null;
		for(CorsProfile cors : api.getCorsProfiles()) {
			if(profileName.equals(cors.getName())) return cors;
		}
		return null;
	}
	
	private AuthenticationProfile getAuthNProfile(API api, String profileName) {
		if(api.getAuthenticationProfiles()==null || api.getAuthenticationProfiles().size()==0) return null;
		for(AuthenticationProfile profile : api.getAuthenticationProfiles()) {
			if(profileName.equals(profile.getName())) return profile;
		}
		return null;
	}
	
	private SecurityProfile getSecurityProfile(API api, String profileName) throws AppException {
		if(api.getSecurityProfiles()==null || api.getSecurityProfiles().size()==0) return null;
		for(SecurityProfile profile : api.getSecurityProfiles()) {
			if(profileName.equals(profile.getName())) return profile;
		}
		return null;
	}
	
	private void handleVhost(API apiConfig) {
		if(apiConfig.getVhost() == null) return;
		// Consider an empty VHost as not set, as it is logically not possible to have an empty VHost.
		if("".equals(apiConfig.getVhost()) ) {
			apiConfig.setVhost(null);
		}
	}
}
