package com.axway.apim.swagger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.APIImportDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
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
 * @author cwiechmann
 * The APIContract reflects the given Meta-Information with 
 * the parameter: contract. 
 * This class will read the contract plus the optional set stage.
 * 
 */
public class APIImportConfig {
	
	private static Logger LOG = LoggerFactory.getLogger(APIImportConfig.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private String pathToSwagger;
	
	private String apiContract;
	
	private String stage;
	
	

	public APIImportConfig(String apiContract, String stage, String pathToSwagger) throws AppException {
		super();
		this.apiContract = apiContract;
		this.stage = stage;
		this.pathToSwagger = pathToSwagger;
	}
	
	public IAPIDefinition getImportAPIDefinition() throws AppException {
		IAPIDefinition stagedConfig;
		try {
			IAPIDefinition baseConfig = mapper.readValue(new File(apiContract), APIImportDefinition.class);
			ObjectReader updater = mapper.readerForUpdating(baseConfig);
			if(getStageContract(stage, apiContract)!=null) {
				LOG.info("Overriding configuration from: " + getStageContract(stage, apiContract));
				stagedConfig = updater.readValue(new File(getStageContract(stage, apiContract)));
			} else {
				stagedConfig = baseConfig;
			}
			addDefaultPassthroughSecurityProfile(stagedConfig);
			stagedConfig.setSwaggerDefinition(new APISwaggerDefinion(getSwaggerDefFromFile()));
			addImageContent(stagedConfig);
			validateCustomProperties(stagedConfig);
			validateDescription(stagedConfig);
			validateCorsConfig(stagedConfig);
			validateOutboundAuthN(stagedConfig);
			completeCaCerts(stagedConfig);
			addQuotaConfiguration(stagedConfig);
			handleAllOrganizations(stagedConfig);
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
		String baseDir = new File(this.pathToSwagger).getParent();
		File file = new File(baseDir + "/" + cert.getCertFile());
		InputStream is;
		if(file.exists()) { 
			try {
				is = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new AppException("Cant read given certificate file", ErrorCode.CANT_READ_CONFIG_FILE);
			}
		} else {
			// Try to read it from classpath
			is = APIManagerAdapter.class.getResourceAsStream(cert.getCertFile()); 
		}
		if(is==null) throw new AppException("Can't read certificate: "+cert.getCertFile()+" from file or classpath", ErrorCode.CANT_READ_CONFIG_FILE);
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
	
	private byte[] getSwaggerDefFromFile() throws AppException {
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
		File inputFile = new File(pathToSwagger);
		InputStream is = null;
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
		return is;
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
				String baseDir = new File(this.pathToSwagger).getParent();
				File file = new File(baseDir + "/" + importApi.getImage().getFilename());
				if(file.exists()) { 
					importApi.getImage().setImageContent(IOUtils.toByteArray(new FileInputStream(file)));
				} else {
					// Try to read it from classpath
					importApi.getImage().setImageContent(IOUtils.toByteArray(
							this.getClass().getResourceAsStream(importApi.getImage().getFilename())));
				}
			} catch (IOException e) {
				throw new AppException("Can't read image-file: "+importApi.getImage().getFilename()+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR, e);
			}
		}
		return importApi;
	}
}
