package com.axway.apim.organization.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.StandardExportParams.OutputFormat;
import com.axway.apim.lib.StandardExportParams.Wide;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.lib.*;
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

public class OrgCLIOptionsTest {

    private String apimCliHome;
    @BeforeClass
    private void init() throws IOException {
        apimCliHome = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + "apimcli";
        String confPath = String.valueOf(Files.createDirectories(Paths.get(apimCliHome + "/conf")).toAbsolutePath());
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
             OutputStream outputStream= Files.newOutputStream(new File(confPath, "env.properties").toPath())){
            IOUtils.copy(inputStream,outputStream );
        }
    }

    @Test
    public void testAppImportParameters() throws AppException {
        String[] args = {"-s", "prod", "-c", "myOrgConfig.json", "-stageConfig", "myStageConfig.json", "-apimCLIHome", apimCliHome};
        CLIOptions options = OrgImportCLIOptions.create(args);
        OrgImportParams params = (OrgImportParams) options.getParams();
        // Validate core parameters are included
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        // Validate App-Import parameters
        Assert.assertEquals(params.getConfig(), "myOrgConfig.json");
        Assert.assertEquals(params.getStageConfig(), "myStageConfig.json");
        Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
    }

    @Test
    public void testExportOrganizationParameters() throws AppException {
        String[] args = {"-s", "prod", "-n", "*My organization*", "-id", "UUID-ID-OF-THE-ORG", "-dev", "true", "-o", "csv", "-ultra", "-apimCLIHome", apimCliHome};
        CLIOptions options = OrgExportCLIOptions.create(args);
        OrgExportParams params = (OrgExportParams) options.getParams();
        // Validate core parameters are included
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        // Validate standard export parameters are included
        Assert.assertEquals(params.getWide(), Wide.ultra);
        Assert.assertEquals(params.getOutputFormat(), OutputFormat.csv);

        // Validate Org-Import parameters
        Assert.assertEquals(params.getName(), "*My organization*");
        Assert.assertEquals(params.getId(), "UUID-ID-OF-THE-ORG");
        Assert.assertEquals(params.getDev(), "true");
        Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
    }

    @Test
    public void testDeleteOrganizationParameters() throws AppException {
        String[] args = {"-s", "prod", "-n", "*My organization*", "-id", "UUID-ID-OF-THE-ORG", "-dev", "true", "-apimCLIHome", apimCliHome};
        CLIOptions options = OrgDeleteCLIOptions.create(args);
        OrgExportParams params = (OrgExportParams) options.getParams();
        // Validate core parameters are included
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");

        // Validate Org-Import parameters
        Assert.assertEquals(params.getName(), "*My organization*");
        Assert.assertEquals(params.getId(), "UUID-ID-OF-THE-ORG");
        Assert.assertEquals(params.getDev(), "true");
        Assert.assertNotNull(params.getProperties(), "Properties should never be null. They must be created as a base or per stage.");
    }

}
