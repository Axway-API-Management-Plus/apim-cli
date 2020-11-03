package com.axway.apim.organization.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.lib.OrgExportCLIOptions;
import com.axway.apim.organization.lib.OrgExportParams;
import com.axway.apim.organization.lib.OrgImportCLIOptions;
import com.axway.apim.organization.lib.OrgImportParams;

public class OrgCLIOptionsTest {
	@Test
	public void testAppImportParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-c", "myOrgConfig.json"};
		CLIOptions options = OrgImportCLIOptions.create(args);
		OrgImportParams params = (OrgImportParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate App-Import parameters
		Assert.assertEquals(params.getConfig(), "myOrgConfig.json");
	}
	
	@Test
	public void testExportApplicationParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-n", "*My organization*", "-id", "UUID-ID-OF-THE-ORG", "-dev", "true", "-o", "csv", "-ultra"};
		CLIOptions options = OrgExportCLIOptions.create(args);
		OrgExportParams params = (OrgExportParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate standard export parameters are included
		Assert.assertEquals(params.getWide(), Wide.ultra);
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.csv);
		
		// Validate Org-Import parameters
		Assert.assertEquals(params.getName(), "*My organization*");
		Assert.assertEquals(params.getId(), "UUID-ID-OF-THE-ORG");
		Assert.assertEquals(params.getDev(), "true");
	}

}
