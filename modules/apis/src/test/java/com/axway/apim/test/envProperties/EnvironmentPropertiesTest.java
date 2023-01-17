package com.axway.apim.test.envProperties;

import com.axway.apim.lib.EnvironmentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class EnvironmentPropertiesTest {

	private static final Logger LOG = LoggerFactory.getLogger(EnvironmentPropertiesTest.class);
	private String apimCliHome;
	@BeforeClass
	private void initCommandParameters() {
		apimCliHome = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + "apimcli";
	}


	@Test
	public void testNoStage() {
		EnvironmentProperties properties = new EnvironmentProperties("NOT_SET", apimCliHome);
		Assert.assertEquals(properties.containsKey("doesnExists"), false);
		Assert.assertEquals(properties.containsKey("username"), true);
	}

	@Test
	public void testStage() {
		EnvironmentProperties properties = new EnvironmentProperties("anyOtherStage", apimCliHome);
		Assert.assertEquals(properties.containsKey("thisKeyExists"), true);
	}
	
	@Test
	public void testNoStageFromConfDir() throws URISyntaxException {
		// A given path should be used to load the Environment-Config file from
		String path = EnvironmentPropertiesTest.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		path += "envPropertiesTest/swaggerPromoteHome";

		EnvironmentProperties properties = new EnvironmentProperties(null, path);

		Assert.assertEquals(properties.containsKey("thisKeyExists"), true);
		Assert.assertEquals(properties.get("thisKeyExists"), "keyFromSwaggerPromoteHome");
	}

	@Test
	public void testStageFromConfDir() throws URISyntaxException {
		// A given path should be used to load the Environment-Config file from
		String path = EnvironmentPropertiesTest.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		path += "envPropertiesTest/swaggerPromoteHome";

		EnvironmentProperties properties = new EnvironmentProperties("fromSwaggerPromoteHome", path);

		Assert.assertEquals(properties.containsKey("thisKeyExists"), true);
		Assert.assertEquals(properties.get("thisKeyExists"), "stageKeyFromSwaggerPromoteHome");
	}
	
	@Test
	public void testEnvironementWithOSEnvVariables() throws IOException {
		// For this test to run, the system must provide the environment properties CI & JAVA_HOME
		EnvironmentProperties properties = new EnvironmentProperties("NOT_SET");
		System.out.println(properties);
		Assert.assertNotEquals(properties.get("variableFromOSEnvironmentVariable"), "${JAVA_HOME}");
		String javaHome = System.getenv("JAVA_HOME");
		if(javaHome == null){
			LOG.warn("JAVA_HOME is not set and test is 'testEnvironementWithOSEnvVariables' is ignored");
			return;
		}
		String CI = System.getenv("CI");
		if(CI == null){
			LOG.warn("CI is not set and test is 'variablePartiallyFromOSEnvironmentVariable' is ignored");
			return;
		}
		Assert.assertEquals(properties.get("variablePartiallyFromOSEnvironmentVariable"), "Fixed value and true some dynamic parts");		
	}
}
