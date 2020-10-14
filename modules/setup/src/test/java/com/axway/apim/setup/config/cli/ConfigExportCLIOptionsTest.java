package com.axway.apim.setup.config.cli;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.setup.config.lib.ConfigExportCLIOptions;
import com.axway.apim.setup.config.lib.ConfigExportParams;

public class ConfigExportCLIOptionsTest {
	@Test
	public void testConfigExportParams() throws ParseException, AppException {
		String[] args = {"-s", "mytest", "-t", "myTarget", "-o", "csv", "-wide", "-deleteTarget"};
		ConfigExportCLIOptions options = new ConfigExportCLIOptions(args);
		ConfigExportParams params = options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		
		Assert.assertEquals(params.getWide(), Wide.wide);
		Assert.assertTrue(params.isDeleteTarget());
		Assert.assertEquals(params.getTarget(), "myTarget");
		Assert.assertEquals(params.getOutputFormat(), OutputFormat.csv);
	}
	
	@Test
	public void testUltra() throws ParseException, AppException {
		String[] args = {"-s", "mytest", "-ultra"};
		ConfigExportCLIOptions options = new ConfigExportCLIOptions(args);
		ConfigExportParams params = options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		
		Assert.assertEquals(params.getWide(), Wide.ultra);
		// Validate target is current directory if not given
		Assert.assertEquals(params.getTarget(), ".");
	}
}
