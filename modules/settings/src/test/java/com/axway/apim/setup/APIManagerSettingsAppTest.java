package com.axway.apim.setup;

import com.axway.apim.WiremockWrapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class APIManagerSettingsAppTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void exportConfig() {
        String[] args = {"-h", "localhost"};
        int returnCode = APIManagerSettingsApp.exportConfig(args);
        Assert.assertEquals(returnCode, 0);
    }
    @Test
    public void importConfig() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String configFile = classLoader.getResource("com/axway/apim/setup/adapter/apimanager-config.json").getFile();
        String[] args = {"-h", "localhost", "-c", configFile};
        int returnCode = APIManagerSettingsApp.importConfig(args);
        Assert.assertEquals(returnCode, 0);
    }
}