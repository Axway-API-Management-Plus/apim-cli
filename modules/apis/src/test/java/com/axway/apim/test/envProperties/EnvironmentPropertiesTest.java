package com.axway.apim.test.envProperties;

import java.io.IOException;
import java.net.URISyntaxException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.errorHandling.AppException;

public class EnvironmentPropertiesTest {

	@Test
	public void testNoStage() throws AppException, IOException {
		EnvironmentProperties properties = new EnvironmentProperties("NOT_SET");

		Assert.assertEquals(properties.containsKey("doesnExists"), false);
		Assert.assertEquals(properties.containsKey("admin_username"), true);

		Assert.assertEquals(properties.get("admin_username"), "apiadmin");
		Assert.assertEquals(properties.get("admin_password"), "changeme");
	}

	@Test
	public void testStage() throws AppException, IOException {
		EnvironmentProperties properties = new EnvironmentProperties("anyOtherStage");

		Assert.assertEquals(properties.containsKey("thisKeyExists"), true);

		Assert.assertEquals(properties.get("admin_username"), "anyOtherUser");
		Assert.assertEquals(properties.get("admin_password"), "anyOtherPassword");
	}
	
	@Test
	public void testNoStageFromConfDir() throws AppException, IOException, URISyntaxException {
		// A given path should be used to load the Environent-Config file from
		String path = EnvironmentPropertiesTest.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		path += "envPropertiesTest/swaggerPromoteHome";

		EnvironmentProperties properties = new EnvironmentProperties(null, path);

		Assert.assertEquals(properties.containsKey("thisKeyExists"), true);
		Assert.assertEquals(properties.get("thisKeyExists"), "keyFromSwaggerPromoteHome");
		
		
		Assert.assertEquals(properties.get("admin_username"), "userFromSwaggerPromoteHome");
		Assert.assertEquals(properties.get("admin_password"), "passwordFromSwaggerPromoteHome");
	}

	@Test
	public void testStageFromConfDir() throws AppException, IOException, URISyntaxException {
		// A given path should be used to load the Environment-Config file from
		String path = EnvironmentPropertiesTest.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		path += "envPropertiesTest/swaggerPromoteHome";

		EnvironmentProperties properties = new EnvironmentProperties("fromSwaggerPromoteHome", path);

		Assert.assertEquals(properties.containsKey("thisKeyExists"), true);
		Assert.assertEquals(properties.get("thisKeyExists"), "stageKeyFromSwaggerPromoteHome");
		
		
		Assert.assertEquals(properties.get("admin_username"), "stageUserFromSwaggerPromoteHome");
		Assert.assertEquals(properties.get("admin_password"), "stageUasswordFromSwaggerPromoteHome");
	}
	
	@Test
	public void testEnvironementWithOSEnvVariables() throws AppException, IOException {
		// For this test to run, the system must provide the environment properties CI & JAVA_HOME
		EnvironmentProperties properties = new EnvironmentProperties("NOT_SET");
		
		Assert.assertNotEquals(properties.get("variableFromOSEnvironmentVariable"), "${JAVA_HOME}");
		Assert.assertEquals(properties.get("variablePartiallyFromOSEnvironmentVariable"), "Fixed value and true some dynamic parts");		
	}
}
