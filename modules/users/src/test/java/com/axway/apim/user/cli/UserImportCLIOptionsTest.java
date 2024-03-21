package com.axway.apim.user.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.users.lib.cli.UserImportCLIOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class UserImportCLIOptionsTest {

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-s", "prod", "-c", "myUserConfig.json"};
        CLIOptions options = UserImportCLIOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-c myUserConfig.json"));
    }
}
