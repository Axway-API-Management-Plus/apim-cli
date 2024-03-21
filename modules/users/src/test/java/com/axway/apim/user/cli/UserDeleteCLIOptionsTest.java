package com.axway.apim.user.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.users.lib.cli.UserDeleteCLIOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class UserDeleteCLIOptionsTest {

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-s", "prod", "-n", "test"};
        CLIOptions options = UserDeleteCLIOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-n test"));
    }
}
