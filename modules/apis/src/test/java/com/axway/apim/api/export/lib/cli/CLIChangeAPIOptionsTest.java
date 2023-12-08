package com.axway.apim.api.export.lib.cli;

import com.axway.apim.api.export.lib.params.APIChangeParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CLIChangeAPIOptionsTest {

    @Test
    public void testChangeAPIParameters() throws AppException {
        String[] args = {"-s", "prod", "-a", "/api/v1/greet", "-newBackend", "http://my.new.backend", "-oldBackend", "http://my.old.backend"};
        CLIOptions options = CLIChangeAPIOptions.create(args);
        APIChangeParams params = (APIChangeParams) options.getParams();
        // Validate core parameters are included
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        // Validate wide is is using standard as default
        Assert.assertEquals(params.getWide(), StandardExportParams.Wide.standard);
        // Validate the output-format is Console as the default
        Assert.assertEquals(params.getOutputFormat(), StandardExportParams.OutputFormat.console);

        // Validate an API-Filter parameters are included
        Assert.assertEquals(params.getApiPath(), "/api/v1/greet");

        // Validate the change parameters are included
        Assert.assertEquals(params.getNewBackend(), "http://my.new.backend");
        Assert.assertEquals(params.getOldBackend(), "http://my.old.backend");
    }

}
