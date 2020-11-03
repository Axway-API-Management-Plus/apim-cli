package com.axway.apim.setup.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsExportCLIOptions;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsExportParams;

public class RemoteHostExportCLIOptionsTest {
	@Test
	public void testRemoteHostExportParams() throws ParseException, AppException {
		String[] args = {"-s", "mytest", "-n", "*MyHost*", "-id", "MyRemoteHostID", "-t", "myTarget", "-o", "json", "-wide", "-deleteTarget"};
		CLIOptions options = RemoteHostsExportCLIOptions.create(args);
		RemoteHostsExportParams params = (RemoteHostsExportParams) options.getParams();
		// This make sure staging is working
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		
		Assert.assertEquals(params.getWide(), Wide.wide);
		Assert.assertTrue(params.isDeleteTarget());
		Assert.assertEquals(params.getTarget(), "myTarget");
		Assert.assertEquals(params.getName(), "*MyHost*");
		Assert.assertEquals(params.getId(), "MyRemoteHostID");
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.json);
	}
	
	@Test
	public void testUltra() throws ParseException, AppException {
		String[] args = {"-s", "mytest", "-ultra"};
		CLIOptions options = RemoteHostsExportCLIOptions.create(args);
		RemoteHostsExportParams params = (RemoteHostsExportParams) options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		
		Assert.assertEquals(params.getWide(), Wide.ultra);
		// Validate target is current directory if not given
		Assert.assertEquals(params.getTarget(), ".");
	}
}
