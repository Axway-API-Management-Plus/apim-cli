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
import java.util.Set;
import java.util.stream.Collectors;

public class APIQuotaManagerTest extends WiremockWrapper {

    @BeforeClass
    @Override
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    @Override
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
        // The existing restrictions (per=2) are not part of the desired state, which only contains the restrictions with per=8 and per=5.
        // As period and per are part of the identity of a restriction, they are replaced and must not be merged into each other (issue #592).
        Assert.assertEquals(result.size(), 2, "Result: " + result);
        Assert.assertEquals(result.stream().map(restriction -> restriction.getConfig().get("per")).collect(Collectors.toSet()), Set.of("8", "5"));
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
        Assert.assertEquals(result.size(), 2);
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
        Assert.assertEquals(result.size(), 3);
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
        Assert.assertEquals(result.size(), 3);
    }

    @Test
    public void mergeRestrictionWithSameMethodAndDifferentTypes() throws IOException {
        String actualQuota = "[\n" +
            "  {\n" +
            "    \"api\": \"api-1\",\n" +
            "    \"method\": \"GET /orders\",\n" +
            "    \"type\": \"throttle\",\n" +
            "    \"config\": {\n" +
            "      \"messages\": \"100\",\n" +
            "      \"period\": \"minute\",\n" +
            "      \"per\": \"1\"\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"api\": \"api-1\",\n" +
            "    \"method\": \"GET /orders\",\n" +
            "    \"type\": \"throttlemb\",\n" +
            "    \"config\": {\n" +
            "      \"mb\": \"20\",\n" +
            "      \"period\": \"day\",\n" +
            "      \"per\": \"1\"\n" +
            "    }\n" +
            "  }\n" +
            "]";

        String desiredQuota = "[\n" +
            "  {\n" +
            "    \"api\": \"api-1\",\n" +
            "    \"method\": \"GET /orders\",\n" +
            "    \"type\": \"throttle\",\n" +
            "    \"config\": {\n" +
            "      \"messages\": \"250\",\n" +
            "      \"period\": \"minute\",\n" +
            "      \"per\": \"1\"\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"api\": \"api-1\",\n" +
            "    \"method\": \"GET /orders\",\n" +
            "    \"type\": \"throttlemb\",\n" +
            "    \"config\": {\n" +
            "      \"mb\": \"42\",\n" +
            "      \"period\": \"day\",\n" +
            "      \"per\": \"1\"\n" +
            "    }\n" +
            "  }\n" +
            "]";

        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<>() {
        };
        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);

        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);

        Assert.assertEquals(result.size(), 2);
        QuotaRestriction throttle = result.stream()
            .filter(restriction -> "GET /orders".equals(restriction.getMethod()) && "throttle".equals(restriction.getType().name()))
            .findFirst()
            .orElse(null);
        QuotaRestriction throttleMb = result.stream()
            .filter(restriction -> "GET /orders".equals(restriction.getMethod()) && "throttlemb".equals(restriction.getType().name()))
            .findFirst()
            .orElse(null);

        Assert.assertNotNull(throttle);
        Assert.assertNotNull(throttleMb);
        Assert.assertEquals(throttle.getConfig().get("messages"), "250");
        Assert.assertEquals(throttleMb.getConfig().get("mb"), "42");
    }


    private List<QuotaRestriction> restrictions(String... restrictions) throws JsonProcessingException {
        return objectMapper.readValue("[" + String.join(",", restrictions) + "]", new TypeReference<List<QuotaRestriction>>() {
        });
    }

    private String throttle(String method, String messages, String period, String per) {
        return "{\"api\":\"api-1\",\"method\":\"" + method + "\",\"type\":\"throttle\",\"config\":{\"messages\":\"" + messages + "\",\"period\":\"" + period + "\",\"per\":\"" + per + "\"}}";
    }

    private String throttleMb(String method, String mb, String period, String per) {
        return "{\"api\":\"api-1\",\"method\":\"" + method + "\",\"type\":\"throttlemb\",\"config\":{\"mb\":\"" + mb + "\",\"period\":\"" + period + "\",\"per\":\"" + per + "\"}}";
    }

    private QuotaRestriction find(List<QuotaRestriction> restrictions, String type, String period) {
        return restrictions.stream()
            .filter(restriction -> restriction.getType().name().equals(type) && period.equals(restriction.getConfig().get("period")))
            .findFirst().orElse(null);
    }

    /**
     * Issue #592: Both restrictions are applied to all methods. Only the message limit is changed.
     */
    @Test
    public void mergeRestrictionOfAllMethodsOnlyChangesTheChangedRestriction() throws IOException {
        List<QuotaRestriction> actual = restrictions(throttle("*", "10000", "day", "1"), throttleMb("*", "100", "minute", "1"));
        List<QuotaRestriction> desired = restrictions(throttle("*", "20000", "day", "1"), throttleMb("*", "100", "minute", "1"));

        List<QuotaRestriction> result = new APIQuotaManager(null, null).mergeRestriction(actual, desired);

        Assert.assertEquals(result.size(), 2, "Result: " + result);
        QuotaRestriction throttle = find(result, "throttle", "day");
        QuotaRestriction throttleMb = find(result, "throttlemb", "minute");
        Assert.assertNotNull(throttle, "The throttle restriction must not get lost. Result: " + result);
        Assert.assertNotNull(throttleMb, "Result: " + result);
        Assert.assertEquals(throttle.getConfig().get("messages"), "20000");
        Assert.assertNull(throttle.getConfig().get("mb"), "Config of the other type must not be mixed in");
        Assert.assertEquals(throttleMb.getConfig().get("mb"), "100");
        Assert.assertNull(throttleMb.getConfig().get("messages"), "Config of the other type must not be mixed in");
    }

    /**
     * Several restrictions of the same type for the same method, which only differ in period/per (e.g. 100 per second and 10000 per day).
     */
    @Test
    public void mergeRestrictionWithSameMethodAndSameTypeAndDifferentPeriods() throws IOException {
        List<QuotaRestriction> actual = restrictions(throttle("*", "100", "second", "1"), throttle("*", "10000", "day", "1"));
        List<QuotaRestriction> desired = restrictions(throttle("*", "100", "second", "1"), throttle("*", "20000", "day", "1"));

        List<QuotaRestriction> result = new APIQuotaManager(null, null).mergeRestriction(actual, desired);

        Assert.assertEquals(result.size(), 2, "Result: " + result);
        Assert.assertEquals(find(result, "throttle", "second").getConfig().get("messages"), "100");
        Assert.assertEquals(find(result, "throttle", "day").getConfig().get("messages"), "20000");
    }

    @Test
    public void mergeRestrictionWithChangedPeriodReplacesTheRestriction() throws IOException {
        List<QuotaRestriction> actual = restrictions(throttle("*", "100", "minute", "1"), throttleMb("*", "100", "minute", "1"));
        List<QuotaRestriction> desired = restrictions(throttle("*", "100", "second", "1"), throttleMb("*", "100", "minute", "1"));

        List<QuotaRestriction> result = new APIQuotaManager(null, null).mergeRestriction(actual, desired);

        Assert.assertEquals(result.size(), 2, "Result: " + result);
        Assert.assertNull(find(result, "throttle", "minute"), "The old restriction must be removed. Result: " + result);
        Assert.assertEquals(find(result, "throttle", "second").getConfig().get("messages"), "100");
        Assert.assertEquals(find(result, "throttlemb", "minute").getConfig().get("mb"), "100");
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
            "        \"api\": \"*\",\n" +
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
