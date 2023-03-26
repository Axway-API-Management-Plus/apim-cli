package com.axway.apim.setup;

import com.axway.apim.WiremockWrapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        String[] args = {"-h", "localhost", "-type", "config,alerts,remotehosts"};
        int returnCode = APIManagerSettingsApp.exportConfig(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void exportConfigJson() {
        String[] args = {"-h", "localhost", "-type", "config,alerts,remotehosts,globalquotas", "-o", "json", "-deleteTarget"};
        int returnCode = APIManagerSettingsApp.exportConfig(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void exportConfigYaml() {
        String[] args = {"-h", "localhost", "-type", "config,alerts,remotehosts,globalquotas", "-o", "yaml", "-deleteTarget"};
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

    @Test
    public void importRemoteHosts() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String configFile = classLoader.getResource("com/axway/apim/setup/adapter/two-remotehosts-config.json").getFile();
        String[] args = {"-h", "localhost", "-c", configFile, "-type", "remotehosts"};
        int returnCode = APIManagerSettingsApp.importConfig(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void importGlobalQuotas() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String configFile = classLoader.getResource("com/axway/apim/setup/adapter/global-quotas.json").getFile();
        String[] args = {"-h", "localhost", "-c", configFile, "-type", "globalquotas"};
        int returnCode = APIManagerSettingsApp.importConfig(args);
        Assert.assertEquals(returnCode, 0);
    }
}
