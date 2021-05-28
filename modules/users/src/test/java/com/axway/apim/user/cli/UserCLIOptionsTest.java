package com.axway.apim.user.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.users.lib.UserImportParams;
import com.axway.apim.users.lib.cli.UserChangePasswordCLIOptions;
import com.axway.apim.users.lib.cli.UserExportCLIOptions;
import com.axway.apim.users.lib.cli.UserImportCLIOptions;
import com.axway.apim.users.lib.params.UserChangePasswordParams;
import com.axway.apim.users.lib.params.UserExportParams;

public class UserCLIOptionsTest {
	@Test
	public void testUserImportParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myUserConfig.json"};
		CLIOptions options = UserImportCLIOptions.create(args);
		UserImportParams params = (UserImportParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate App-Import parameters
		Assert.assertEquals(params.getConfig(), "myUserConfig.json");
	}
	
	@Test
	public void testExportUserParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-id", "UUID-ID-OF-THE-USER", "-loginName", "*mark24*", "-n", "*Mark*", "-email", "*@axway.com*", "-type", "external", "-org", "*Partner*", "-role", "oadmin", "-state", "pending", "-enabled", "false", "-o", "json"};
		CLIOptions options = UserExportCLIOptions.create(args);
		UserExportParams params = (UserExportParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate standard export parameters are included
		Assert.assertEquals(params.getWide(), Wide.standard);
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.json);
		
		// Validate user filter parameters
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
	public void testUserChangePasswordParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-id", "UUID-ID-OF-THE-USER", "-loginName", "*mark24*", "-n", "*Mark*", "-email", "*@axway.com*", "-type", "external", "-org", "*Partner*", "-role", "oadmin", "-state", "pending", "-enabled", "true", "-o", "json", "-newpassword", "123456"};
		CLIOptions options = UserChangePasswordCLIOptions.create(args);
		UserChangePasswordParams params = (UserChangePasswordParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate user filter parameters
		Assert.assertEquals(params.getId(), "UUID-ID-OF-THE-USER");
		Assert.assertEquals(params.getLoginName(), "*mark24*");
		Assert.assertEquals(params.getName(), "*Mark*");
		Assert.assertEquals(params.getEmail(), "*@axway.com*");
		Assert.assertEquals(params.getType(), "external");
		Assert.assertEquals(params.getOrg(), "*Partner*");
		Assert.assertEquals(params.getRole(), "oadmin");
		Assert.assertEquals(params.getState(), "pending");
		Assert.assertTrue(params.isEnabled());
		
		Assert.assertEquals(params.getNewPassword(), "123456");
	}
	
	
	@Test
	public void testEnabledToggleDefault() throws ParseException, AppException {
		String[] args = {"-s", "prod"};
		CLIOptions options = UserExportCLIOptions.create(args);
		UserExportParams params = (UserExportParams) options.getParams();
		Assert.assertNull(params.isEnabled(), "Should be null, if not given");
	}

}
