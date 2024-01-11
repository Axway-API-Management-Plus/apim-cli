package com.axway.apim.appexport.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class AppExportCLIOptionsTest {

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-h", "localhost", "-n", "test"};
        CLIOptions options = AppExportCLIOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-n test"));
    }
}
