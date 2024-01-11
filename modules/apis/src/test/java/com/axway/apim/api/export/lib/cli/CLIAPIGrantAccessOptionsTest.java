package com.axway.apim.api.export.lib.cli;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.export.lib.params.APIGrantAccessParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CLIAPIGrantAccessOptionsTest {
    @Test
    public void testGrantAccessAPIParameters() throws AppException {
        String[] args = {"-s", "prod", "-a", "/api/v1/some", "-orgName", "OrgName", "-orgId", "OrgId", "-n", "MyAPIName", "-org", "MyAPIOrg", "-id", "MY-API-ID", "-vhost", "api.chost.com", "-backend", "backend.host",
            "-policy", "PolicyName", "-inboundsecurity", "api-key", "-tag", "tagGroup=*myTagValue*"};
        CLIOptions cliOptions = CLIAPIGrantAccessOptions.create(args);
        APIGrantAccessParams params = (APIGrantAccessParams)cliOptions.getParams();

        // Validate core parameters are included
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        // Validate an API-Filter parameters are included
        Assert.assertEquals(params.getApiPath(), "/api/v1/some");
        Assert.assertEquals(params.getName(), "MyAPIName");
        Assert.assertEquals(params.getBackend(), "backend.host");
        Assert.assertEquals(params.getPolicy(), "PolicyName");
        Assert.assertEquals(params.getVhost(), "api.chost.com");
        Assert.assertEquals(params.getInboundSecurity(), "api-key");
        Assert.assertEquals(params.getId(), "MY-API-ID");
        Assert.assertEquals(params.getTag(), "tagGroup=*myTagValue*");

        // Validate Grant-Access params are included
        Assert.assertEquals(params.getOrgId(), "OrgId");
        Assert.assertEquals(params.getOrgName(), "OrgName");

        APIFilter apiFilter = params.getAPIFilter();
        Assert.assertEquals(apiFilter.getState(), "published"); // Must be published as only published APIs can be considered for grant access
        Assert.assertEquals(apiFilter.getApiPath(), "/api/v1/some");
        Assert.assertEquals(apiFilter.getName(), "MyAPIName");
        Assert.assertEquals(apiFilter.getBackendBasepath(), "backend.host");
        Assert.assertEquals(apiFilter.getPolicyName(), "PolicyName");
        Assert.assertEquals(apiFilter.getVhost(), "api.chost.com");
        Assert.assertEquals(apiFilter.getInboundSecurity(), "api-key");
        Assert.assertEquals(apiFilter.getTag(), "tagGroup=*myTagValue*");
    }

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"-s", "prod", "-a", "/api/v1/some", "-orgName", "OrgName", "-orgId", "OrgId", "-n", "MyAPIName", "-org", "MyAPIOrg", "-id", "MY-API-ID", "-vhost", "api.chost.com", "-backend", "backend.host",
            "-policy", "PolicyName", "-inboundsecurity", "api-key", "-tag", "tagGroup=*myTagValue*"};
        CLIOptions options = CLIAPIGrantAccessOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("-a /api/v1/some"));
    }
}
