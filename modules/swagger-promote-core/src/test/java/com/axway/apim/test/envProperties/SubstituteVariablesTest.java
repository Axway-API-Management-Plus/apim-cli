package com.axway.apim.test.envProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.APIImportConfigAdapter;
import com.axway.apim.swagger.api.properties.APIDefintion;
import com.axway.apim.swagger.api.state.DesiredAPI;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SubstituteVariablesTest {
	
	@BeforeClass
	private void initCommandParameters() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("replaceHostInSwagger", "true");
		new CommandParameters(params);
	}
	
	@Test
	public void validateSystemOSAreSubstituted() throws AppException, IOException {
		Properties props = System.getProperties();
		props.setProperty("OS_PROPERTY_ONLY", "apiNameFromOSProperty");
		
		String configFile = "com/axway/apim/test/files/envProperties/1_config-with-os-variable.json";
		String pathToConfigFile = this.getClass().getClassLoader().getResource(configFile).getFile();
		String apiDefinition = "/api_definition_1/petstore.json";
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(pathToConfigFile, null, apiDefinition, false);
		
		IAPI testAPI = importConfig.getApiConfig();
		
		Assert.assertEquals(testAPI.getName(), "apiNameFromOSProperty");
	}
	
	@Test
	public void validateBaseEnvReplacedOSAttribute() throws AppException, IOException {
		Properties props = System.getProperties();
		props.setProperty("OS_AND_MAIN_ENV_PROPERTY", "valueFromOS");
		
		EnvironmentProperties envProps = new EnvironmentProperties(null);
		CommandParameters.getInstance().setEnvProperties(envProps);
		
		String configFile = "com/axway/apim/test/files/envProperties/1_config-with-os-variable.json";
		String pathToConfigFile = this.getClass().getClassLoader().getResource(configFile).getFile();
		String apiDefinition = "/api_definition_1/petstore.json";
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(pathToConfigFile, null, apiDefinition, false);
		
		IAPI testAPI = importConfig.getApiConfig();
		
		Assert.assertEquals(testAPI.getPath(), "valueFromMainEnv");
	}
	
	@Test
	public void validateStageEnvOveridesAll() throws AppException, IOException {
		Properties props = System.getProperties();
		props.setProperty("OS_MAIN_AND_STAGE_ENV_PROPERTY", "valueFromOS");
		
		EnvironmentProperties envProps = new EnvironmentProperties("anyOtherStage");
		CommandParameters.getInstance().setEnvProperties(envProps);
		
		String configFile = "com/axway/apim/test/files/envProperties/1_config-with-os-variable.json";
		String pathToConfigFile = this.getClass().getClassLoader().getResource(configFile).getFile();
		String apiDefinition = "/api_definition_1/petstore.json";
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(pathToConfigFile, null, apiDefinition, false);
		
		IAPI testAPI = importConfig.getApiConfig();
		
		Assert.assertEquals(testAPI.getOrganization(), "valueFromAnyOtherStageEnv");
	}

}
