package com.axway.apim.user.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.users.lib.cli.UserExportCLIOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class UserExportCLIOptionsTest {

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-s", "prod", "-id", "UUID-ID-OF-THE-USER", "-loginName", "*mark24*", "-n", "*Mark*", "-email", "*@axway.com*", "-type", "external", "-org", "*Partner*", "-role", "oadmin", "-state", "pending", "-enabled", "false", "-o", "json"};
        CLIOptions options = UserExportCLIOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-id UUID-ID-OF-THE-USER"));
    }
}
