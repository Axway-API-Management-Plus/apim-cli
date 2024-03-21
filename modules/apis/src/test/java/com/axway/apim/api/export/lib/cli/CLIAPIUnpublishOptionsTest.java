package com.axway.apim.api.export.lib.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CLIAPIUnpublishOptionsTest {

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-s", "prod", "-id", "f6106454-1651-430e-8a2f-e3514afad8ee"};
        CLIOptions options = CLIAPIUnpublishOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-id f6106454-1651-430e-8a2f-e3514afad8ee"));
    }
}
