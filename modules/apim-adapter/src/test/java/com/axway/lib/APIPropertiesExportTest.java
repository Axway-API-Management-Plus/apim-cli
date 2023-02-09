package com.axway.lib;

import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.StandardImportParams;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class APIPropertiesExportTest {

    @Test
    public void testStore() throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String uuid = UUID.randomUUID().toString();
        StandardImportParams coreParameters = new StandardImportParams();
        coreParameters.setDetailsExportFile(tempDir + "/test.properties");
        coreParameters.setConfig("config.json");
        APIPropertiesExport apiPropertiesExport = APIPropertiesExport.getInstance();
        apiPropertiesExport.setProperty("feApiId", uuid);
        apiPropertiesExport.store();
        Properties properties = new Properties();
        properties.load(new FileInputStream(tempDir + "/test.properties"));
        Assert.assertEquals(uuid, properties.getProperty("feApiId"));
    }
}
