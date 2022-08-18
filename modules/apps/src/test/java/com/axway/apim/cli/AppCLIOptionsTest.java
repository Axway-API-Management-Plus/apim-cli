package com.axway.apim.cli;

import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.appimport.lib.AppImportCLIOptions;
import com.axway.apim.appimport.lib.AppImportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AppCLIOptionsTest {
    private String apimCliHome;

    @BeforeClass
    private void init() throws IOException {
        apimCliHome = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + "apimcli";
        String confPath = String.valueOf(Files.createDirectories(Paths.get(apimCliHome + "/conf")).toAbsolutePath());
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
             OutputStream outputStream = Files.newOutputStream(new File(confPath, "env.properties").toPath())) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    @Test
    public void testAppImportParameters() throws AppException {
        String[] args = {"-s", "prod", "-c", "myAppConfig.json", "-stageConfig", "myStageConfig.json", "-apimCLIHome", apimCliHome};
        CLIOptions options = AppImportCLIOptions.create(args);
        AppImportParams params = (AppImportParams) options.getParams();
        // Validate core parameters are included
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        // Validate App-Import parameters
        Assert.assertEquals(params.getConfig(), "myAppConfig.json");
        Assert.assertEquals(params.getStageConfig(), "myStageConfig.json");
        Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
    }

    @Test
    public void testExportApplicationParameters() throws AppException {
        String[] args = {"-s", "prod", "-n", "*My Great App*", "-id", "UUID-ID-OF-THE-APP", "-state", "pending", "-orgName", "*Partners*", "-createdBy", "Tom", "-credential", "*9877979779*", "-redirectUrl", "*localhost*", "-o", "json", "-wide", "-apimCLIHome", apimCliHome};
        CLIOptions options = AppExportCLIOptions.create(args);
        AppExportParams params = (AppExportParams) options.getParams();
        // Validate core parameters are included
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        // Validate standard export parameters are included
        Assert.assertEquals(params.getWide(), Wide.wide);
        Assert.assertEquals(params.getOutputFormat(), OutputFormat.json);

        // Validate App-Import parameters
        Assert.assertEquals(params.getName(), "*My Great App*");
        Assert.assertEquals(params.getId(), "UUID-ID-OF-THE-APP");
        Assert.assertEquals(params.getState(), "pending");
        Assert.assertEquals(params.getOrgName(), "*Partners*");
        Assert.assertEquals(params.getCreatedBy(), "Tom");
        Assert.assertEquals(params.getCredential(), "*9877979779*");
        Assert.assertEquals(params.getRedirectUrl(), "*localhost*");
        Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
    }

}
