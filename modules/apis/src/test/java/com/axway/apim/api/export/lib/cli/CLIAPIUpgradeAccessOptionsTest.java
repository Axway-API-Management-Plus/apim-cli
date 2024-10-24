package com.axway.apim.api.export.lib.cli;

import com.axway.apim.api.export.lib.params.APIUpgradeAccessParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CLIAPIUpgradeAccessOptionsTest {

    @Test
    public void testUpgradeAccessAPIParameters() throws AppException {
        String[] args = {"-s", "prod", "-a", "/api/v1/to/be/upgraded", "-refAPIId", "123456", "-refAPIName", "myRefOldAPI", "-refAPIVersion", "1.2.3", "-refAPIOrg", "RefOrg", "-refAPIDeprecate", "true", "-refAPIRetire", "true", "-refAPIRetireDate", "31.12.2027"};
        CLIOptions cliOptions = CLIAPIUpgradeAccessOptions.create(args);
        APIUpgradeAccessParams params = (APIUpgradeAccessParams)cliOptions.getParams();

        // Validate core parameters are included
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        // Validate an API-Filter parameters are included
        Assert.assertEquals(params.getApiPath(), "/api/v1/to/be/upgraded");

        Assert.assertEquals(params.getReferenceAPIId(), "123456");
        Assert.assertEquals(params.getReferenceAPIName(), "myRefOldAPI");
        Assert.assertEquals(params.getReferenceAPIVersion(), "1.2.3");
        Assert.assertEquals(params.getReferenceAPIOrganization(), "RefOrg");
        Assert.assertTrue(params.isReferenceAPIRetire());
        Assert.assertTrue(params.isReferenceAPIDeprecate());
        Assert.assertEquals(Long.parseLong("1830211200000"), (long) params.getReferenceAPIRetirementDate());

        // Make sure, the default handling works for deprecate / and retire
        String[] args2 = {"-s", "prod", "-a", "/api/v1/to/be/upgraded"};
        cliOptions = CLIAPIUpgradeAccessOptions.create(args2);
        params = (APIUpgradeAccessParams)cliOptions.getParams();
        Assert.assertFalse(params.isReferenceAPIRetire());
        Assert.assertFalse(params.isReferenceAPIDeprecate());
    }

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-s", "prod", "-a", "/api/v1/to/be/upgraded", "-refAPIId", "123456", "-refAPIName", "myRefOldAPI", "-refAPIVersion", "1.2.3", "-refAPIOrg", "RefOrg", "-refAPIDeprecate", "true", "-refAPIRetire", "true", "-refAPIRetireDate", "31.12.2027"};
        CLIOptions options = CLIAPIUpgradeAccessOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-refAPIId 123456"));
    }

}
