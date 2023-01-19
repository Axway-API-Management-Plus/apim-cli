package com.axway.apim;

import com.axway.apim.api.export.impl.WiremockTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class APIImportAppTest extends WiremockTest {

    @Test
    public void importApiTest() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String specFile = classLoader.getResource("com/axway/apim/api/definition/petstore-openapi30.json").getFile();
        String confFile = classLoader.getResource("com/axway/apim/test/files/basic/config.json").getFile();
        String[] args = {"-h", "localhost", "-c", confFile, "-a", specFile};
        int returnCode = APIImportApp.importAPI(args);
        Assert.assertEquals(returnCode, 0);
    }
}
