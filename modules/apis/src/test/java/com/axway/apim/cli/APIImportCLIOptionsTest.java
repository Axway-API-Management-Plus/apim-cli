package com.axway.apim.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.apiimport.lib.APIImportCLIOptions;
import com.axway.apim.apiimport.lib.APIImportParams;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;

public class APIImportCLIOptionsTest {
	@Test
	public void testWithoutAdminUser() throws ParseException, AppException {
		String[] args = {"-u", "myUser", "-p", "myPassword", "-port", "8175", "-c", "myConfig.json"};
		APIImportCLIOptions options = new APIImportCLIOptions(args);
		APIImportParams params = options.getAPIImportParams();
		Assert.assertEquals(params.getUsername(), "myUser");        // Taken from cmd directly
		Assert.assertEquals(params.getPassword(), "myPassword");    // Taken from cmd directly
		Assert.assertEquals(params.getAdminUsername(), "apiadmin"); // Loaded from env.properties
		Assert.assertEquals(params.getAdminPassword(), "changeme"); // Loaded from env.properties
		Assert.assertEquals(params.getConfig(), "myConfig.json");
	}
	
	@Test
	public void testUserDetailsFromStage() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myConfig.json"};
		APIImportCLIOptions options = new APIImportCLIOptions(args);
		APIImportParams params = options.getAPIImportParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
	}
	
	@Test
	public void testAPIImportParameter() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myConfig.json", "-clientOrgsMode", "replace", "-clientAppsMode", "replace", "-quotaMode", "replace", "-detailsExportFile", "myExportFile.txt"};
		APIImportCLIOptions options = new APIImportCLIOptions(args);
		APIImportParams params = options.getAPIImportParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		Assert.assertEquals(params.getClientOrgsMode(), "replace");
		Assert.assertEquals(params.getClientAppsMode(), "replace");
		Assert.assertEquals(params.getQuotaMode(), "replace");
		Assert.assertEquals(params.getDetailsExportFile(), "myExportFile.txt");
	}
	
	@Test
	public void testToggles() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myConfig.json", "-rollback", "true", "-allowOrgAdminsToPublish", "true", "-replaceHostInSwagger", "true", "-force", "-forceUpdate", "-ignoreCache", "-useFEAPIDefinition", "-changeOrganization", "-ignoreAdminAccount", "-ignoreQuotas"};
		APIImportCLIOptions options = new APIImportCLIOptions(args);
		APIImportParams params = options.getAPIImportParams();
		Assert.assertTrue(params.isForce());
		Assert.assertTrue(params.isForceUpdate());
		Assert.assertTrue(params.isIgnoreCache());
		Assert.assertTrue(params.isAllowOrgAdminsToPublish());
		Assert.assertTrue(params.isChangeOrganization());
		Assert.assertTrue(params.isReplaceHostInSwagger());
		Assert.assertTrue(params.isUseFEAPIDefinition());
		Assert.assertTrue(params.isIgnoreQuotas());
		Assert.assertTrue(params.isIgnoreAdminAccount());
		Assert.assertTrue(params.isRollback());
	}
	
	@Test
	public void testModeParameterDefaults() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myConfig.json"};
		APIImportCLIOptions options = new APIImportCLIOptions(args);
		APIImportParams params = options.getAPIImportParams();
		options.addCoreParameters(params);
		Assert.assertFalse(params.isIgnoreClientApps(), "Should be false, as the default is add");
		Assert.assertFalse(params.isIgnoreClientOrgs(), "Should be false, as the default is add");
	}
	
	@Test
	public void testAPIDefinitionAsCLIArg() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myConfig.json", "-a", "thisIsMyAPIDefinition"};
		APIImportCLIOptions options = new APIImportCLIOptions(args);
		APIImportParams params = options.getAPIImportParams();
		options.addCoreParameters(params);
		Assert.assertEquals(params.getConfig(), "myConfig.json");
		Assert.assertEquals(params.getApiDefintion(), "thisIsMyAPIDefinition");
	}
}
