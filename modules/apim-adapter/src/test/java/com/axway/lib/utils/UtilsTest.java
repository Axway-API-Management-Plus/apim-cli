package com.axway.lib.utils;

import java.io.File;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.utils.Utils;

public class UtilsTest {
	
	private static Logger LOG = LoggerFactory.getLogger(UtilsTest.class);
	
	String testConfig = this.getClass().getResource("/stageConfigTest/test-api-config.json").getFile();
	String stageConfig = this.getClass().getResource("/stageConfigTest/my-stage-test-api-config.json").getFile();
	
	@Test
	public void testGetStageConfigNoStage() {
		File stageConfigFile = Utils.getStageConfig(null, null, null);
		Assert.assertNull(stageConfigFile);
	}
	
	@Test
	public void testGetStageConfigSomeStage() {
		File stageConfigFile = Utils.getStageConfig("someStage", null, new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "test-api-config.someStage.json");
	}
	
	@Test
	public void testGetStageConfigUnknownStage() {
		File stageConfigFile = Utils.getStageConfig("unknownStage", null, new File(testConfig));
		Assert.assertNull(stageConfigFile);
	}
	
	@Test
	public void testGetStageConfigFile() {
		File stageConfigFile = Utils.getStageConfig(null, stageConfig, new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testGetStageConfigFileWithStageAndConfig() {
		File stageConfigFile = Utils.getStageConfig("prod", stageConfig, new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testGetStageConfigFileWithStageAndRelativeConfig() {
		File stageConfigFile = Utils.getStageConfig(null, "my-stage-test-api-config.json", new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testMissingMandatoryCustomProperty() {
		File stageConfigFile = Utils.getStageConfig(null, "my-stage-test-api-config.json", new File(testConfig));
		Assert.assertEquals(stageConfigFile.getName(), "my-stage-test-api-config.json");
	}
	
	@Test
	public void testLocateInstallFolder() {
		File installFolder = Utils.getInstallFolder();
		LOG.info("Validate install folder: "+installFolder+" exists");
		Assert.assertNotNull(installFolder);
		Assert.assertTrue(installFolder.exists());
	}


	@Test
	public void testHandleOpenAPIServerUrlBackendBasePathWithSlash()  throws MalformedURLException{
		String serverUrl = "https://petstore3.swagger.io/api/v3";
		String backendBasePath = "http://backend/";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}

	@Test
	public void testHandleOpenAPIServerUrlBackendBasePathWithoutSlash()  throws MalformedURLException{
		String serverUrl = "https://petstore3.swagger.io/api/v3";
		String backendBasePath = "http://backend";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}
	@Test
	public void testHandleOpenAPIServerUriBackendBasePathWithSlash()  throws MalformedURLException{
		String serverUrl = "https://petstore3.swagger.io/api/v3";
		String backendBasePath = "http://backend/";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}
	@Test
	public void testHandleOpenAPIServerUrlBackendBasePathWithPath()  throws MalformedURLException{
		String serverUrl = "https://petstore3.swagger.io/api/v3";
		String backendBasePath = "http://backend/api";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/api/v3");
	}

	@Test
	public void testHandleOpenAPIServerUriBackendBasePathWithoutSlash()  throws MalformedURLException{
		String serverUrl = "/api/v3";
		String backendBasePath = "http://backend";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}
	@Test
	public void testHandleOpenAPIServerUrlWithoutSlashAndUri()  throws MalformedURLException{
		String serverUrl = "/api/v3";
		String backendBasePath = "http://backend";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/v3");
	}

	@Test
	public void testHandleOpenAPIServerUriBackendBasePathWithPath()  throws MalformedURLException{
		String serverUrl = "/api/v3";
		String backendBasePath = "http://backend/api";
		String	result = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
		Assert.assertEquals(result, "http://backend/api/api/v3");
	}


}
