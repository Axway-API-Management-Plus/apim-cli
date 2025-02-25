package com.axway.apim;

import com.axway.apim.api.API;
import com.axway.apim.apiimport.APIImportConfigAdapter;
import com.axway.apim.apiimport.lib.params.APIImportParams;
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
        Assert.assertEquals(returnCode, 15);
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

    @Test
    public void testEmptyPassword() throws Exception {
        APIImportParams params = new APIImportParams();
        ClassLoader classLoader = this.getClass().getClassLoader();
        String confFile = classLoader.getResource("com/axway/apim/test/files/serviceprofile/empty_password.json").getFile();
        params.setConfig(confFile);
        params.setUsername("admin");
        params.setPassword("admin");
        params.setAPIManagerURL("https://localhost:8075");
        APIImportConfigAdapter configAdapter = new APIImportConfigAdapter(params);
        // Creates an API-Representation of the desired API
        API desiredAPI = configAdapter.getDesiredAPI();
        Assert.assertEquals("",desiredAPI.getAuthenticationProfiles().get(0).getParameters().get("password"));
//        ObjectMapper mapper = new ObjectMapper();
//
//        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(desiredAPI.getAuthenticationProfiles()));
    }
}
