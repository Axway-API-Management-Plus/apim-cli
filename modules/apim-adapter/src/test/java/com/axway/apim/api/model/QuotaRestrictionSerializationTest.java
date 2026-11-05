package com.axway.apim.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The constraint ID assigned by the API-Manager must never be written anywhere: it is ignored when a restriction is
 * created, it is taken from the URL when a restriction is updated, and it must not end up in an exported config file.
 * <p>
 * QuotaRestriction is serialized through four independent filter configurations across the modules, two of which do
 * not use QuotaRestrictionSerializer at all, so every one of them is asserted here.
 */
public class QuotaRestrictionSerializationTest {

    private static QuotaRestriction restrictionWithId() {
        QuotaRestriction restriction = new QuotaRestriction();
        restriction.setId("c0000000-0000-0000-0000-000000000001");
        restriction.setApiId("9170e76e-ee7a-45ae-8afe-32a32a5da65c");
        restriction.setMethod("*");
        restriction.setType(QuotaRestrictionType.throttle);
        Map<String, String> config = new LinkedHashMap<>();
        config.put("period", "day");
        config.put("per", "1");
        config.put("messages", "1000");
        restriction.setConfig(config);
        return restriction;
    }

    private static String serializeWith(SimpleBeanPropertyFilter filter) throws Exception {
        FilterProvider filterProvider = new SimpleFilterProvider().setDefaultFilter(filter);
        return new ObjectMapper().writer(filterProvider).writeValueAsString(restrictionWithId());
    }

    /**
     * APIManagerQuotaAdapter when writing to the API-Manager.
     */
    @Test
    public void idIsNotSerializedForTheApiManagerRequest() throws Exception {
        String json = serializeWith(SimpleBeanPropertyFilter.serializeAllExcept("apiId", "apiName", "apiVersion", "apiPath", "vhost", "queryVersion"));
        Assert.assertFalse(json.contains("\"id\""), json);
        Assert.assertTrue(json.contains("\"api\""), json);
    }

    /**
     * JsonAPIManagerSetupExporter, which uses the default bean serializer.
     */
    @Test
    public void idIsNotSerializedForTheSettingsExport() throws Exception {
        String json = serializeWith(SimpleBeanPropertyFilter.serializeAllExcept("apiId"));
        Assert.assertFalse(json.contains("\"id\""), json);
    }

    /**
     * ExportHelper, which serializes everything that is not explicitly excluded.
     */
    @Test
    public void idIsNotSerializedForTheApiExport() throws Exception {
        String json = serializeWith(SimpleBeanPropertyFilter.serializeAllExcept());
        Assert.assertFalse(json.contains("\"id\""), json);
    }

    /**
     * JsonApplicationExporter.
     */
    @Test
    public void idIsNotSerializedForTheApplicationExport() throws Exception {
        String json = serializeWith(SimpleBeanPropertyFilter.serializeAllExcept("api", "apiId"));
        Assert.assertFalse(json.contains("\"id\""), json);
    }

    /**
     * Two restrictions differing only by the constraint ID must stay equal, as the desired state read from a config
     * file never carries one. Otherwise every API would be reported as changed on every run.
     */
    @Test
    public void idIsNotPartOfEqualsAndHashCode() {
        QuotaRestriction withId = restrictionWithId();
        QuotaRestriction withoutId = restrictionWithId();
        withoutId.setId(null);
        Assert.assertEquals(withId, withoutId);
        Assert.assertEquals(withId.hashCode(), withoutId.hashCode());
    }
}
