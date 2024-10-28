package com.axway.apim.apiimport.actions;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class APIQuotaManagerTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void mergeRestriction() throws IOException {
        String actualQuota = "[\n" +
            "  {\n" +
            "    \"api\": \"*\",\n" +
            "    \"method\": \"*\",\n" +
            "    \"type\": \"throttle\",\n" +
            "    \"config\": {\n" +
            "      \"messages\": \"1000\",\n" +
            "      \"period\": \"second\",\n" +
            "      \"per\": \"2\"\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"api\": \"19a7dd22-1dbc-415e-9ba5-a43e7d863ba8\",\n" +
            "    \"method\": \"*\",\n" +
            "    \"type\": \"throttle\",\n" +
            "    \"config\": {\n" +
            "      \"messages\": \"1000\",\n" +
            "      \"period\": \"second\",\n" +
            "      \"per\": \"2\"\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"api\": \"4f52e8a2-cba1-4645-8088-2ed98f5ea57e\",\n" +
            "    \"method\": \"*\",\n" +
            "    \"type\": \"throttle\",\n" +
            "    \"config\": {\n" +
            "      \"messages\": \"1000\",\n" +
            "      \"period\": \"second\",\n" +
            "      \"per\": \"2\"\n" +
            "    }\n" +
            "  }\n" +
            "]";

        String desiredQuota = "[\n" +
            "      {\n" +
            "        \"api\": \"4f52e8a2-cba1-4645-8088-2ed98f5ea57e\",\n" +
            "        \"method\": \"*\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"8\",\n" +
            "          \"messages\": \"1000\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"api\": \"*\",\n" +
            "        \"method\": \"*\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"5\",\n" +
            "          \"messages\": \"1000\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<>() {
        };
        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        Assert.assertEquals(3, result.size());
    }


    @Test
    public void mergeRestrictionActual() throws IOException {
        String actualQuota = "[\n" +
            "      {\n" +
            "        \"api\": \"4f52e8a2-cba1-4645-8088-2ed98f5ea57e\",\n" +
            "        \"method\": \"method1\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"8\",\n" +
            "          \"messages\": \"1000\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"api\": \"*\",\n" +
            "        \"method\": \"method2\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"5\",\n" +
            "          \"messages\": \"1000\"\n" +
            "        }\n" +
            "  },\n" +
            "  {\n" +
            "    \"api\": \"4f52e8a2-cba1-4645-8088-2ed98f5ea57e\",\n" +
            "    \"method\": \"*\",\n" +
            "    \"type\": \"throttle\",\n" +
            "    \"config\": {\n" +
            "      \"messages\": \"1000\",\n" +
            "      \"period\": \"second\",\n" +
            "      \"per\": \"2\"\n" +
            "    }\n" +
            "  }\n" +
            "]";

        String desiredQuota = "[\n" +
            "      {\n" +
            "        \"api\": \"4f52e8a2-cba1-4645-8088-2ed98f5ea57e\",\n" +
            "        \"method\": \"method1\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"8\",\n" +
            "          \"messages\": \"1000\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"api\": \"method2\",\n" +
            "        \"method\": \"*\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"5\",\n" +
            "          \"messages\": \"1000\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<>() {
        };
        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void mergeRestrictionDesired() throws IOException {
        String actualQuota = "[\n" +
            "      {\n" +
            "        \"method\": \"PATCH /{txid}\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"656\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"OPTIONS /{txid}\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"750\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";

        String desiredQuota = "[\n" +
            "      {\n" +
            "        \"method\": \"PATCH /{txid}\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"656\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"OPTIONS /{txid}\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"750\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"POST /{txid}\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"887\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<>() {
        };
        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        Assert.assertEquals(3, result.size());
    }


    @Test
    public void mergeRestrictionDesiredWithNoActual() throws IOException {
        String actualQuota = "[]";

        String desiredQuota = "[\n" +
            "      {\n" +
            "        \"method\": \"PATCH /{txid}\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"656\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"OPTIONS /{txid}\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"750\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"POST /{txid}\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"887\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<>() {
        };
        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        Assert.assertEquals(3, result.size());
    }


    @Test
    public void populateMethodIdForNewApi() throws JsonProcessingException {

        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());

        String desiredQuota = "[\n" +
            "      {\n" +
            "        \"method\": \"addPet\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"656\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"deleteUser\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"750\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"getUserByName\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"887\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<>() {
        };
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        API createdAPI = new API();
        APIQuota apiQuota = new APIQuota();
        apiQuota.setRestrictions(desiredQuotaRestriction);
        createdAPI.setSystemQuota(apiQuota);
        createdAPI.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        apiQuotaManager.populateMethodId(createdAPI, desiredQuotaRestriction);

        Assert.assertEquals(createdAPI.getSystemQuota().getRestrictions().get(0).getMethod(), "dfd61eee-cf2d-4d97-bbb6-f602fd2063bd");


    }

    @Test
    public void populateMethodIdForExistingApi() throws JsonProcessingException {

        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());

        String actualQuota = "[\n" +
            "      {\n" +
            "        \"method\": \"addPet\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"656\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"deleteUser\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"750\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"getUserByName\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"887\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";

        String desiredQuota = "[\n" +
            "      {\n" +
            "        \"method\": \"addPet\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"656\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"deleteUser\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"750\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"getUserByName\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"887\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<>() {
        };
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        API createdAPI = new API();
        APIQuota apiQuota = new APIQuota();
        apiQuota.setRestrictions(desiredQuotaRestriction);
        createdAPI.setSystemQuota(apiQuota);
        createdAPI.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");


        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        API actualAPI = new API();
        APIQuota apiQuotaActualApi = new APIQuota();

        apiQuotaActualApi.setRestrictions(actualQuotaRestriction);
        actualAPI.setSystemQuota(apiQuotaActualApi);
        actualAPI.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");

        APIQuotaManager apiQuotaManager = new APIQuotaManager(createdAPI, actualAPI);
        apiQuotaManager.populateMethodId(createdAPI, desiredQuotaRestriction);

        Assert.assertEquals(createdAPI.getSystemQuota().getRestrictions().get(0).getMethod(), "dfd61eee-cf2d-4d97-bbb6-f602fd2063bd");
        Assert.assertEquals(actualAPI.getSystemQuota().getRestrictions().get(0).getMethod(), "addPet");
    }


    @Test
    public void populateMethodIdForExistingApiWithQuotaChange() throws JsonProcessingException {

        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());

        String actualQuota = "[\n" +
            "      {\n" +
            "        \"method\": \"deleteUser\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"750\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"getUserByName\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"887\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";

        String desiredQuota = "[\n" +
            "      {\n" +
            "        \"method\": \"addPet\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"656\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"deleteUser\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"750\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"method\": \"getUserByName\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"887\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<>() {
        };
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        API createdAPI = new API();
        APIQuota apiQuota = new APIQuota();
        apiQuota.setRestrictions(desiredQuotaRestriction);
        createdAPI.setSystemQuota(apiQuota);
        createdAPI.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");


        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        API actualAPI = new API();
        APIQuota apiQuotaActualApi = new APIQuota();

        apiQuotaActualApi.setRestrictions(actualQuotaRestriction);
        actualAPI.setSystemQuota(apiQuotaActualApi);
        actualAPI.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");

        APIQuotaManager apiQuotaManager = new APIQuotaManager(createdAPI, actualAPI);
        apiQuotaManager.populateMethodId(createdAPI, desiredQuotaRestriction);

        Assert.assertEquals(createdAPI.getSystemQuota().getRestrictions().get(0).getMethod(), "dfd61eee-cf2d-4d97-bbb6-f602fd2063bd");
        Assert.assertEquals(actualAPI.getSystemQuota().getRestrictions().get(0).getMethod(), "deleteUser");
    }


    @Test
    public void populateMethodAllWithQuotaChange() throws JsonProcessingException {

        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());
      //  CoreParameters.getInstance().setQuotaMode(CoreParameters.Mode.replace);

        String actualQuota = "[\n" +
            "      {\n" +
            "        \"api\": \"a451eaf0-8adb-44f7-9b1b-c9675f35e9a3\",\n" +
            "        \"method\": \"*\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"750\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";

        String desiredQuota = "[\n" +
            "      {\n" +
            "        \"method\": \"*\",\n" +
            "        \"type\": \"throttle\",\n" +
            "        \"config\": {\n" +
            "          \"period\": \"second\",\n" +
            "          \"per\": \"1\",\n" +
            "          \"messages\": \"656\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]";
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<>() {
        };
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        API createdAPI = new API();
        APIQuota apiQuota = new APIQuota();
        apiQuota.setRestrictions(desiredQuotaRestriction);
        createdAPI.setSystemQuota(apiQuota);
        createdAPI.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");


        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        API actualAPI = new API();
        APIQuota apiQuotaActualApi = new APIQuota();

        apiQuotaActualApi.setRestrictions(actualQuotaRestriction);
        actualAPI.setSystemQuota(apiQuotaActualApi);
        actualAPI.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");

        APIQuotaManager apiQuotaManager = new APIQuotaManager(createdAPI, actualAPI);
        List<QuotaRestriction> mergedQuotaRestriction = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        System.out.println(mergedQuotaRestriction);
        Assert.assertEquals(mergedQuotaRestriction.get(0).getConfig().get("messages"), "656");
    }
}
