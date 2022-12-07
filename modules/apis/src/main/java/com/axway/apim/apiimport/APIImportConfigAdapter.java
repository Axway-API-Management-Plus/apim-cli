package com.axway.apim.apiimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.axway.apim.api.model.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer.DeserializeMode;
import com.axway.apim.api.API;
import com.axway.apim.api.apiSpecification.APISpecification;
import com.axway.apim.api.apiSpecification.APISpecificationFactory;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * The APIConfig reflects the given API-Configuration plus the API-Definition, which is either a
 * Swagger-File or a WSDL.
 * This class will read the API-Configuration plus the optional set stage and the API-Definition.
 *
 * @author cwiechmann
 */
public class APIImportConfigAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(APIImportConfigAdapter.class);

	private ObjectMapper mapper = new ObjectMapper();


	/** This is the given path to WSDL or Swagger. It is either set using -a parameter or as part of the config file */
	private String pathToAPIDefinition;

	/** The API-Config-File given by the user with -c parameter */
	private File apiConfigFile;

	/** The APIConfig instance created by the APIConfigImporter */
	private API apiConfig;

	/** If true, an OrgAdminUser is used to start the tool */
	private boolean usingOrgAdmin;


	public APIImportConfigAdapter(APIImportParams params) throws AppException {
		this(params.getConfig(), params.getStage(), params.getApiDefintion(), APIManagerAdapter.hasOrgAdmin(), params.getStageConfig());
	}

	/**
	 * Constructs the APIImportConfig
	 * @param apiConfigFileName the API-Config given by the user
	 * @param stage an optional stage used to load overrides and stage specific environment properties
	 * @param pathToAPIDefinition an optional path to the API-Definition (Swagger / WSDL), can be in the config-file as well.
	 * @param usingOrgAdmin access to API-Manager should be limited to the Org-Admin account
	 * @param stageConfig a stage config string
	 * @throws AppException if the config-file can't be parsed for some reason
	 */
	public APIImportConfigAdapter(String apiConfigFileName, String stage, String pathToAPIDefinition, boolean usingOrgAdmin, String stageConfig) throws AppException {
		super();
		SimpleModule module = new SimpleModule();
		module.addDeserializer(QuotaRestriction.class, new QuotaRestrictionDeserializer(DeserializeMode.configFile, false));
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
			baseConfig = reader.withAttribute("validateOrganization", validateOrganization).forType(DesiredAPI.class).readValue(Utils.substituteVariables(this.apiConfigFile));
			if(stageConfigFile!=null) {
				try {
					// If the baseConfig doesn't have a valid organization, the stage config must
					validateOrganization = (baseConfig.getOrganization()==null) ? true : false;
					ObjectReader updater = mapper.readerForUpdating(baseConfig).withAttribute("validateOrganization", validateOrganization);
					// Organization must be valid in staged configuration
					apiConfig = updater.withAttribute("validateOrganization", true).readValue(Utils.substituteVariables(stageConfigFile));
					LOG.info("Loaded stage API-Config from file: " + stageConfigFile);
				} catch (FileNotFoundException e) {
					LOG.warn("No config file found for stage: '"+stage+"'");
					apiConfig = baseConfig;
				}
			} else {
				apiConfig = baseConfig;
			}
		} catch (MismatchedInputException e) {
			if(e.getMessage().contains("com.axway.apim.api.model.APISpecIncludeExcludeFilter")) {
				throw new AppException("An error occurred while reading the API specification filters. Please note that the filter structure has changed "
						+ "between version 1.8.0 and 1.9.0. You can find more information here: "
						+ "https://github.com/Axway-API-Management-Plus/apim-cli/wiki/2.1.10-API-Specification#filter-api-specifications", ErrorCode.CANT_READ_CONFIG_FILE, e);
			} else {
				throw new AppException("Error reading API-Config file(s)", "Exception: " + e.getClass().getName() + ": " + e.getMessage(), ErrorCode.CANT_READ_CONFIG_FILE, e);
			}
		} catch (JsonParseException e) {
			throw new AppException("Cannot parse API-Config file(s).", "Exception: " + e.getClass().getName() + ": " + e.getMessage(), ErrorCode.CANT_READ_JSON_PAYLOAD, e);
		} catch (Exception e) {
			throw new AppException("Error reading API-Config file(s)", "Exception: " + e.getClass().getName() + ": " + e.getMessage(), ErrorCode.CANT_READ_CONFIG_FILE, e);
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
			addAPISpecification(apiConfig);
			addDefaultPassthroughSecurityProfile(apiConfig);
			addDefaultAuthenticationProfile(apiConfig);
			validateOutboundProfile(apiConfig);
			validateInboundProfile(apiConfig);
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
			validateMethodDescription(apiConfig.getApiMethods());
			return apiConfig;
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			if(e.getCause() instanceof AppException) {
				throw (AppException)e.getCause();
			}
			throw new AppException("Cannot validate/fulfill configuration file.", ErrorCode.CANT_READ_CONFIG_FILE, e);
		}
	}

	private void validateExposurePath(API apiConfig) throws AppException {
		if(apiConfig.getPath()==null) {
			throw new AppException("API-Config parameter: 'path' is missing. Please check your API-Config file.", ErrorCode.CANT_READ_CONFIG_FILE);
		}
		if(!apiConfig.getPath().startsWith("/")) {
			throw new AppException("API-Config parameter: 'path' must start with a \"/\" following by a valid API-Path (e.g. /api/v1/customer).", ErrorCode.CANT_READ_CONFIG_FILE);
		}
	}

	private void validateOrganization(API apiConfig) throws AppException {
		if(apiConfig instanceof DesiredTestOnlyAPI) return;
		if(apiConfig.getOrganization()==null || !apiConfig.getOrganization().isDevelopment()) {
			throw new AppException("The given organization: '"+apiConfig.getOrganization()+"' is either unknown or hasn't the Development flag.", ErrorCode.UNKNOWN_ORGANIZATION);
		}
//		if(usingOrgAdmin) { // Hardcode the orgId to the organization of the used OrgAdmin
//			//apiConfig.get
//			apiConfig.getOrganization().setId(APIManagerAdapter.getCurrentUser(false).getOrganization().getId());
//		}
	}

	private void addAPISpecification(API apiConfig) throws IOException {
		APISpecification apiSpecification;
		if(((DesiredAPI)apiConfig).getDesiredAPISpecification()!=null) {
			// API-Specification object that might contain filters, the type of an API, etc.
			apiSpecification = APISpecificationFactory.getAPISpecification(((DesiredAPI)apiConfig).getDesiredAPISpecification(), this.apiConfigFile.getCanonicalFile().getParent(), apiConfig.getName());
		} else if (StringUtils.isNotEmpty(this.pathToAPIDefinition)) {
			apiSpecification = APISpecificationFactory.getAPISpecification(this.pathToAPIDefinition, this.apiConfigFile.getCanonicalFile().getParent(), apiConfig.getName());
		} else if (StringUtils.isNotEmpty(apiConfig.getApiDefinitionImport()))  {
			apiSpecification = APISpecificationFactory.getAPISpecification(apiConfig.getApiDefinitionImport(), this.apiConfigFile.getCanonicalFile().getParent(), apiConfig.getName());
			this.pathToAPIDefinition=apiConfig.getApiDefinitionImport();
			LOG.debug("Reading API Definition from configuration file");
		} else {
			throw new AppException("No API Specification configured", ErrorCode.NO_API_DEFINITION_CONFIGURED);
		}
		apiSpecification.configureBasePath(((DesiredAPI)apiConfig).getBackendBasepath(), apiConfig);
		apiConfig.setApiDefinition(apiSpecification);
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
			apiConfig.setRequestForAllOrgs(true);
		} else {
			// As the API-Manager internally handles the owning organization in the same way,
			// we have to add the Owning-Org as a desired org
			if(!apiConfig.getClientOrganizations().contains(apiConfig.getOrganization())) {
				apiConfig.getClientOrganizations().add(apiConfig.getOrganization());
			}
			// And validate each configured organization really exists in the API-Manager
			Iterator<Organization> it = apiConfig.getClientOrganizations().iterator();
			String invalidClientOrgs = null;
			List<Organization> foundOrgs = new ArrayList<>();
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
		initQuota(apiConfig.getSystemQuota());
		initQuota(apiConfig.getApplicationQuota());
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
				String markdownDescription = "";
				String newLine = "";
				for(String markdownFilename : apiConfig.getMarkdownLocal()) {
					if("ORIGINAL".equals(markdownFilename)) {
						markdownDescription += newLine + apiConfig.getApiDefinition().getDescription();
					} else {
						File markdownFile = new File(markdownFilename);
						if(!markdownFile.exists()) { // The file isn't provided with an absolute path, try to read it relative to the config file
							LOG.trace("Error reading markdown description file (absolute): '" + markdownFile.getCanonicalPath() + "'");
							String baseDir = this.apiConfigFile.getCanonicalFile().getParent();
							markdownFile = new File(baseDir + "/" + markdownFilename);
						}
						if(!markdownFile.exists()) {
							LOG.trace("Error reading markdown description file (relative): '" + markdownFile.getCanonicalPath() + "'");
							throw new AppException("Error reading markdown description file: " + markdownFilename, ErrorCode.CANT_READ_CONFIG_FILE);
						}
						LOG.debug("Reading local markdown description file: " + markdownFile.getPath());
						markdownDescription += newLine + new String(Files.readAllBytes(markdownFile.toPath()), StandardCharsets.UTF_8);
					}
					newLine = "\n";
				}
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

	private void validateMethodDescription(List<APIMethod> apiMethods) throws AppException {
		if(apiMethods == null)
			return;
		for (APIMethod apiMethod: apiMethods){
			String descriptionType = apiMethod.getDescriptionType();
			if(descriptionType == null ){
				throw new AppException("apiMethods descriptionType can't be null set default value as 'original'", ErrorCode.CANT_READ_CONFIG_FILE);
			}
			if(descriptionType.equals("original"))
				return;
			else if(descriptionType.equals("manual")) {
				if(apiMethod.getDescriptionManual()==null) {
					throw new AppException("apiMethods descriptionManual can't be null with descriptionType set to 'manual'", ErrorCode.CANT_READ_CONFIG_FILE);
				}
			} else if(descriptionType.equals("url")) {
				if(apiMethod.getDescriptionUrl()==null) {
					throw new AppException("apiMethods descriptionUrl can't be null with descriptionType set to 'url'", ErrorCode.CANT_READ_CONFIG_FILE);
				}
			} else if(descriptionType.equals("markdown")) {
				if (apiMethod.getDescriptionMarkdown() == null) {
					throw new AppException("apiMethods descriptionMarkdown can't be null with descriptionType set to 'markdown'", ErrorCode.CANT_READ_CONFIG_FILE);
				}
				if (!apiMethod.getDescriptionMarkdown().startsWith("${env.")) {
					throw new AppException("apiMethods descriptionMarkdown must start with an environment variable", ErrorCode.CANT_READ_CONFIG_FILE);
				}
			}else {
				throw new AppException("apiMethods Unknown descriptionType: '"+descriptionType+"'", ErrorCode.CANT_READ_CONFIG_FILE);
			}
		}
	}

	private void addDefaultCorsProfile(API apiConfig) throws AppException {
		if(apiConfig.getCorsProfiles()==null) {
			apiConfig.setCorsProfiles(new ArrayList<>());
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
			List<CaCert> completedCaCerts = new ArrayList<>();
			for(CaCert cert :apiConfig.getCaCerts()) {
				if(cert.getCertBlob()==null) {
					try(InputStream is = getInputStreamForCertFile(cert)) {
						JsonNode certInfo = APIManagerAdapter.getCertInfo(is, "", cert);
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
			LOG.debug("Can't read certifiate from file-location: " + file + ". Now trying to read it from the classpath.");
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

	private API validateInboundProfile(API importApi) throws AppException {
		if(importApi.getInboundProfiles()==null || importApi.getInboundProfiles().size()==0) {
			Map<String, InboundProfile> def = new HashMap<>();
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
		if(importApi.getSecurityProfiles()==null) importApi.setSecurityProfiles(new ArrayList<>());
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
			}
			// Check the referenced authentication profile exists
			if(!profile.getAuthenticationProfile().equals("_default")) {
				if(profile.getAuthenticationProfile()!=null && getAuthNProfile(importApi, profile.getAuthenticationProfile())==null) {
					throw new AppException("OutboundProfile is referencing a unknown AuthenticationProfile: '"+profile.getAuthenticationProfile()+"'", ErrorCode.REFERENCED_PROFILE_INVALID);
				}
			}
			// Check a routingPolicy is given, if routeType is policy
			if("policy".equals(profile.getRouteType()) && profile.getRoutePolicy()==null) {
				throw new AppException("Missing routingPolicy when routeType is set to policy", ErrorCode.CANT_READ_CONFIG_FILE);
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
		if(importApi.getAuthenticationProfiles()==null || importApi.getAuthenticationProfiles().size()==0) return;

		for(AuthenticationProfile authProfile : importApi.getAuthenticationProfiles()) {
			if(authProfile.getType().equals(AuthType.ssl)) {
				handleOutboundSSLAuthN(authProfile);
			} else if(authProfile.getType().equals(AuthType.oauth)) {
				handleOutboundOAuthAuthN(authProfile);
			}
		}

	}

	private void handleOutboundOAuthAuthN(AuthenticationProfile authnProfile) throws AppException {
		if(!authnProfile.getType().equals(AuthType.oauth)) return;
		String providerProfile = (String)authnProfile.getParameters().get("providerProfile");
		if(providerProfile!=null && providerProfile.startsWith("<key")) return;
		OAuthClientProfile clientProfile = APIManagerAdapter.getInstance().oauthClientAdapter.getOAuthClientProfile(providerProfile);
		if(clientProfile==null) {
			List<String> knownProfiles = new ArrayList<>();
			for(OAuthClientProfile profile : APIManagerAdapter.getInstance().oauthClientAdapter.getOAuthClientProfiles()) {
				knownProfiles.add(profile.getName());
			}
			throw new AppException("The OAuth provider profile is unkown: '"+providerProfile+"'. Known profiles: " + knownProfiles, ErrorCode.REFERENCED_PROFILE_INVALID);
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
			JsonNode fileData;
			try(InputStream inputStream = Files.newInputStream(clientCertFile.toPath())) {
				fileData = APIManagerAdapter.getFileData(IOUtils.toByteArray(inputStream), keystore, ContentType.create("application/x-pkcs12"));
			}
			CaCert cert = new CaCert();
			cert.setCertFile(clientCertFile.getName());
			cert.setInbound("false");
			cert.setOutbound("true");
			// This call is to validate the given password, keystore is valid
			APIManagerAdapter.getCertInfo(Files.newInputStream(clientCertFile.toPath()), password, cert);
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
		File file;
		try {
			file = new File(importApi.getImage().getFilename());
			if(!file.exists()) { // The image isn't provided with an absolute path, try to read it relative to the config file
				String baseDir = this.apiConfigFile.getCanonicalFile().getParent();
				file = new File(baseDir + "/" + importApi.getImage().getFilename());
			}
			importApi.getImage().setBaseFilename(file.getName());
			if(file.exists()) {
				LOG.info("Loading image from: '"+file.getCanonicalFile()+"'");
				try(InputStream is = Files.newInputStream(file.toPath())) {
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