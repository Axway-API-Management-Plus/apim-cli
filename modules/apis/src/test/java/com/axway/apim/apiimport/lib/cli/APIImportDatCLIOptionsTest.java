package com.axway.apim.apiimport.lib.cli;

import com.axway.apim.apiimport.lib.params.APIImportDatParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class APIImportDatCLIOptionsTest {

	@Test
	public void testAPIImportDatParameter() throws AppException {
		String[] args = {"-s", "prod", "-a", "api-export.dat", "-orgName", "API Development", "-datPassword", "changeme"};
        CLIOptions options = CLIAPIImportDatOptions.create(args);
        APIImportDatParams params = (APIImportDatParams) options.getParams();
		Assert.assertEquals(params.getUsername(), "apiadmin");
		Assert.assertEquals(params.getPassword(), "changeme");
		Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");
		Assert.assertEquals(params.getApiDefinition(), "api-export.dat");
		Assert.assertEquals(params.getOrgName(), "API Development");
		Assert.assertEquals(params.getDatPassword(), "changeme");
	}

	@Test
	public void testAPIImportDatParameterWithoutPassword() throws AppException {
        String[] args = {"-s", "prod", "-a", "api-export.dat", "-orgName", "API Development"};
        CLIOptions options = CLIAPIImportDatOptions.create(args);
        APIImportDatParams params = (APIImportDatParams) options.getParams();
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");
        Assert.assertEquals(params.getApiDefinition(), "api-export.dat");
        Assert.assertEquals(params.getOrgName(), "API Development");
        Assert.assertNull(params.getDatPassword());
	}

	@Test
	public void testPrintUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-s", "prod", "-a", "api-export.dat", "-orgName", "API Development"};
        CLIOptions options = CLIAPIImportDatOptions.create(args);
        options.printUsage("test", args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("import-dat"));

    }
}
