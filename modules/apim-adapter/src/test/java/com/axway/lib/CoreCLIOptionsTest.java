package com.axway.lib;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;

public class CoreCLIOptionsTest {
	@Test
	public void testCoreParameters() throws AppException {
		CoreParameters params = new CoreParameters();
		String[] args = {"-h", "api-env", "-u", "apiadmin", "-p", "changeme", "-port", "8888", "-apimCLIHome", "My-home-is-my-castle", "-clearCache", "ALL", "-returnCodeMapping", "10:0", "-rollback", "false", "-force", "-ignoreCache", "-ignoreAdminAccount"};
		CoreCLIOptions options = new CoreCLIOptions(args) {
			
			@Override
			protected String getAppName() {
				return "TEST";
			}
		};
		options.addCoreParameters(params);
		Assert.assertEquals(params.getHostname(), "api-env");
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getPort(), 8888);
		Assert.assertEquals(params.getApimCLIHome(), "My-home-is-my-castle");
		Assert.assertEquals(params.getClearCache(), "ALL");
		Assert.assertEquals(params.getReturnCodeMapping(), "10:0");
		Assert.assertTrue(params.isForce());
		Assert.assertFalse(params.isRollback());
		Assert.assertTrue(params.isIgnoreCache());
		Assert.assertTrue(params.isIgnoreAdminAccount());
	}
	
	@Test
	public void testOldForceParameter() throws AppException {
		CoreParameters params = new CoreParameters();
		String[] args = {"-s", "api-env", "-f", "true"};
		CoreCLIOptions options = new CoreCLIOptions(args) {
			
			@Override
			protected String getAppName() {
				return "TEST";
			}
		};
		options.addCoreParameters(params);
		Assert.assertTrue(params.isForce());
	}
	
	@Test
	public void testStagePropertyFiles() throws ParseException, AppException {
		CoreParameters params = new CoreParameters();
		String[] args = {"-s", "yetAnotherStage"};
		CoreCLIOptions options = new CoreCLIOptions(args) {
			
			@Override
			protected String getAppName() {
				return "TEST";
			}
		};
		options.addCoreParameters(params);
		Assert.assertEquals(params.getAdminUsername(), "yetanotherUser");
		Assert.assertEquals(params.getAdminPassword(), "yetanotherPassword");
		Assert.assertEquals(params.getProperties().get("yetAnotherProperty"), "HellImHere"); // from env.yetAnotherStage.properties
		Assert.assertEquals(params.getProperties().get("myTestVariable"), "resolvedToSomething"); // from env.properties
	}
	
	@Test
	public void testPropertyFileWithoutStage() throws ParseException, AppException {
		CoreParameters params = new CoreParameters();
		String[] args = {};
		CoreCLIOptions options = new CoreCLIOptions(args) {
			
			@Override
			protected String getAppName() {
				return "TEST";
			}
		};
		options.addCoreParameters(params);
		Assert.assertEquals(params.getHostname(), "localhost");
		Assert.assertEquals(params.getProperties().get("myTestVariable"), "resolvedToSomething"); // from env.properties
	}
}
