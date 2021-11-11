package com.axway.apim.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.export.lib.cli.CLIAPIApproveOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIExportOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIGrantAccessOptions;
import com.axway.apim.api.export.lib.cli.CLIAPIUpgradeAccessOptions;
import com.axway.apim.api.export.lib.cli.CLIChangeAPIOptions;
import com.axway.apim.api.export.lib.cli.CLICheckCertificatesOptions;
import com.axway.apim.api.export.lib.params.APIApproveParams;
import com.axway.apim.api.export.lib.params.APIChangeParams;
import com.axway.apim.api.export.lib.params.APICheckCertificatesParams;
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
		String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-n", "*MyAPIName*", "-id", "412378923", "-policy", "*PolicyName*", "-vhost", "custom.host.com", "-state", "approved", "-backend", "backend.customer.com", "-tag", "*myTag*", "-t", "myTarget", "-o", "csv", "-useFEAPIDefinition", "-wide", "-deleteTarget", "-datPassword", "123456Axway"};
		CLIOptions options = CLIAPIExportOptions.create(args);
		APIExportParams params = (APIExportParams) options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
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
		Assert.assertEquals(params.getDatPassword(), "123456Axway");
	}
	
	@Test
	public void testUltra() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-ultra"};
		CLIOptions options = CLIAPIExportOptions.create(args);
		APIExportParams params = (APIExportParams) options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
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
		Assert.assertEquals(params.getHostname(), "localhost");
		
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
		Assert.assertEquals(params.getHostname(), "localhost");
		
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
		Assert.assertEquals(params.getHostname(), "localhost");
		
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
		String[] args = {"-s", "prod", "-a", "/api/v1/some", "-orgName", "OrgName", "-orgId", "OrgId", "-n", "MyAPIName", "-org", "MyAPIOrg", "-id", "MY-API-ID", "-vhost", "api.chost.com", "-backend", "backend.host", 
				"-policy", "PolicyName", "-inboundsecurity", "api-key", "-tag", "tagGroup=*myTagValue*"};
		CLIOptions cliOptions = CLIAPIGrantAccessOptions.create(args);
		APIGrantAccessParams params = (APIGrantAccessParams)cliOptions.getParams();
		
		// Validate core parameters are included
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
		
		// Validate an API-Filter parameters are included
		Assert.assertEquals(params.getApiPath(), "/api/v1/some");
		Assert.assertEquals(params.getName(), "MyAPIName");
		Assert.assertEquals(params.getBackend(), "backend.host");
		Assert.assertEquals(params.getPolicy(), "PolicyName");
		Assert.assertEquals(params.getVhost(), "api.chost.com");
		Assert.assertEquals(params.getInboundSecurity(), "api-key");
		Assert.assertEquals(params.getId(), "MY-API-ID");
		Assert.assertEquals(params.getTag(), "tagGroup=*myTagValue*");
		
		// Validate Grant-Access params are included
		Assert.assertEquals(params.getOrgId(), "OrgId");
		Assert.assertEquals(params.getOrgName(), "OrgName");
		
		APIFilter apiFilter = params.getAPIFilter();
		Assert.assertEquals(apiFilter.getState(), "published"); // Must be published as only published APIs can be considered for grant access
		Assert.assertEquals(apiFilter.getApiPath(), "/api/v1/some");
		Assert.assertEquals(apiFilter.getName(), "MyAPIName");
		Assert.assertEquals(apiFilter.getBackendBasepath(), "backend.host");
		Assert.assertEquals(apiFilter.getPolicyName(), "PolicyName");
		Assert.assertEquals(apiFilter.getVhost(), "api.chost.com");
		Assert.assertEquals(apiFilter.getInboundSecurity(), "api-key");
		Assert.assertEquals(apiFilter.getTag(), "tagGroup=*myTagValue*");
	}
	
	@Test
	public void testCreatedOnAPIFilterParameters() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-createdOn", "2020-01-01:2020-12-31"};
		CLIOptions options = CLIAPIExportOptions.create(args);
		APIExportParams params = (APIExportParams) options.getParams();
		Assert.assertEquals(params.getCreatedOnAfter().toString(), "1577836800000");
		Assert.assertEquals(params.getCreatedOnBefore().toString(), "1609459199000");
		
		// This means:
		// 2020 as the start	- It should be the same as 2020-01-01
		// 2021 as the end		- It should be the same as 2021-12-31 23:59:59
		String[] args2 = {"-s", "prod", "-createdOn", "2020:2021"};
		options = CLIAPIExportOptions.create(args2);
		params = (APIExportParams) options.getParams();
		Assert.assertEquals(params.getCreatedOnAfter().toString(), "1577836800000");
		Assert.assertEquals(params.getCreatedOnBefore().toString(), "1640995199000");
		
		// This means:
		// 2020-06 as the start	- It should be the same as 2020-06-01
		// now as the end		- The current date
		String[] args3 = {"-s", "prod", "-createdOn", "2020-06:now"};
		options = CLIAPIExportOptions.create(args3);
		params = (APIExportParams) options.getParams();
		Assert.assertEquals(params.getCreatedOnAfter().toString(), "1590969600000");
		Assert.assertTrue(Long.parseLong(params.getCreatedOnBefore())>Long.parseLong("1630665581555"), "Now should be always in the future.");
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "You cannot use 'now' as the start date.")
	public void testCreatedOnWithStartNow() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-createdOn", "now:2020-12-31"};
		CLIOptions options = CLIAPIExportOptions.create(args);
		options.getParams();
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "You must separate the start- and end-date with a ':'.")
	public void testCreatedWithoutColon() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-createdOn", "2020-01-01-2020-12-31"};
		CLIOptions options = CLIAPIExportOptions.create(args);
		options.getParams();
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "The start-date: 01/Jan/2021 00:00:00 GMT cannot be bigger than the end date: 31/Dec/2020 23:59:59 GMT.")
	public void testCreatedOnWithBiggerStartDate() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-createdOn", "2021-01-01:2020-12-31"};
		CLIOptions options = CLIAPIExportOptions.create(args);
		options.getParams();
	}
	
	@Test
	public void testCertificateCheckParams() throws ParseException, AppException {
		String[] args = {"-s", "prod", "-days", "999"};
		CLIOptions options = CLICheckCertificatesOptions.create(args);
		APICheckCertificatesParams params = (APICheckCertificatesParams)options.getParams();
		Assert.assertEquals(params.getNumberOfDays(), 999);
		// Check base parameters to make sure, all parameters up to the root are parsed
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getHostname(), "localhost");
	}
}
