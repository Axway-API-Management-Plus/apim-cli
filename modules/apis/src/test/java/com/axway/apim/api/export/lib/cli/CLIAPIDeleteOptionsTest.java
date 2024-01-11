package com.axway.apim.api.export.lib.cli;

import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CLIAPIDeleteOptionsTest {

    @Test
    public void cliApiDelete() throws AppException {
        String[] args = {"-h", "localhost", "-n", "petstore"};
        CLIOptions options = CLIAPIDeleteOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        Assert.assertEquals(params.getName(), "petstore");
    }

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-h", "localhost", "-n", "petstore"};
        CLIOptions options = CLIAPIDeleteOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-n petstore"));
    }
}
