package com.axway.apim.appimport.lib;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appimport.ClientAppImportManager;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ClientAppImportManagerTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("apiadmin");
        coreParameters.setPassword(Utils.getEncryptedPassword());

    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "No changes detected between Desired- and Actual-App\\.")
    public void replicateNoChangeApplication() throws JsonProcessingException {
        String applicationRequest = "{\n" +
            "  \"name\": \"TestApp\",\n" +
            "  \"organization\": \"orga\",\n" +
            "  \"state\": \"approved\",\n" +
            "  \"enabled\": true,\n" +
            "  \"email\": \"user@domain.com\",\n" +
            "  \"phone\": \"654654646234\",\n" +
            "  \"customProperties\":{\n" +
            "    \"publicApp\":true\n" +
            "  }\n" +
            "}";
        ObjectMapper objectMapper = new ObjectMapper();
        ClientApplication clientApplication = objectMapper.readValue(applicationRequest, ClientApplication.class);
        ClientAppImportManager clientAppImportManager = new ClientAppImportManager();
        clientAppImportManager.setActualApp(clientApplication);
        clientAppImportManager.setDesiredApp(clientApplication);
        clientAppImportManager.replicate();
    }
}
