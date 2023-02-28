package com.axway.apim.user.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.users.lib.UserImportParams;
import com.axway.apim.users.lib.cli.UserChangePasswordCLIOptions;
import com.axway.apim.users.lib.cli.UserExportCLIOptions;
import com.axway.apim.users.lib.cli.UserImportCLIOptions;
import com.axway.apim.users.lib.params.UserChangePasswordParams;
import com.axway.apim.users.lib.params.UserExportParams;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UserCLIOptionsTest {

	private String apimCliHome;
	@BeforeClass
	private void init() throws IOException, URISyntaxException {
		URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
		apimCliHome =  Paths.get(uri) + File.separator + "apimcli";
		String confPath = String.valueOf(Files.createDirectories(Paths.get(apimCliHome + "/conf")).toAbsolutePath());
		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
			 OutputStream outputStream= Files.newOutputStream(new File(confPath, "env.properties").toPath())){
			IOUtils.copy(inputStream,outputStream );
		}
	}

	@Test
	public void testUserImportParameters() throws AppException {
		String[] args = {"-s", "prod", "-c", "myUserConfig.json", "-apimCLIHome", apimCliHome};
		CLIOptions options = UserImportCLIOptions.create(args);
		UserImportParams params = (UserImportParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");
		
		// Validate App-Import parameters
		Assert.assertEquals(params.getConfig(), "myUserConfig.json");
		Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
	}
	
	@Test
	public void testExportUserParameters() throws AppException {
		String[] args = {"-s", "prod", "-id", "UUID-ID-OF-THE-USER", "-loginName", "*mark24*", "-n", "*Mark*", "-email", "*@axway.com*", "-type", "external", "-org", "*Partner*", "-role", "oadmin", "-state", "pending", "-enabled", "false", "-o", "json", "-apimCLIHome", apimCliHome};
		CLIOptions options = UserExportCLIOptions.create(args);
		UserExportParams params = (UserExportParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");
		
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
		Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
	}
	
	@Test
	public void testUserChangePasswordParameters() throws AppException {
		String[] args = {"-s", "prod", "-id", "UUID-ID-OF-THE-USER", "-loginName", "*mark24*", "-n", "*Mark*", "-email", "*@axway.com*", "-type", "external", "-org", "*Partner*", "-role", "oadmin", "-state", "pending", "-enabled", "true", "-o", "json", "-newpassword", "123456", "-apimCLIHome", apimCliHome};
		CLIOptions options = UserChangePasswordCLIOptions.create(args);
		UserChangePasswordParams params = (UserChangePasswordParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");
		
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
		Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
	}
	
	
	@Test
	public void testEnabledToggleDefault() throws AppException {
		String[] args = {"-s", "prod"};
		CLIOptions options = UserExportCLIOptions.create(args);
		UserExportParams params = (UserExportParams) options.getParams();
		Assert.assertNull(params.isEnabled(), "Should be null, if not given");
	}

}
