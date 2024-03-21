package com.axway.apim.api.export.lib.cli;

import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CLIAPIExportOptionsTest {


    @Test
    public void cliApiExport() throws AppException {

        String[] args = {"-h", "localhost", "-n", "petstore"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        Assert.assertEquals(params.getName(), "petstore");

    }

    @Test
    public void testAPIExportParams() throws AppException {
        String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-n", "*MyAPIName*", "-id", "412378923", "-policy", "*PolicyName*", "-vhost", "custom.host.com", "-state", "approved", "-backend", "backend.customer.com", "-tag", "*myTag*", "-t", "myTarget", "-o", "csv", "-useFEAPIDefinition", "-wide", "-deleteTarget", "-datPassword", "123456Axway"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        Assert.assertEquals(params.getWide(), StandardExportParams.Wide.wide);
        Assert.assertTrue(params.isDeleteTarget());
        Assert.assertEquals(params.getTarget(), "myTarget");
        Assert.assertEquals(params.getTag(), "*myTag*");
        Assert.assertEquals(params.getOutputFormat(), StandardExportParams.OutputFormat.csv);

        Assert.assertTrue(params.isUseFEAPIDefinition());
        Assert.assertEquals(params.getApiPath(), "/api/v1/greet");
        Assert.assertEquals(params.getName(), "*MyAPIName*");
        Assert.assertEquals(params.getId(), "412378923");
        Assert.assertEquals(params.getPolicy(), "*PolicyName*");
        Assert.assertEquals(params.getVhost(), "custom.host.com");
        Assert.assertEquals(params.getState(), "approved");
        Assert.assertEquals(params.getBackend(), "backend.customer.com");
        Assert.assertEquals(params.getDatPassword(), "123456Axway");
        Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
    }


    @Test
    public void testUltra() throws AppException {
        String[] args = {"-s", "prod", "-ultra"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        Assert.assertEquals(params.getWide(), StandardExportParams.Wide.ultra);
        // Validate target is current directory if not given
        Assert.assertNotEquals(params.getTarget(), "");
    }


    @Test
    public void testCreatedOnAPIFilterParameters() throws AppException {
        String[] args = {"-s", "prod", "-createdOn", "2020-01-01:2020-12-31"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        Assert.assertEquals(params.getCreatedOnAfter(), "1577836800000");
        Assert.assertEquals(params.getCreatedOnBefore(), "1609459199000");

        // This means:
        // 2020 as the start	- It should be the same as 2020-01-01
        // 2021 as the end		- It should be the same as 2021-12-31 23:59:59
        String[] args2 = {"-s", "prod", "-createdOn", "2020:2021"};
        options = CLIAPIExportOptions.create(args2);
        params = (APIExportParams) options.getParams();
        Assert.assertEquals(params.getCreatedOnAfter(), "1577836800000");
        Assert.assertEquals(params.getCreatedOnBefore(), "1640995199000");

        // This means:
        // 2020-06 as the start	- It should be the same as 2020-06-01
        // now as the end		- The current date
        String[] args3 = {"-s", "prod", "-createdOn", "2020-06:now"};
        options = CLIAPIExportOptions.create(args3);
        params = (APIExportParams) options.getParams();
        Assert.assertEquals(params.getCreatedOnAfter(), "1590969600000");
        Assert.assertTrue(Long.parseLong(params.getCreatedOnBefore()) > Long.parseLong("1630665581555"), "Now should be always in the future.");
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "The start-date: 01/Jan/2021 00:00:00 GMT cannot be bigger than the end date: 31/Dec/2020 23:59:59 GMT.")
    public void testCreatedOnWithBiggerStartDate() throws AppException {
        String[] args = {"-s", "prod", "-createdOn", "2021-01-01:2020-12-31"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        options.getParams();
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "You cannot use 'now' as the start date.")
    public void testCreatedOnWithStartNow() throws AppException {
        String[] args = {"-s", "prod", "-createdOn", "now:2020-12-31"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        options.getParams();
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "You must separate the start- and end-date with a ':'.")
    public void testCreatedWithoutColon() throws AppException {
        String[] args = {"-s", "prod", "-createdOn", "2020-01-01-2020-12-31"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        options.getParams();
    }


    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-s", "prod", "-createdOn", "2020-01-01-2020-12-31"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-createdOn 2020-01-01-2020-12-31 "));
    }

}
