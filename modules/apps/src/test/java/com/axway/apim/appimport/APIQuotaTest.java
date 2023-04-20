package com.axway.apim.appimport;

import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class APIQuotaTest {

    String desiredRestriction = "[\n" +
        "  {\n" +
        "    \"api\": \"*\",\n" +
        "    \"method\": \"*\",\n" +
        "    \"type\": \"throttle\",\n" +
        "    \"config\": {\n" +
        "      \"messages\": \"1000\",\n" +
        "      \"period\": \"hour\",\n" +
        "      \"per\": \"1\"\n" +
        "    }\n" +
        "  },\n" +
        "  {\n" +
        "    \"api\": \"myAPI\",\n" +
        "    \"method\": \"*\",\n" +
        "    \"type\": \"throttle\",\n" +
        "    \"config\": {\n" +
        "      \"messages\": \"1000\",\n" +
        "      \"period\": \"hour\",\n" +
        "      \"per\": \"1\"\n" +
        "    }\n" +
        "  }\n" +
        "]";


    @Test
    public void compareAppQuotaNotEquals() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<QuotaRestriction>> typeReference = new TypeReference<List<QuotaRestriction>>() {
        };
        List<QuotaRestriction> quotaRestrictionList = objectMapper.readValue(desiredRestriction, typeReference);

        APIQuota desiredQuota = new APIQuota();
        desiredQuota.setRestrictions(quotaRestrictionList);

        String actualRestriction = "[\n" +
            "  {\n" +
            "    \"api\": \"*\",\n" +
            "    \"method\": \"*\",\n" +
            "    \"type\": \"throttle\",\n" +
            "    \"config\": {\n" +
            "      \"messages\": \"1000\",\n" +
            "      \"period\": \"hour\",\n" +
            "      \"per\": \"1\"\n" +
            "    }\n" +
            "  }\n" +
            "]\n";

        List<QuotaRestriction> AcutalQuotaRestrictionList = objectMapper.readValue(actualRestriction, typeReference);
        APIQuota actualQuota = new APIQuota();
        actualQuota.setRestrictions(AcutalQuotaRestrictionList);
        Assert.assertNotEquals(desiredQuota, actualQuota);

    }

    @Test
    public void compareAppQuotaEquals() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<QuotaRestriction>> typeReference = new TypeReference<List<QuotaRestriction>>() {
        };
        List<QuotaRestriction> quotaRestrictionList = objectMapper.readValue(desiredRestriction, typeReference);

        APIQuota desiredQuota = new APIQuota();
        desiredQuota.setRestrictions(quotaRestrictionList);

        List<QuotaRestriction> AcutalQuotaRestrictionList = objectMapper.readValue(desiredRestriction, typeReference);
        APIQuota actualQuota = new APIQuota();
        actualQuota.setRestrictions(AcutalQuotaRestrictionList);
        Assert.assertEquals(desiredQuota, actualQuota);

    }
}
