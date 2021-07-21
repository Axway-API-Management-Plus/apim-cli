package com.axway.apim.test.envProperties;

import java.io.IOException;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.api.API;
import com.axway.apim.apiimport.APIImportConfigAdapter;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;

public class SubstituteVariablesTest extends APIManagerMockBase {
	
	private static String OS = null;

	@BeforeClass
	private void initCommandParameters() throws AppException, IOException {
		setupMockData();
		TestIndicator.getInstance().setTestRunning(true);
		APIImportParams params = new APIImportParams();
		params.setReplaceHostInSwagger(true);
	}
	
	@Test
	public void validateSystemOSAreSubstituted() throws AppException, IOException {
		String configFile = "com/axway/apim/test/files/envProperties/1_config-with-os-variable.json";
		String pathToConfigFile = this.getClass().getClassLoader().getResource(configFile).getFile();
		String apiDefinition = "/api_definition_1/petstore.json";
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(pathToConfigFile, null, apiDefinition, false);
		
		API testAPI = importConfig.getApiConfig();
		if(System.getenv("TRAVIS")!=null && System.getenv("TRAVIS").equals("true")) {
			// At Travis an environment-variable USER exists which should have been replaced
			Assert.assertNotEquals(testAPI.getName(), "${USER}");			
		} else if(System.getenv("GITHUB_ACTIONS")!=null && System.getenv("GITHUB_ACTIONS").equals("true")) {
			// When running at GitHub actions
			Assert.assertNotEquals(testAPI.getVhost(), "${GITHUB_ACTION}");
		} else {
			// On Windows use USERNAME in the version
			if(isWindows()){
				Assert.assertNotEquals(testAPI.getVersion(), "${USERNAME}");
			} else { // MacOS X and Linux use USER in the name
				Assert.assertNotEquals(testAPI.getName(), "${USER}");
			}
		}
	}
	
	@Test
	public void validateBaseEnvReplacedOSAttribute() throws AppException, IOException {
		Properties props = System.getProperties();
		props.setProperty("OS_AND_MAIN_ENV_PROPERTY", "valueFromOS");
		
		EnvironmentProperties envProps = new EnvironmentProperties(null);
		CoreParameters.getInstance().setProperties(envProps);
		
		String configFile = "com/axway/apim/test/files/envProperties/1_config-with-os-variable.json";
		String pathToConfigFile = this.getClass().getClassLoader().getResource(configFile).getFile();
		String apiDefinition = "/api_definition_1/petstore.json";
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(pathToConfigFile, null, apiDefinition, false);
		
		API testAPI = importConfig.getApiConfig();
		
		Assert.assertEquals(testAPI.getPath(), "valueFromMainEnv");
	}
	
	@Test
	public void validateStageEnvOveridesAll() throws AppException, IOException {
		Properties props = System.getProperties();
		props.setProperty("OS_MAIN_AND_STAGE_ENV_PROPERTY", "valueFromOS");
		
		EnvironmentProperties envProps = new EnvironmentProperties("anyOtherStage");
		CoreParameters.getInstance().setProperties(envProps);
		
		String configFile = "com/axway/apim/test/files/envProperties/1_config-with-os-variable.json";
		String pathToConfigFile = this.getClass().getClassLoader().getResource(configFile).getFile();
		String apiDefinition = "/api_definition_1/petstore.json";
		
		APIImportConfigAdapter importConfig = new APIImportConfigAdapter(pathToConfigFile, null, apiDefinition, false);
		
		API testAPI = importConfig.getApiConfig();
		
		Assert.assertEquals(testAPI.getDescriptionManual(), "valueFromAnyOtherStageEnv");
	}

	private static String getOsName(){
    	if(OS == null) { 
			OS = System.getProperty("os.name"); 
		}
      	return OS;
	}

	public static boolean isWindows(){
      	return getOsName().startsWith("Windows");
    }
}
