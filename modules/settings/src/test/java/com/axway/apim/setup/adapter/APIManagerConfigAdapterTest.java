package com.axway.apim.setup.adapter;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIManagerConfigAdapterTest extends WiremockWrapper {

	@BeforeClass
	public void init() {
		initWiremock();
		TestIndicator.getInstance().setTestRunning(true);
	}

	@AfterClass
	public void stop() {
		close();
	}
	
	private static final String PACKAGE = "com/axway/apim/setup/adapter/";
	

	
	@Test
	public void testNonStagedConfig() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "apimanager-config.json").getFile();
		importParams.setConfig(configFileName);
		importParams.setHostname("localhost");
		APIManagerConfigAdapter adapter = new APIManagerConfigAdapter(importParams);
		APIManagerConfig managerConfig = adapter.getManagerConfig();
		Assert.assertEquals(managerConfig.getConfig().getPortalName(), "My API Manager");
		Assert.assertTrue(managerConfig.getConfig().getGlobalFaultHandlerPolicy().getId().startsWith("<key"));
		Assert.assertEquals(managerConfig.getConfig().getGlobalFaultHandlerPolicy().getName(), "Default Fault Handler");
	}
	
	@Test
	public void testValidStagedConfig() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "apimanager-config.json").getFile();
		importParams.setConfig(configFileName);
		importParams.setStage("test-stage");
		importParams.setHostname("localhost");
		APIManagerConfigAdapter adapter = new APIManagerConfigAdapter(importParams);
		APIManagerConfig managerConfig = adapter.getManagerConfig();
		Assert.assertEquals(managerConfig.getConfig().getPortalName(), "Axway API Manager Test-Stage");
	}
	
	@Test
	public void testInvalidStagedConfig() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "apimanager-config.json").getFile();
		importParams.setConfig(configFileName);
		importParams.setStage("invalid-stage");
		importParams.setHostname("localhost");
		APIManagerConfigAdapter adapter = new APIManagerConfigAdapter(importParams);
		APIManagerConfig managerConfig = adapter.getManagerConfig();
		Assert.assertEquals(managerConfig.getConfig().getPortalName(), "My API Manager");
	}
	
	@Test
	public void testManualStageConfig() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "apimanager-config.json").getFile();
		importParams.setConfig(configFileName);
		importParams.setHostname("localhost");
		importParams.setStageConfig("staged-apimanager-config.json");
		APIManagerConfigAdapter adapter = new APIManagerConfigAdapter(importParams);
		APIManagerConfig managerConfig = adapter.getManagerConfig();
		Assert.assertEquals(managerConfig.getConfig().getPortalHostname(), "staged-portal-hostname");
	}
}
