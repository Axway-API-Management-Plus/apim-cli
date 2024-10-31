package com.axway.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardImportCLIOptions;
import com.axway.lib.utils.SampleCLIOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StandardImportCLIOptionsTest {

    @Test
    public void testStandardImportCLIOptions() throws Exception {
        String[] args = {"-enabledCaches", "applicationsQuotaCache", "-stageConfig", "my-staged-config.json"};
        CLIOptions options = SampleCLIOptions.create(args);
        StandardImportCLIOptions standardImportCLIOptions = new StandardImportCLIOptions();
        standardImportCLIOptions.addOptions(options);
        options.parse();
        Assert.assertTrue(options.hasOption("enabledCaches"));
        Assert.assertTrue(options.hasOption("stageConfig"));
    }
}
