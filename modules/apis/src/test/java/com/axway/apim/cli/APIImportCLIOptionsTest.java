package com.axway.apim.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.apiimport.lib.cli.CLIAPIImportOptions;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreParameters.Mode;
import com.axway.apim.lib.errorHandling.AppException;

public class APIImportCLIOptionsTest {
	@Test
	public void testWithoutAdminUser() throws ParseException, AppException {
		String[] args = {"-u", "myUser", "-p", "myPassword", "-port", "8175", "-c", "myConfig.json"};
		CLIOptions options = CLIAPIImportOptions.create(args);
		APIImportParams params = (APIImportParams) options.getParams();
		Assert.assertEquals(params.getUsername(), "myUser");        // Taken from cmd directly
		Assert.assertEquals(params.getPassword(), "myPassword");    // Taken from cmd directly
		Assert.assertEquals(params.getAdminUsername(), "apiadmin"); // Loaded from env.properties
		Assert.assertEquals(params.getAdminPassword(), "changeme"); // Loaded from env.properties
		Assert.assertEquals(params.getConfig(), "myConfig.json");
	}
	
	@Test
	public void testUserDetailsFromStage() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myConfig.json"};
		CLIOptions options = CLIAPIImportOptions.create(args);
		APIImportParams params = (APIImportParams) options.getParams();
		Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");
	}
	
	@Test
	public void testAPIImportParameter() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myConfig.json", "-clientOrgsMode", "replace", "-clientAppsMode", "replace", "-quotaMode", "replace", "-detailsExportFile", "myExportFile.txt", "-stageConfig", "myStageConfigFile.json", "-enabledCaches", "applicationsQuotaCache,*API*"};
		CLIOptions options = CLIAPIImportOptions.create(args);
		APIImportParams params = (APIImportParams) options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");
		Assert.assertEquals(params.getClientOrgsMode(), Mode.replace);
		Assert.assertEquals(params.getClientAppsMode(), Mode.replace);
		Assert.assertEquals(params.getQuotaMode(), Mode.replace);
		Assert.assertEquals(params.getDetailsExportFile(), "myExportFile.txt");
		Assert.assertEquals(params.getStageConfig(), "myStageConfigFile.json");
		Assert.assertEquals(params.getEnabledCaches(), "applicationsQuotaCache,*API*");
	}
	
	@Test
	public void testToggles() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myConfig.json", "-rollback", "true", "-allowOrgAdminsToPublish", "false", "-replaceHostInSwagger", "true", "-force", "-forceUpdate", "-ignoreCache", "-useFEAPIDefinition", "-changeOrganization", "-ignoreAdminAccount", "-ignoreQuotas", "-updateOnly"};
		CLIOptions options = CLIAPIImportOptions.create(args);
		APIImportParams params = (APIImportParams) options.getParams();
		Assert.assertTrue(params.isForce());
		Assert.assertTrue(params.isForceUpdate());
		Assert.assertTrue(params.isIgnoreCache());
		Assert.assertTrue(params.isUpdateOnly());
		Assert.assertFalse(params.isAllowOrgAdminsToPublish());
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
		CLIOptions options = CLIAPIImportOptions.create(args);
		APIImportParams params = (APIImportParams) options.getParams();
		Assert.assertFalse(params.isIgnoreClientApps(), "Should be false, as the default is add");
		Assert.assertFalse(params.isIgnoreClientOrgs(), "Should be false, as the default is add");
		Assert.assertFalse(params.isUpdateOnly());
	}
	
	@Test
	public void testAPIDefinitionAsCLIArg() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myConfig.json", "-a", "thisIsMyAPIDefinition"};
		CLIOptions options = CLIAPIImportOptions.create(args);
		APIImportParams params = (APIImportParams) options.getParams();
		Assert.assertEquals(params.getConfig(), "myConfig.json");
		Assert.assertEquals(params.getApiDefintion(), "thisIsMyAPIDefinition");
	}
}
