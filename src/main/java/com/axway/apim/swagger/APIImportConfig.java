package com.axway.apim.swagger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.APIImportDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.axway.apim.swagger.api.properties.inboundprofiles.InboundProfile;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityDevice;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

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
	
	private JsonNode configuration;
	
	private JsonNode stageConfiguration;
	
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
		//mapper.setDefaultMergeable(true);
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
			return stagedConfig;
		} catch (Exception e) {
			if(e.getCause() instanceof AppException) {
				throw (AppException)e.getCause();
			}
			throw new AppException("Cant parse JSON-Config file(s)", ErrorCode.CANT_READ_CONFIG_FILE, e);
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
	 * @throws AppException 
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
	
	/**
	 * @param property - JSON-Path to the requested property
	 * @return the value of the property stage-depending
	 */
	public JsonNode getProperty(String property) {
		JsonNode returnValue;
		if(stageConfiguration!=null && stageConfiguration.at(property)!=null) {
			returnValue = stageConfiguration.at(property);
		} else {
			returnValue = configuration.at(property);
		}
		return returnValue;
	}
	
	private String getStageContract(String stage, String apiContract) {
		File stageFile = new File(stage);
		if(stageFile.exists()) { // This is to support testing with dynamic created files!
			return stageFile.getAbsolutePath();
		}
		if(stage!=null && !stage.equals("NOT_SET")) {
			return apiContract.substring(0, apiContract.lastIndexOf(".")+1) + stage + apiContract.substring(apiContract.lastIndexOf("."));
		}
		LOG.error("No stage provided");
		return null;
	}
	
	/**
	 * To make testing easier we allow reading test-files from classpath as well
	 * @throws AppException 
	 */
	private JsonNode getConfiguration(String pathToResource) throws JsonProcessingException, IOException, AppException {
		JsonNode jsonConfig;
		File inputFile = new File(pathToResource);
		if(inputFile.exists()) { 
			jsonConfig = mapper.readTree(new File(pathToResource));
			//jsonConfig = IOUtils.read(input, buffer).readFileToString(new File(pathToResource));
		} else {
			jsonConfig = mapper.readTree(this.getClass().getResourceAsStream(pathToResource));
		}
		if(jsonConfig == null) {
			throw new AppException("Unable to read config file from, as it is null", ErrorCode.CANT_READ_CONFIG_FILE);
		}
		return jsonConfig;
	}
	
	private JsonNode getJsonContent(String pathToResource) throws JsonProcessingException, IOException, AppException {
		JsonNode jsonConfig;
		File inputFile = new File(pathToResource);
		if(inputFile.exists()) { 
			jsonConfig = mapper.readTree(new File(pathToResource));
			//jsonConfig = IOUtils.read(input, buffer).readFileToString(new File(pathToResource));
		} else {
			jsonConfig = mapper.readTree(this.getClass().getResourceAsStream(pathToResource));
		}
		if(jsonConfig == null) {
			throw new AppException("Unable to read config file from, as it is null", ErrorCode.CANT_READ_CONFIG_FILE);
		}
		return jsonConfig;
	}
	
	private IAPIDefinition addDefaultPassthroughProfile(IAPIDefinition importApi) {
		if(importApi.getInboundProfiles()==null || importApi.getInboundProfiles().size()==0) {
			InboundProfile passthroughProfile = new InboundProfile();
			passthroughProfile.setCorsProfile("_default");
			passthroughProfile.setSecurityProfile("_default");
			importApi.setInboundProfiles(new LinkedHashMap<String, InboundProfile>());
			importApi.getInboundProfiles().put("_default", passthroughProfile);
		}
		return importApi;
	}
	
	private IAPIDefinition addDefaultPassthroughSecurityProfile(IAPIDefinition importApi) {
		if(importApi.getSecurityProfiles()==null || importApi.getSecurityProfiles().size()==0) {
			SecurityProfile passthroughProfile = new SecurityProfile();
			passthroughProfile.setName("_default");
			passthroughProfile.setIsDefault("true");
			SecurityDevice passthroughDevice = new SecurityDevice();
			passthroughDevice.setName("Pass Through");
			passthroughDevice.setType("passThrough");
			passthroughDevice.getProperties().put("subjectIdFieldName", "Pass Through");
			passthroughDevice.getProperties().put("removeCredentialsOnSuccess", "true");
			passthroughProfile.getDevices().add(passthroughDevice);
			
			importApi.setSecurityProfiles(new ArrayList<SecurityProfile>());
			importApi.getSecurityProfiles().add(passthroughProfile);
		}
		return importApi;
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
				throw new AppException("Can't read image-file from file", ErrorCode.UNXPECTED_ERROR, e);
			}
		}
		return importApi;
	}
}
