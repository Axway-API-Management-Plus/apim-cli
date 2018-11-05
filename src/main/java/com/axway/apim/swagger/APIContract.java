package com.axway.apim.swagger;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author cwiechmann
 * The APIContract reflects the given Meta-Information with 
 * the parameter: contract. 
 * This class will read the contract plus the optional set stage.
 * 
 */
public class APIContract {
	
	private static Logger LOG = LoggerFactory.getLogger(APIContract.class);
	/**
	 * Required meta information
	 */
	private String apiContract;
	
	/**
	 * Any provided stage (prod, pre-prod) 
	 * Is used to lookup additional contract-files using the 
	 * pattern: 
	 * <code>contract-file.json --> contract-file.<stage>.json</code> 
	 */
	private String stage = null;
	
	private JsonNode configuration;
	
	private JsonNode stageConfiguration;

	public APIContract(String apiContract, String stage) {
		super();
		this.apiContract = apiContract;
		this.stage = stage;
		init();
	}
	
	private void init() {
		try {
			configuration = getConfiguration(this.apiContract);
			if(configuration==null) {
				throw new RuntimeException("Unable to read contract from: " + this.apiContract);
			}
			if(this.stage!=null) {
				String stageFile = getStageContract();
				LOG.info("Overriding configuration from: " + stageFile);
				stageConfiguration = getConfiguration(stageFile);
			}
		} catch (IOException e) {
			LOG.error("Error initializing API-Contract.");
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * @param property - JSON-Path to the requested property
	 * @return the value of the property stage-depending
	 */
	public JsonNode getProperty(String property) {
		JsonNode returnValue;
		if(stageConfiguration!=null && stageConfiguration.get(property)!=null) {
			returnValue = stageConfiguration.get(property);
		} else {
			returnValue = configuration.at(property);
		}
		return returnValue;
	}
	
	private String getStageContract() {
		if(this.stage!=null) {
			return this.apiContract.substring(0, this.apiContract.lastIndexOf(".")+1) + this.stage + this.apiContract.substring(this.apiContract.lastIndexOf("."));
		}
		LOG.error("No stage provided");
		return "";
	}
	
	/**
	 * To make testing easier we allow reading test-files from classpath as well
	 */
	private JsonNode getConfiguration(String pathToResource) throws JsonProcessingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonConfig;
		File inputFile = new File(pathToResource);
		if(inputFile.exists()) { 
			jsonConfig = objectMapper.readTree(new File(pathToResource));
			//jsonConfig = IOUtils.read(input, buffer).readFileToString(new File(pathToResource));
		} else {
			jsonConfig = objectMapper.readTree(this.getClass().getResourceAsStream(pathToResource));
		}
		if(jsonConfig == null) {
			throw new IOException("Unable to read configuration from: " + pathToResource);
		}
		return jsonConfig;
	}
}
