package com.axway.apim.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.api.export.lib.cli.CLIAPIApproveOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIExportOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIGrantAccessOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIUpgradeAccessOptions;
import com.axway.apim.api.export.lib.cli.CLIChangeAPIOptions;
import com.axway.apim.api.export.lib.params.APIApproveParams;
import com.axway.apim.api.export.lib.params.APIChangeParams;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.export.lib.params.APIGrantAccessParams;
import com.axway.apim.api.export.lib.params.APIUpgradeAccessParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;

public class APIExportCLIOptionsTest {
	@Test
	public void testAPIExportParams() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-n", "*MyAPIName*", "-id", "412378923", "-policy", "*PolicyName*", "-vhost", "custom.host.com", "-state", "approved", "-backend", "backend.customer.com", "-tag", "*myTag*", "-t", "myTarget", "-o", "csv", "-useFEAPIDefinition", "-wide", "-deleteTarget"};
		CLIOptions options = CLIAPIExportOptions.create(args);
		APIExportParams params = (APIExportParams) options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		
		Assert.assertEquals(params.getWide(), Wide.wide);
		Assert.assertTrue(params.isDeleteTarget());
		Assert.assertEquals(params.getTarget(), "myTarget");
		Assert.assertEquals(params.getTag(), "*myTag*");
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.csv);
		
		Assert.assertTrue(params.isUseFEAPIDefinition());
		Assert.assertEquals(params.getApiPath(), "/api/v1/greet");
		Assert.assertEquals(params.getName(), "*MyAPIName*");
		Assert.assertEquals(params.getId(), "412378923");
		Assert.assertEquals(params.getPolicy(), "*PolicyName*");
		Assert.assertEquals(params.getVhost(), "custom.host.com");
		Assert.assertEquals(params.getState(), "approved");
		Assert.assertEquals(params.getBackend(), "backend.customer.com");
	}
	
	@Test
	public void testUltra() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-ultra"};
		CLIOptions options = CLIAPIExportOptions.create(args);
		APIExportParams params = (APIExportParams) options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		
		Assert.assertEquals(params.getWide(), Wide.ultra);
		// Validate target is current directory if not given
		Assert.assertEquals(params.getTarget(), ".");
	}
	
	@Test
	public void testChangeAPIParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-newBackend", "http://my.new.backend", "-oldBackend", "http://my.old.backend"};
		CLIOptions options = CLIChangeAPIOptions.create(args);
		APIChangeParams params = (APIChangeParams) options.getParams();
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		
		// Validate wide is is using standard as default
		Assert.assertEquals(params.getWide(), Wide.standard);
		// Validate the output-format is Console as the default
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.console);
		
		// Validate an API-Filter parameters are included
		Assert.assertEquals(params.getApiPath(), "/api/v1/greet");
		
		// Validate the change parameters are included
		Assert.assertEquals(params.getNewBackend(), "http://my.new.backend");
		Assert.assertEquals(params.getOldBackend(), "http://my.old.backend");
	}
	
	@Test
	public void testApproveAPIParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-publishVHost", "my.api-host.com"};
		CLIOptions cliOptions = CLIAPIApproveOptions.create(args);
		APIApproveParams params = (APIApproveParams)cliOptions.getParams();
		
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		
		// Validate an API-Filter parameters are included
		Assert.assertEquals(params.getApiPath(), "/api/v1/greet");
		
		Assert.assertEquals(params.getPublishVhost(), "my.api-host.com");
	}
	
	@Test
	public void testUpgradeAccessAPIParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-a", "/api/v1/to/be/upgraded", "-refAPIId", "123456", "-refAPIName", "myRefOldAPI", "-refAPIVersion", "1.2.3", "-refAPIOrg", "RefOrg", "-refAPIDeprecate", "true", "-refAPIRetire", "true", "-refAPIRetireDate", "31.12.2021"};
		CLIOptions cliOptions = CLIAPIUpgradeAccessOptions.create(args);
		APIUpgradeAccessParams params = (APIUpgradeAccessParams)cliOptions.getParams();
		
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		
		// Validate an API-Filter parameters are included
		Assert.assertEquals(params.getApiPath(), "/api/v1/to/be/upgraded");
		
		Assert.assertEquals(params.getReferenceAPIId(), "123456");
		Assert.assertEquals(params.getReferenceAPIName(), "myRefOldAPI");
		Assert.assertEquals(params.getReferenceAPIVersion(), "1.2.3");
		Assert.assertEquals(params.getReferenceAPIOrganization(), "RefOrg");
		Assert.assertTrue(params.getReferenceAPIRetire());
		Assert.assertTrue(params.getReferenceAPIDeprecate());
		Assert.assertTrue(params.getReferenceAPIRetirementDate() == Long.parseLong("1640908800000"));
		
		// Make sure, the default handling works for deprecate / and retire
		String[] args2 = {"-s", "prod", "-a", "/api/v1/to/be/upgraded"};
		cliOptions = CLIAPIUpgradeAccessOptions.create(args2);
		params = (APIUpgradeAccessParams)cliOptions.getParams();
		Assert.assertFalse(params.getReferenceAPIRetire());
		Assert.assertFalse(params.getReferenceAPIDeprecate());
	}
	
	@Test
	public void testGrantAccessAPIParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-a", "/api/v1/some", "-orgName", "OrgName", "-orgId", "OrgId", "-n", "MyAPIName", "-org", "MyAPIOrg", "-id", "API-ID", "-vhost", "api.chost.com", "-backend", "backend.host"};
		CLIOptions cliOptions = CLIAPIGrantAccessOptions.create(args);
		APIGrantAccessParams params = (APIGrantAccessParams)cliOptions.getParams();
		
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "api-env");
		
		// Validate an API-Filter parameters are included
		Assert.assertEquals(params.getApiPath(), "/api/v1/some");
		Assert.assertEquals(params.getName(), "MyAPIName");
		Assert.assertEquals(params.getBackend(), "backend.host");
		
		// Validate Grant-Access params are included
		Assert.assertEquals(params.getOrgId(), "OrgId");
		Assert.assertEquals(params.getOrgName(), "OrgName");
	}
}
