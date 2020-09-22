package com.axway.apim.user.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.users.lib.UserExportCLIOptions;
import com.axway.apim.users.lib.UserExportParams;
import com.axway.apim.users.lib.UserImportCLIOptions;
import com.axway.apim.users.lib.UserImportParams;

public class UserCLIOptionsTest {
	@Test
	public void testAppImportParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myUserConfig.json"};
		UserImportCLIOptions options = new UserImportCLIOptions(args);
		UserImportParams params = options.getUserImportParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate App-Import parameters
		Assert.assertEquals(params.getConfig(), "myUserConfig.json");
	}
	
	@Test
	public void testExportApplicationParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-id", "UUID-ID-OF-THE-USER", "-loginName", "*mark24*", "-n", "*Mark*", "-email", "*@axway.com*", "-type", "external", "-org", "*Partner*", "-role", "oadmin", "-state", "pending", "-enabled", "false", "-o", "json"};
		UserExportCLIOptions options = new UserExportCLIOptions(args);
		UserExportParams params = options.getUserExportParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate standard export parameters are included
		Assert.assertEquals(params.getWide(), Wide.standard);
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.json);
		
		// Validate Org-Import parameters
		Assert.assertEquals(params.getId(), "UUID-ID-OF-THE-USER");
		Assert.assertEquals(params.getLoginName(), "*mark24*");
		Assert.assertEquals(params.getName(), "*Mark*");
		Assert.assertEquals(params.getEmail(), "*@axway.com*");
		Assert.assertEquals(params.getType(), "external");
		Assert.assertEquals(params.getOrg(), "*Partner*");
		Assert.assertEquals(params.getRole(), "oadmin");
		Assert.assertEquals(params.getState(), "pending");
		Assert.assertFalse(params.isEnabled());
	}
	
	
	@Test
	public void testEnabledToggleDefault() throws ParseException, AppException {
		String[] args = {"-s", "prod"};
		UserExportCLIOptions options = new UserExportCLIOptions(args);
		UserExportParams params = options.getUserExportParams();
		Assert.assertTrue(params.isEnabled(), "Enabled must be true, as it's not given!");
	}

}
