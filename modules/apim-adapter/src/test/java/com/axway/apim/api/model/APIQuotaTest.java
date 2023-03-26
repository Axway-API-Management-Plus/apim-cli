package com.axway.apim.api.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIQuotaTest {

    @Test
    public void compareApiQuota(){
        APIQuota apiQuotaActual = new APIQuota();
        apiQuotaActual.setId(null);
        apiQuotaActual.setType(null);

        QuotaRestriction quotaRestrictionActual = new QuotaRestriction();
        quotaRestrictionActual.setApiId("*");
        quotaRestrictionActual.setMethod("*");
        quotaRestrictionActual.setType(QuotaRestrictionType.throttle);
        Map<String, String> config = new HashMap<>();
        config.put("period","day");
        config.put("per","1");
        config.put("messages","1000");
        quotaRestrictionActual.setConfig(config);

        QuotaRestriction quotaRestrictionActual2 = new QuotaRestriction();
        quotaRestrictionActual2.setApiId("44e19b75-82ce-4fdf-8511-69480a4b4dca");
        quotaRestrictionActual2.setMethod("*");
        quotaRestrictionActual2.setType(QuotaRestrictionType.throttle);
        config = new HashMap<>();
        config.put("period","minute");
        config.put("per","1");
        config.put("messages","2000");
        quotaRestrictionActual2.setConfig(config);

        QuotaRestriction quotaRestrictionActual3 = new QuotaRestriction();
        quotaRestrictionActual3.setApiId("44e19b75-82ce-4fdf-8511-69480a4b4dca");
        quotaRestrictionActual3.setMethod("5a5a027d-832d-4a8d-9fc0-29bb1695b1b9");
        quotaRestrictionActual3.setType(QuotaRestrictionType.throttle);
        config = new HashMap<>();
        config.put("period","hour");
        config.put("per","1");
        config.put("messages","3000");
        quotaRestrictionActual3.setConfig(config);


        List<QuotaRestriction> quotaRestrictions = new ArrayList<>();
        quotaRestrictions.add(quotaRestrictionActual);
        quotaRestrictions.add(quotaRestrictionActual2);
        quotaRestrictions.add(quotaRestrictionActual3);
        apiQuotaActual.setRestrictions(quotaRestrictions);

        APIQuota apiQuotaDesired = new APIQuota();
        apiQuotaDesired.setId("99d88055-f8e3-4c90-8f32-1972b009bb60");
        apiQuotaDesired.setType("APPLICATION");

        QuotaRestriction quotaRestrictionDesired = new QuotaRestriction();
        quotaRestrictionDesired.setApiId("*");
        quotaRestrictionDesired.setMethod("*");
        quotaRestrictionDesired.setType(QuotaRestrictionType.throttle);
        config = new HashMap<>();
        config.put("period","day");
        config.put("per","1");
        config.put("messages","1000");
        quotaRestrictionDesired.setConfig(config);


        QuotaRestriction quotaRestrictionDesired2 = new QuotaRestriction();
        quotaRestrictionDesired2.setApiId("44e19b75-82ce-4fdf-8511-69480a4b4dca");
        quotaRestrictionDesired2.setMethod("5a5a027d-832d-4a8d-9fc0-29bb1695b1b9");
        quotaRestrictionDesired2.setType(QuotaRestrictionType.throttle);
        config = new HashMap<>();
        config.put("period","hour");
        config.put("per","1");
        config.put("messages","3000");
        quotaRestrictionDesired2.setConfig(config);
       //api=44e19b75-82ce-4fdf-8511-69480a4b4dca, method=*, type=throttle, config={period=minute, per=1, messages=2000
        QuotaRestriction quotaRestrictionDesired3 = new QuotaRestriction();
        quotaRestrictionDesired3.setApiId("44e19b75-82ce-4fdf-8511-69480a4b4dca");
        quotaRestrictionDesired3.setMethod("*");
        quotaRestrictionDesired3.setType(QuotaRestrictionType.throttle);
        config = new HashMap<>();
        config.put("period","minute");
        config.put("per","1");
        config.put("messages","2000");
        quotaRestrictionDesired3.setConfig(config);




        quotaRestrictions = new ArrayList<>();
        quotaRestrictions.add(quotaRestrictionDesired);
        quotaRestrictions.add(quotaRestrictionDesired2);
        quotaRestrictions.add(quotaRestrictionDesired3);

        apiQuotaDesired.setRestrictions(quotaRestrictions);

        Assert.assertEquals(apiQuotaActual, apiQuotaDesired);


       // 2023-03-24 23:24:14,671 [ClientAppImportManager] INFO : Desired app quota APIQuota [id=null, type=null, restrictions=[QuotaRestriction [api=*, method=*, type=throttle, config={period=day, per=1, messages=1000}], QuotaRestriction [api=44e19b75-82ce-4fdf-8511-69480a4b4dca, method=*, type=throttle, config={period=minute, per=1, messages=2000}], QuotaRestriction [api=44e19b75-82ce-4fdf-8511-69480a4b4dca, method=5a5a027d-832d-4a8d-9fc0-29bb1695b1b9, type=throttle, config={period=hour, per=1, messages=3000}]]]
        //2023-03-24 23:24:14,672 [ClientAppImportManager] INFO : Actual app quota APIQuota [id=99d88055-f8e3-4c90-8f32-1972b009bb60, type=APPLICATION, restrictions=[QuotaRestriction [api=*, method=*, type=throttle, config={period=day, per=1, messages=1000}], QuotaRestriction [api=44e19b75-82ce-4fdf-8511-69480a4b4dca, method=5a5a027d-832d-4a8d-9fc0-29bb1695b1b9, type=throttle, config={period=hour, per=1, messages=3000}], QuotaRestriction [api=44e19b75-82ce-4fdf-8511-69480a4b4dca, method=*, type=throttle, config={period=minute, per=1, messages=2000}]]]

    }
}
