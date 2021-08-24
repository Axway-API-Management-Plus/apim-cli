package com.axway.apim.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.appimport.lib.AppImportCLIOptions;
import com.axway.apim.appimport.lib.AppImportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;

public class AppCLIOptionsTest {
	@Test
	public void testAppImportParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myAppConfig.json"};
		CLIOptions options = AppImportCLIOptions.create(args);
		AppImportParams params = (AppImportParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate App-Import parameters
		Assert.assertEquals(params.getConfig(), "myAppConfig.json");
	}
	
	@Test
	public void testExportApplicationParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-n", "*My Great App*", "-id", "UUID-ID-OF-THE-APP", "-state", "pending", "-orgName", "*Partners*", "-credential", "*9877979779*", "-redirectUrl", "*localhost*", "-o", "json", "-wide"};
		CLIOptions options = AppExportCLIOptions.create(args);
		AppExportParams params = (AppExportParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate standard export parameters are included
		Assert.assertEquals(params.getWide(), Wide.wide);
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.json);
		
		// Validate App-Import parameters
		Assert.assertEquals(params.getName(), "*My Great App*");
		Assert.assertEquals(params.getId(), "UUID-ID-OF-THE-APP");
		Assert.assertEquals(params.getState(), "pending");
		Assert.assertEquals(params.getOrgName(), "*Partners*");
		Assert.assertEquals(params.getCredential(), "*9877979779*");
		Assert.assertEquals(params.getRedirectUrl(), "*localhost*");
	}

}
