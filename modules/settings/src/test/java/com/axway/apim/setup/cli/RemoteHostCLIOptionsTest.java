package com.axway.apim.setup.cli;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.setup.lib.APIManagerSetupImportCLIOptions;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsExportCLIOptions;
import com.axway.apim.setup.remotehosts.lib.RemoteHostsExportParams;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RemoteHostCLIOptionsTest {

	private String apimCliHome;
	@BeforeClass
	private void init() throws IOException, URISyntaxException {
		URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
		apimCliHome =  Paths.get(uri) + File.separator + "apimcli";
		String confPath = String.valueOf(Files.createDirectories(Paths.get(apimCliHome + "/conf")).toAbsolutePath());
		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
			 OutputStream outputStream=new FileOutputStream(new File(confPath, "env.properties" ))){
			IOUtils.copy(inputStream,outputStream );
		}
	}

	@Test
	public void testRemoteHostExportParams() throws ParseException, AppException {
		String[] args = {"-s", "mytest", "-n", "*MyHost*", "-id", "MyRemoteHostID", "-t", "myTarget", "-o", "json", "-wide", "-deleteTarget", "-apimCLIHome", apimCliHome};
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
		Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
	}
	
	@Test
	public void testUltra() throws ParseException, AppException {
		String[] args = {"-s", "mytest", "-ultra", "-apimCLIHome", apimCliHome};
		CLIOptions options = RemoteHostsExportCLIOptions.create(args);
		RemoteHostsExportParams params = (RemoteHostsExportParams) options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		
		Assert.assertEquals(params.getWide(), Wide.ultra);
		// Validate target is current directory if not given
		Assert.assertNotEquals(params.getTarget(), "");
	}
	
	@Test
	public void testSettingsImportParams() throws ParseException, AppException {
		String[] args = {"-c", "mySettings.json", "-stageConfig", "myStagedSettings.json"};
		CLIOptions options = APIManagerSetupImportCLIOptions.create(args);
		StandardImportParams params = (StandardImportParams) options.getParams();
		
		Assert.assertEquals(params.getConfig(), "mySettings.json");
		Assert.assertEquals(params.getStageConfig(), "myStagedSettings.json");
		Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
	}
}
