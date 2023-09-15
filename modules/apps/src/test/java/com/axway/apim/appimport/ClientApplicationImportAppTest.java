package com.axway.apim.appimport;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.jackson.AppCredentialsDeserializer;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            String applicationFile = classLoader.getResource("com/axway/apim/appimport/apps/basic/application.json").getFile();
            String[] args = {"-h", "localhost1", "-c", applicationFile, "-returnCodeMapping", "10:0, 25:0"};
            int returnCode = ClientApplicationImportApp.importApp(args);
            Assert.assertEquals(returnCode, 0);
        } finally {
            APIManagerAdapter.deleteInstance();
        }
    }

    @Test
    public void compareAppQuotaNotEqualsWithEmpty() throws JsonProcessingException {
        CoreParameters coreParameters = CoreParameters.getInstance();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("apiadmin");
        coreParameters.setPassword(Utils.getEncryptedPassword());


        String existing = "{\n" +
            "  \"name\": \"Test app\",\n" +
            "  \"state\": \"approved\",\n" +
            "  \"enabled\": true,\n" +
            "  \"organization\": \"orga\",\n" +
            "  \"credentials\": [\n" +
            "  ],\n" +
            "  \"appQuota\": {\n" +
            "\t    \"restrictions\": [\n" +
            "      {  \"api\": \"*\",\n" +
            "        \"method\": \"*\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"messages\": \"1000\",\n" +
            "          \"period\": \"hour\",\n" +
            "          \"per\": \"1\"\n" +
            "        } },\n" +
            "{  \"api\": \"*\",\n" +
            "        \"method\": \"*\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"messages\": \"1000\",\n" +
            "          \"period\": \"hour\",\n" +
            "          \"per\": \"1\"\n" +
            "        } }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule().addDeserializer(ClientAppCredential.class, new AppCredentialsDeserializer()));
        objectMapper.registerModule(new SimpleModule().addDeserializer(QuotaRestriction.class, new QuotaRestrictionDeserializer(QuotaRestrictionDeserializer.DeserializeMode.configFile)));
        ClientApplication existingApp = objectMapper.readValue(existing, ClientApplication.class);



        String actual= "{\n" +
            "  \"name\": \"Test app\",\n" +
            "  \"state\": \"approved\",\n" +
            "  \"enabled\": true,\n" +
            "  \"organization\": \"orga\",\n" +
            "  \"credentials\": [\n" +
            "  ],\n" +
            "  \"appQuota\": {\n" +
            "\t    \"restrictions\": [\n" +
            "    ]\n" +
            "  }\n" +
            "}";

        ClientApplication newApp = objectMapper.readValue(actual, ClientApplication.class);
        Assert.assertFalse(ClientAppImportManager.appsAreEqual(existingApp, newApp));

    }

}
