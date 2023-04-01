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
    public void addOrMergeRestriction() throws IOException {
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
        TypeReference<List<QuotaRestriction>> quotaRestrictionTypeRef = new TypeReference<List<QuotaRestriction>>() {};
        List<QuotaRestriction> actualQuotaRestriction =  objectMapper.readValue(actualQuota, quotaRestrictionTypeRef);
        List<QuotaRestriction> desiredQuotaRestriction =  objectMapper.readValue(desiredQuota, quotaRestrictionTypeRef);
        APIQuotaManager apiQuotaManager = new APIQuotaManager(null, null);
        List<QuotaRestriction> result = apiQuotaManager.addOrMergeRestriction(actualQuotaRestriction, desiredQuotaRestriction);
        Assert.assertEquals(5, result.size());
    }
}
