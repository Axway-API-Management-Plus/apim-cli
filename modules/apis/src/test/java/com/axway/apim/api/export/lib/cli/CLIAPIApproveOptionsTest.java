package com.axway.apim.api.export.lib.cli;

import com.axway.apim.api.export.lib.params.APIApproveParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CLIAPIApproveOptionsTest {

    @Test
    public void testCliApiApprove() throws AppException {

        String[] args = {"-h", "localhost", "-n", "petstore", "-publishVHost", "api.axway.com"};
        CLIOptions options = CLIAPIApproveOptions.create(args);
        APIApproveParams params = (APIApproveParams) options.getParams();

        Assert.assertEquals(params.getPublishVhost(), "api.axway.com");
        Assert.assertEquals(params.getName(), "petstore");

    }


    @Test
    public void testApproveAPIParameters() throws AppException {
        String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-publishVHost", "my.api-host.com"};
        CLIOptions cliOptions = CLIAPIApproveOptions.create(args);
        APIApproveParams params = (APIApproveParams)cliOptions.getParams();

        // Validate core parameters are included
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        // Validate an API-Filter parameters are included
        Assert.assertEquals(params.getApiPath(), "/api/v1/greet");

        Assert.assertEquals(params.getPublishVhost(), "my.api-host.com");
    }

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-publishVHost", "my.api-host.com"};
        CLIOptions options = CLIAPIApproveOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-publishVHost my.api-host.com"));
    }
}
