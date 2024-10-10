package com.axway.apim.apiimport.actions;

import com.axway.apim.api.model.QuotaRestriction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class APIQuotaManagerTest {

    @Test
    public void mergeRestriction() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
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
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<List<QuotaRestriction>>() {
        };
        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        Assert.assertEquals(3, result.size());
    }


    @Test
    public void mergeRestrictionActual() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
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
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<List<QuotaRestriction>>() {
        };
        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void mergeRestrictionDesired() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
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
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<List<QuotaRestriction>>() {
        };
        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        Assert.assertEquals(3, result.size());
    }


    @Test
    public void mergeRestrictionDesiredWithNoActual() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
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
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<List<QuotaRestriction>>() {
        };
        List<QuotaRestriction> actualQuotaRestriction = objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction = objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.mergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        Assert.assertEquals(3, result.size());
    }
}
