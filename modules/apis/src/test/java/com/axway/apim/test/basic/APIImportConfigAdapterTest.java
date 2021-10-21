package com.axway.apim.test.basic;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.apiimport.APIImportConfigAdapter;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.errorHandling.AppException;

public class APIImportConfigAdapterTest extends APIManagerMockBase {

	private static Logger LOG = LoggerFactory.getLogger(APIImportConfigAdapterTest.class);

	@BeforeClass
	private void initCommandParameters() throws AppException, IOException {
		setupMockData();
	}

	// Make sure, you don't have configured APIM_CLI_HOME when running this test
	@Test
	public void withoutStage() throws AppException, ParseException {
		// Create Environment properties without any stage (basically loads env.properties)
		EnvironmentProperties props = new EnvironmentProperties(null);
		APIImportParams params = new APIImportParams();
		params.setProperties(props);
		String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();

		APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false, null);
		DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
		Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomething");
	}

	// Make sure, you don't have configured APIM_CLI_HOME when running this test 
	@Test
	public void withStage() throws AppException, ParseException {
		// Providing a stage, it should load the env.variabletest.properties 
		EnvironmentProperties props = new EnvironmentProperties("variabletest");
		APIImportParams params = new APIImportParams();
		params.setProperties(props);
		String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();

		APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false, null);
		DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
		Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomethingElse");
	}

	// Make sure, you don't have configured APIM_CLI_HOME when running this test
	@Test
	public void withManualStageConfig() throws AppException, ParseException {
		String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
		APIImportParams params = new APIImportParams();
		params.setConfig(testConfig);
		params.setStageConfig("staged-minimal-config.json");

		APIImportConfigAdapter adapter = new APIImportConfigAdapter(params);
		DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
		Assert.assertEquals(apiConfig.getName(), "API-Name is different for this stage");
	}

	@Test
	public void usingOSEnvVariable() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();

			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false, null);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			String osArch = System.getProperty("os.arch");
			Assert.assertEquals(apiConfig.getState(), "notUsed "+osArch);
		} catch (Exception e) {
			LOG.error("Error running test: usingOSEnvVariable", e);
			throw e;
		}
	}

	@Test
	public void notDeclaredVariable() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();

			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false, null);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "${notDeclared}");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}

	@Test
	public void configFileWithSpaces() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api config with spaces.json").getFile();

			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", false, null);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "${notDeclared}");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}

	@Test
	public void stageConfigInSubDirectory() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();

			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, "testStageProd", "notRelavantForThis Test", false, null);
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "9.0.0");
			Assert.assertEquals(apiConfig.getName(), "API Config from testStageProd sub folder");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}

	@Test
	public void outboundOAuthValidConfig() throws AppException, ParseException {
		try {
			EnvironmentProperties props = new EnvironmentProperties(null);  
			props.put("myOAuthProfileName", "Sample OAuth Client Profile");
			APIImportParams params = new APIImportParams();
			params.setProperties(props);
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/outbound-oauth-config.json").getFile();

			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, "testStageProd", "petstore.json", false, null);
			adapter.getDesiredAPI();
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertEquals(apiConfig.getVersion(), "kk1");
			Assert.assertEquals(apiConfig.getName(), "My OAuth API");
		} catch (Exception e) {
			LOG.error("Error running test: notDeclaredVariable", e);
			throw e;
		}
	}

	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "The OAuth provider profile is unkown: 'Invalid profile name'")
	public void outboundOAuthInValidConfig() throws AppException, ParseException {
		EnvironmentProperties props = new EnvironmentProperties(null);
		props.put("myOAuthProfileName", "Invalid profile name");
		APIImportParams params = new APIImportParams();
		params.setProperties(props);
		String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/outbound-oauth-config.json").getFile();

		APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "petstore.json", false, null);
		adapter.getDesiredAPI();
		DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
		Assert.assertEquals(apiConfig.getVersion(), "kk1");
		Assert.assertEquals(apiConfig.getName(), "My OAuth API");
	}

	@Test
	public void emptyVHostTest() throws AppException, ParseException {
		try {
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/empty-vhost-api-config.json").getFile();

			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "petstore.json", false, null);
			adapter.getDesiredAPI();
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Assert.assertNull(apiConfig.getVhost(), "Empty VHost should be considered as not set (null), as an empty VHost is logically not possible to have.");
		} catch (Exception e) {
			throw e;
		}
	}

	@Test
	public void outboundProfileWithDefaultAuthOnlyTest() throws AppException, ParseException {
		try {
			String testConfig = this.getClass().getResource("/com/axway/apim/test/files/methodLevel/method-level-outboundprofile-default-authn-only.json").getFile();

			APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "../basic/petstore.json", false, null);
			adapter.getDesiredAPI();
			DesiredAPI apiConfig = (DesiredAPI)adapter.getApiConfig();
			Map<String, OutboundProfile> outboundProfiles = apiConfig.getOutboundProfiles();
			Assert.assertEquals(outboundProfiles.size(), 2, "Two outbound profiles are expected.");
			OutboundProfile defaultProfile = outboundProfiles.get("_default");
			OutboundProfile getOrderByIdProfile = outboundProfiles.get("getOrderById");
			Assert.assertEquals(defaultProfile.getAuthenticationProfile(), "_default", "Authentication profile should be the default.");
			Assert.assertEquals(getOrderByIdProfile.getAuthenticationProfile(), "_default", "Authentication profile should be the default.");
		} catch (Exception e) {
			throw e;
		}
	}

	@Test(priority = 100, expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Missing required custom property: 'customProperty4'")
	public void testMissingMandatoryCustomProperty() throws ParseException, IOException, InterruptedException {
		String customPropertiesConfig = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "customProperties/customPropertiesConfig.json"));
		APIManagerAdapter.getInstance().customPropertiesAdapter.setAPIManagerTestResponse(customPropertiesConfig);

		EnvironmentProperties props = new EnvironmentProperties(null);
		props.put("orgNumber", "1");
		props.put("apiPath", "/api/with/custom/props");
		props.put("status", "unpublished");
		props.put("customProperty1", "public");
		props.put("customProperty3", "true");

		Thread.sleep(2000);
		APIImportParams params = new APIImportParams();
		params.setProperties(props);

		String testConfig = this.getClass().getResource("/com/axway/apim/test/files/customProperties/1_custom-properties-config.json").getFile();

		APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "../basic/petstore.json", false, null);
		adapter.getDesiredAPI(); // Should fail, as a mandatory customProperty is missing
	}
}
