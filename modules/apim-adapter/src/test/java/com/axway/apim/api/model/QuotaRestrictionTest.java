package com.axway.apim.api.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class QuotaRestrictionTest {

    @Test
    public void compareTwoRestrictions(){
        QuotaRestriction quotaRestrictionActual = new QuotaRestriction();
        quotaRestrictionActual.setApiId("*");
        quotaRestrictionActual.setMethod("*");
        quotaRestrictionActual.setType(QuotaRestrictionType.throttle);
        Map<String, String> config = new HashMap<>();
        config.put("period","day");
        config.put("per","1");
        config.put("messages","1000");
        quotaRestrictionActual.setConfig(config);
        QuotaRestriction quotaRestrictionDesired = new QuotaRestriction();
        quotaRestrictionDesired.setApiId("*");
        quotaRestrictionDesired.setMethod("*");
        quotaRestrictionDesired.setType(QuotaRestrictionType.throttle);
        config = new HashMap<>();
        config.put("period","day");
        config.put("per","1");
        config.put("messages","1000");
        quotaRestrictionDesired.setConfig(config);
        Assert.assertEquals(quotaRestrictionDesired, quotaRestrictionActual);

    }
}
