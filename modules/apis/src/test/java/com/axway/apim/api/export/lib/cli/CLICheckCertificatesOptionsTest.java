package com.axway.apim.api.export.lib.cli;

import com.axway.apim.api.export.lib.params.APICheckCertificatesParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CLICheckCertificatesOptionsTest {

    @Test
    public void testCertificateCheckParams() throws AppException {
        String[] args = {"-s", "prod", "-days", "999"};
        CLIOptions options = CLICheckCertificatesOptions.create(args);
        APICheckCertificatesParams params = (APICheckCertificatesParams)options.getParams();
        Assert.assertEquals(params.getNumberOfDays(), 999);
        // Check base parameters to make sure, all parameters up to the root are parsed
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");
    }
}
