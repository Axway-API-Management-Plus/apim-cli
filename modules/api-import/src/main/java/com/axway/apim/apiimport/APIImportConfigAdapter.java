package com.axway.apim.apiimport;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
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
import com.axway.apim.api.model.DeviceType;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictionDeserializer;
import com.axway.apim.api.model.SecurityDevice;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.URLParser;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
	private API apiConfig;
	
	/** If true, an OrgAdminUser is used to start the tool */
	private boolean usingOrgAdmin;
	
	private ErrorState error = ErrorState.getInstance();
	

	/**
	 * Constructor just for testing. Don't use it!
	 * @param apiConfig the desired API to test with
	 * @param apiConfigFile this is the given config file
	 * @throws AppException 
	 */
	public APIImportConfigAdapter(API apiConfig, String apiConfigFile) throws AppException {
		this.apiConfig = apiConfig;
		this.apiConfigFile = apiConfigFile;
	}
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
		SimpleModule module = new SimpleModule();
		module.addDeserializer(QuotaRestriction.class, new QuotaRestrictionDeserializer());
		mapper.registerModule(module);
		API baseConfig;
		try {
			this.pathToAPIDefinition = pathToAPIDefinition;
			this.usingOrgAdmin = usingOrgAdmin;
			this.apiConfigFile = locateAPIConfigFile(apiConfigFile);
			baseConfig = mapper.readValue(substitueVariables(new File(this.apiConfigFile)), DesiredAPI.class);
			if(getStageConfig(stage, this.apiConfigFile)!=null) {
				try {
					ObjectReader updater = mapper.readerForUpdating(baseConfig);
					apiConfig = updater.readValue(substitueVariables(new File(getStageConfig(stage, this.apiConfigFile))));
					LOG.info("Loaded stage API-Config from file: " + getStageConfig(stage, this.apiConfigFile));
				} catch (FileNotFoundException e) {
					LOG.warn("No config file found for stage: '"+stage+"'");
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
	
	private static String locateAPIConfigFile(String apiConfigFile) throws AppException {
		try {
			apiConfigFile = URLDecoder.decode(apiConfigFile, "UTF-8");
			File configFile = new File(apiConfigFile);
			if(configFile.exists()) return configFile.getCanonicalPath();
			// This is mainly to load the samples sitting inside the package!
			String installFolder = new File(APIImportConfigAdapter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getParent();
			configFile = new File(installFolder + File.separator + apiConfigFile);
			if(configFile.exists()) return configFile.getCanonicalPath();
			throw new AppException("Unable to find given Config-File: '"+apiConfigFile+"'", ErrorCode.CANT_READ_CONFIG_FILE);
		} catch (Exception e) {
			throw new AppException("Unable to find given Config-File: '"+apiConfigFile+"'", ErrorCode.CANT_READ_CONFIG_FILE);
		}
	}
	
	/**
	 * This method is replacing variables such as ${TokenEndpoint} with declared variables coming from either 
	 * the Environment-Variables or from system-properties.
	 * @param inputFile The API-Config file to be replaced and returned as String
	 * @return a String representation of the API-Config-File
	 * @throws IOException if the file can't be found
	 */
	private String substitueVariables(File inputFile) throws IOException {
		StringSubstitutor substitutor = new StringSubstitutor(CommandParameters.getInstance().getEnvironmentProperties());
		String givenConfig = new String(Files.readAllBytes(inputFile.toPath()), StandardCharsets.UTF_8);
		givenConfig = StringSubstitutor.replace(givenConfig, System.getenv());
		return substitutor.replace(givenConfig);
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
			addDefaultCorsProfile(apiConfig);
			addDefaultAuthenticationProfile(apiConfig);
			addDefaultOutboundProfile(apiConfig);
			addDefaultInboundProfile(apiConfig);
			APISpecification apiSpecification = APISpecificationFactory.getAPISpecification(getAPIDefinitionContent(), this.pathToAPIDefinition, ((DesiredAPI)apiConfig).getBackendBasepath());
			apiConfig.setAPIDefinition(apiSpecification);
			addImageContent(apiConfig);
			validateCustomProperties(apiConfig);
			validateDescription(apiConfig);
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
	
	/**
	 * The purpose of this method is to translated the given Method-Names into internal 
	 * operationId. These operationIds are created and then known, when the API has 
	 * been inserted. 
	 * Translating the methodNames to operationIds already during import is required for 
	 * the comparison between the desired and actual API.
	 * @param desiredAPI the configured desired API
	 * @param actualAPI a potentially existing actual API
	 * @return the desired API containing operationId in Inbound- and Outbound-Profiles
	 * @throws AppException when something goes wrong
	 */
	public API completeDesiredAPI(API desiredAPI, API actualAPI) throws AppException {
		if(actualAPI==null) return desiredAPI;
		return desiredAPI;
	}
	
	private void validateExposurePath(API apiConfig) throws AppException {
		if(apiConfig.getPath()==null) {
			ErrorState.getInstance().setError("Config-Parameter: 'path' is not given", ErrorCode.CANT_READ_CONFIG_FILE, false);
			throw new AppException("Path is invalid.", ErrorCode.CANT_READ_CONFIG_FILE);
		}
		if(!apiConfig.getPath().startsWith("/")) {
			ErrorState.getInstance().setError("Config-Parameter: 'path' must start with a \"/\" following by a valid API-Path (e.g. /api/v1/customer).", ErrorCode.CANT_READ_CONFIG_FILE, false);
			throw new AppException("Path is invalid.", ErrorCode.CANT_READ_CONFIG_FILE);
		}
	}
	
	private void validateOrganization(API apiConfig) throws AppException {
		if(apiConfig instanceof DesiredTestOnlyAPI) return;
		if(apiConfig.getOrganization()==null || !apiConfig.getOrganization().getDevelopment()) {
			error.setError("The given organization: '"+apiConfig.getOrganization()+"' is either unknown or hasn't the Development flag.", ErrorCode.UNKNOWN_ORGANIZATION, false);
			throw new AppException("The given organization: '"+apiConfig.getOrganization()+"' is either unknown or hasn't the Development flag.", ErrorCode.UNKNOWN_ORGANIZATION);
		}
		if(usingOrgAdmin) { // Hardcode the orgId to the organization of the used OrgAdmin
			apiConfig.getOrganization().setId(APIManagerAdapter.getCurrentUser(false).getOrganizationId());
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
				ErrorState.getInstance().setError("No API Definition configured", ErrorCode.NO_API_DEFINITION_CONFIGURED, false);
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
		List<Organization> allOrgs =  APIManagerAdapter.getInstance().orgAdapter.getAllOrgs();
		if(apiConfig.getClientOrganizations().contains(new Organization().setName("ALL"))) {
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
			while(it.hasNext()) {
				Organization desiredOrg = it.next();
				if(!allOrgs.contains(desiredOrg)) {
					LOG.warn("Unknown organization with name: '" + desiredOrg.getName() + "' configured. Ignoring this organization.");
					invalidClientOrgs = invalidClientOrgs==null ? desiredOrg.getName() : invalidClientOrgs + ", "+desiredOrg.getName();
					APIPropertiesExport.getInstance().setProperty(ErrorCode.INVALID_CLIENT_ORGANIZATIONS.name(), invalidClientOrgs);
					it.remove();
					continue;
				}
			}
		}
	}
	
	private void addQuotaConfiguration(API apiConfig) throws AppException {
		if(apiConfig.getState()==API.STATE_UNPUBLISHED) return;
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
		} else if(descriptionType.equals("original")) {
			return;
		} else {
			throw new AppException("Unknown descriptionType: '"+descriptionType.equals("manual")+"'", ErrorCode.CANT_READ_CONFIG_FILE);
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
		if(CommandParameters.getInstance().isIgnoreClientApps()) return;
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
	
	private void validateCustomProperties(API apiConfig) throws AppException {
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
	
	private String getStageConfig(String stage, String apiConfig) {
		if(stage == null) return null;
		File stageFile = new File(stage);
		if(stageFile.exists()) { // This is to support testing with dynamic created files!
			return stageFile.getAbsolutePath();
		}
		if(!stage.equals("NOT_SET")) {
			stageFile = new File(apiConfig.substring(0, apiConfig.lastIndexOf(".")+1) + stage + apiConfig.substring(apiConfig.lastIndexOf(".")));
			File subDirStageFile = new File(stageFile.getParentFile()+"/"+stage+"/"+stageFile.getName());
			if(stageFile.exists()) {
				return stageFile.getAbsolutePath();
			} else if(subDirStageFile.exists()) {
				return subDirStageFile.getAbsolutePath();
			} else {
				return null;
			}
		}
		LOG.debug("No stage provided");
		return null;
	}
	
	private API addDefaultInboundProfile(API importApi) throws AppException {
		if(importApi.getInboundProfiles()==null || importApi.getInboundProfiles().size()==0) return importApi;
		Iterator<String> it = importApi.getInboundProfiles().keySet().iterator();
		while(it.hasNext()) {
			String profileName = it.next();
			if(profileName.equals("_default")) return importApi; // Nothing to, there is a default profile
		}
		InboundProfile defaultProfile = new InboundProfile();
		defaultProfile.setSecurityProfile("_default");
		defaultProfile.setCorsProfile("_default");
		defaultProfile.setMonitorAPI(true);
		defaultProfile.setMonitorSubject("authentication.subject.id");
		importApi.getInboundProfiles().put("_default", defaultProfile);
		return importApi;
	}
	
	private API addDefaultPassthroughSecurityProfile(API importApi) throws AppException {
		boolean hasDefaultProfile = false;
		if(importApi.getSecurityProfiles()==null) importApi.setSecurityProfiles(new ArrayList<SecurityProfile>());
		List<SecurityProfile> profiles = importApi.getSecurityProfiles();
		for(SecurityProfile profile : importApi.getSecurityProfiles()) {
			if(profile.getIsDefault() || profile.getName().equals("_default")) {
				if(hasDefaultProfile) {
					ErrorState.getInstance().setError("You can have only one _default SecurityProfile.", ErrorCode.CANT_READ_CONFIG_FILE, false);
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
					ErrorState.getInstance().setError("You can have only one AuthenticationProfile configured as default", ErrorCode.CANT_READ_CONFIG_FILE, false);
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
	
	private API addDefaultOutboundProfile(API importApi) throws AppException {
		if(importApi.getOutboundProfiles()==null || importApi.getOutboundProfiles().size()==0) return importApi;
		Iterator<String> it = importApi.getOutboundProfiles().keySet().iterator();
		while(it.hasNext()) {
			String profileName = it.next();
			if(profileName.equals("_default")) {
				// Validate the _default Outbound-Profile has an AuthN-Profile, otherwise we must add (See isseu #133)
				OutboundProfile profile = importApi.getOutboundProfiles().get(profileName);
				if(profile.getAuthenticationProfile()==null) {
					LOG.warn("Provided default outboundProfile doesn't contain AuthN-Profile - Setting it to default");
					profile.setAuthenticationProfile("_default");
				}
			}
			return importApi;
		}
		OutboundProfile defaultProfile = new OutboundProfile();
		defaultProfile.setAuthenticationProfile("_default");
		defaultProfile.setRouteType("proxy");
		importApi.getOutboundProfiles().put("_default", defaultProfile);
		return importApi;
	}
	
	private void validateOutboundAuthN(API importApi) throws AppException {
		// Request to use some specific Outbound-AuthN for this API
		if(importApi.getAuthenticationProfiles()!=null && importApi.getAuthenticationProfiles().size()!=0) {
			if(importApi.getAuthenticationProfiles().get(0).getType().equals(AuthType.ssl)) 
				handleOutboundSSLAuthN(importApi.getAuthenticationProfiles().get(0));
		}
		
	}
	
	private void handleOutboundSSLAuthN(AuthenticationProfile authnProfile) throws AppException {
		if(!authnProfile.getType().equals(AuthType.ssl)) return;
		String clientCert = (String)authnProfile.getParameters().get("certFile");
		String password = (String)authnProfile.getParameters().get("password");
		String[] result = extractKeystoreTypeFromCertFile(clientCert);
		clientCert 			= result[0];
		String storeType 	= result[1];
		File clientCertFile = new File(clientCert);
		String clientCertClasspath = null;
		try {
			if(!clientCertFile.exists()) {
				// Try to find file using a relative path to the config file
				String baseDir = new File(this.apiConfigFile).getCanonicalFile().getParent();
				clientCertFile = new File(baseDir + "/" + clientCert);
			}
			if(!clientCertFile.exists()) {
				// If not found absolute & relative - Try to load it from ClassPath
				LOG.debug("Trying to load Client-Certificate from classpath");
				if(this.getClass().getResource(clientCert)==null) {
					throw new AppException("Can't read Client-Certificate-Keystore: "+clientCert+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR);
				}
				clientCertClasspath = clientCert;
			}
			KeyStore store = null;
			store = loadKeystore(clientCertFile, clientCertClasspath, storeType, password);
			if(store==null) {
				ErrorState.getInstance().setError("Unable to configure Outbound SSL-Config. Can't load keystore: '"+clientCertFile+"' for any reason. "
						+ "Turn on debug to see log messages.", ErrorCode.WRONG_KEYSTORE_PASSWORD, false);
				throw new AppException("Unable to configure Outbound SSL-Config. Can't load keystore: '"+clientCertFile+"' for any reason.", ErrorCode.WRONG_KEYSTORE_PASSWORD);
			}
			X509Certificate certificate = null;
			Enumeration<String> e = store.aliases();
			while (e.hasMoreElements()) {
				String alias = e.nextElement();
				certificate = (X509Certificate) store.getCertificate(alias);
				certificate.getEncoded();
			}
			if(this.apiConfig instanceof DesiredTestOnlyAPI) return; // Skip here when testing
			JsonNode node = APIManagerAdapter.getFileData(certificate.getEncoded(), clientCert);
			String data = node.get("data").asText();
			authnProfile.getParameters().put("pfx", data);
			authnProfile.getParameters().remove("certFile");
		} catch (Exception e) {
			throw new AppException("Can't read Client-Cert-File: "+clientCert+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR, e);
		} 
	}
	
	private KeyStore loadKeystore(File clientCertFile, String clientCertClasspath, String keystoreType, String password) throws IOException {
		InputStream is = null;
		KeyStore store = null;

		if(keystoreType!=null) {
			try {
				// Get the Inputstream and load the keystore with the given Keystore-Type
				if(clientCertClasspath==null) {
					is = new BufferedInputStream(new FileInputStream(clientCertFile));
				} else {
					is = this.getClass().getResourceAsStream(clientCertClasspath);
				}
				LOG.debug("Loading keystore: '"+clientCertFile+"' using keystore type: '"+keystoreType+"'");
				store = KeyStore.getInstance(keystoreType);
				store.load(is, password.toCharArray());
				return store;
			} catch (IOException e) {
				if(e.getMessage()!=null && e.getMessage().toLowerCase().contains("keystore password was incorrect")) {
					ErrorState.getInstance().setError("Unable to configure Outbound SSL-Config as password for keystore: is incorrect.", ErrorCode.WRONG_KEYSTORE_PASSWORD, false);
					throw e;
				}
				LOG.debug("Error message using type: " + keystoreType + " Error-Message: " + e.getMessage());
				throw e;
			} catch (Exception e) {
				LOG.debug("Error message using type: " + keystoreType + " Error-Message: " + e.getMessage());
				return null;
			} finally {
				if(is!=null) is.close();
			}
		}
		// Otherwise we try every known type		
		LOG.debug("Loading keystore: '"+clientCertFile+"' trying the following types: " + Security.getAlgorithms("KeyStore"));
		for(String type : Security.getAlgorithms("KeyStore")) {
			try {
				LOG.debug("Trying to load keystore: '"+clientCertFile+"' using type: '"+type+"'");
				// Get the Inputstream and load the keystore with the given Keystore-Type
				if(clientCertClasspath==null) {
					is = new BufferedInputStream(new FileInputStream(clientCertFile));
				} else {
					is = this.getClass().getResourceAsStream(clientCertClasspath);
				}
				store = KeyStore.getInstance(type);
				store.load(is, password.toCharArray());
				if(store!=null) {
					LOG.debug("Successfully loaded keystore: '"+clientCertFile+"' with type: " + type);
					return store;
				}
			} catch (IOException e) {
				if(e.getMessage()!=null && e.getMessage().toLowerCase().contains("keystore password was incorrect")) {
					ErrorState.getInstance().setError("Unable to configure Outbound SSL-Config as password for keystore: is incorrect.", ErrorCode.WRONG_KEYSTORE_PASSWORD, false);
					throw e;
				}
				LOG.debug("Error message using type: " + keystoreType + " Error-Message: " + e.getMessage(), e);
			} catch (Exception e) {
				LOG.debug("Error message using type: " + keystoreType + " Error-Message: " + e.getMessage(), e);
			} finally {
				if(is!=null) is.close();
			}
		}
		return null;
	}
	
	private String[] extractKeystoreTypeFromCertFile(String certFileName) throws AppException {
		if(!certFileName.contains(":")) return new String[]{certFileName, null};
		int pos = certFileName.lastIndexOf(":");
		if(pos<3) return new String[]{certFileName, null}; // This occurs for the following format: c:/path/to/my/store
		String type = certFileName.substring(pos+1);
		if(!Security.getAlgorithms("KeyStore").contains(type)) {
			ErrorState.getInstance().setError("Unknown keystore type: '"+type+"'. Supported: " + Security.getAlgorithms("KeyStore"), ErrorCode.WRONG_KEYSTORE_PASSWORD);
			throw new AppException("Unknown keystore type: '"+type+"'. Supported: " + Security.getAlgorithms("KeyStore"), ErrorCode.WRONG_KEYSTORE_PASSWORD);
		}
		certFileName = certFileName.substring(0, pos);
		return new String[]{certFileName, type};
	}
	
	private void validateHasQueryStringKey(API importApi) throws AppException {
		if(1==1) return;
		if(importApi instanceof DesiredTestOnlyAPI) return; // Do nothing when unit-testing
		if(APIManagerAdapter.getApiManagerVersion().startsWith("7.5")) return; // QueryStringRouting isn't supported
		if(APIManagerAdapter.getInstance().hasAdminAccount()) {
			String apiRoutingKeyEnabled = APIManagerAdapter.getInstance().configAdapter.getApiManagerConfig("apiRoutingKeyEnabled");
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
	
	
	
	private API addImageContent(API importApi) throws AppException {
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
	
	
	
}
