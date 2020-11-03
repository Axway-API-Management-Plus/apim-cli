package com.axway.apim.adapter.customProperties;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import com.axway.apim.api.model.CustomProperties;
import com.axway.apim.lib.errorHandling.AppException;

public class APIMgr762CustomPropertiesAdapterTest {
	
	private static String testPackage = "com/axway/apim/adapter/customProperties/";
	
	@Test
	public void testAppConfig() throws IOException, AppException {
		String appConfig = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "testAppConfig.config"));
		
		APIManager762CustomPropertiesAdapter adapter = new APIManager762CustomPropertiesAdapter();
		adapter.setAPIManagerTestResponse(appConfig);
		CustomProperties customProperties = adapter.getCustomProperties();
		
		Assert.assertEquals(customProperties.getApi().size(), 3);
		Assert.assertEquals(customProperties.getApi().get("customProperty2").getLabel(), "Custom Property #2");
		Assert.assertEquals(customProperties.getApi().get("customProperty2").getOptions().size(), 3);
		Assert.assertEquals(customProperties.getApplication().size(), 3);
		Assert.assertEquals(customProperties.getApplication().get("appCustomProperty2").getLabel(), "App custom Property #2");
		Assert.assertEquals(customProperties.getApplication().get("appCustomProperty2").getType(), "select");
	}
	
	@Test
	public void testappKoIssue122() throws IOException, AppException {
		String appConfig = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "app_ko_issue_122.config"));
		
		APIManager762CustomPropertiesAdapter adapter = new APIManager762CustomPropertiesAdapter();
		adapter.setAPIManagerTestResponse(appConfig);
		CustomProperties customProperties = adapter.getCustomProperties();
		
		Assert.assertNotNull(customProperties);
	}
	
	@Test
	public void testApp1Config() throws IOException, AppException {
		String appConfig = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "app-1.config"));
		
		APIManager762CustomPropertiesAdapter adapter = new APIManager762CustomPropertiesAdapter();
		adapter.setAPIManagerTestResponse(appConfig);
		CustomProperties customProperties = adapter.getCustomProperties();
		
		Assert.assertNotNull(customProperties);
	}
	
	@Test
	public void testApp2Issue79Config() throws IOException, AppException {
		String appConfig = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "app-2-issue-79.config"));
		
		APIManager762CustomPropertiesAdapter adapter = new APIManager762CustomPropertiesAdapter();
		adapter.setAPIManagerTestResponse(appConfig);
		CustomProperties customProperties = adapter.getCustomProperties();
		
		Assert.assertNotNull(customProperties);
	}
}
