package com.axway.apim.setup.adapter;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class JSONAPIManagerConfigAdapterTest extends APIManagerMockBase {
	
	private static final String PACKAGE = "com/axway/apim/setup/adapter/";
	
	@BeforeClass
	public void setupTestIndicator() throws AppException, IOException {
		setupMockData();
	}
	
	@Test
	public void testNonStagedConfig() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "apimanager-config.json").getFile();
		importParams.setConfig(configFileName);
		JSONAPIManagerConfigAdapter adapter = new JSONAPIManagerConfigAdapter(importParams);
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
		JSONAPIManagerConfigAdapter adapter = new JSONAPIManagerConfigAdapter(importParams);
		APIManagerConfig managerConfig = adapter.getManagerConfig();
		
		Assert.assertEquals(managerConfig.getConfig().getPortalName(), "Axway API Manager Test-Stage");
	}
	
	@Test
	public void testInvalidStagedConfig() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "apimanager-config.json").getFile();
		importParams.setConfig(configFileName);
		importParams.setStage("invalid-stage");
		JSONAPIManagerConfigAdapter adapter = new JSONAPIManagerConfigAdapter(importParams);
		APIManagerConfig managerConfig = adapter.getManagerConfig();
		
		Assert.assertEquals(managerConfig.getConfig().getPortalName(), "My API Manager");
	}
}
