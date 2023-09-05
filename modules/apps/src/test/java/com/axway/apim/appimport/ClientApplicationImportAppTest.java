package com.axway.apim.appimport;

import com.axway.apim.WiremockWrapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ClientApplicationImportAppTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void importApplication() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String applicationFile = classLoader.getResource("com/axway/apim/appimport/apps/basic/application.json").getFile();
        String[] args = {"-h", "localhost", "-c", applicationFile};
        int returnCode = ClientApplicationImportApp.importApp(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void importApplicationReturnCodeMapping() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String applicationFile = classLoader.getResource("com/axway/apim/appimport/apps/basic/application.json").getFile();
        String[] args = {"-h", "localhost1", "-c", applicationFile, "-returnCodeMapping", "10:0, 25:0"};
        int returnCode = ClientApplicationImportApp.importApp(args);
        Assert.assertEquals(returnCode, 0);
    }

}
