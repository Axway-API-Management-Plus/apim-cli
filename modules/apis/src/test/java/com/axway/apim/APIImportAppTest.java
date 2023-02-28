package com.axway.apim;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIImportAppTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void importApiTest() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String specFile = classLoader.getResource("api_definition_1/petstore-openapi30.json").getFile();
        String confFile = classLoader.getResource("com/axway/apim/test/files/basic/config.json").getFile();
        String[] args = {"-h", "localhost", "-c", confFile, "-a", specFile};
        int returnCode = APIImportApp.importAPI(args);
        Assert.assertEquals(returnCode, 7);
    }

    @Test
    public void importApiMCliHelp() {
        String[] args = {"-help"};
        int returnCode = APIImportApp.importAPI(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void importApiMCliVersion() {
        String[] args = {"-version"};
        int returnCode = APIImportApp.importAPI(args);
        Assert.assertEquals(returnCode, 0);
    }
}
