package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.apis.QuotaConstraintDiff.QuotaConstraintOperations;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictionType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuotaConstraintDiffTest {

    private static final String API_A = "aaaaaaaa-0000-0000-0000-000000000001";
    private static final String API_B = "bbbbbbbb-0000-0000-0000-000000000002";

    private static QuotaRestriction throttle(String id, String apiId, String method, String messages) {
        QuotaRestriction restriction = new QuotaRestriction();
        restriction.setId(id);
        restriction.setApiId(apiId);
        restriction.setMethod(method);
        restriction.setType(QuotaRestrictionType.throttle);
        Map<String, String> config = new LinkedHashMap<>();
        config.put("period", "day");
        config.put("per", "1");
        config.put("messages", messages);
        restriction.setConfig(config);
        return restriction;
    }

    private static QuotaRestriction throttlemb(String id, String apiId, String method, String mb) {
        QuotaRestriction restriction = new QuotaRestriction();
        restriction.setId(id);
        restriction.setApiId(apiId);
        restriction.setMethod(method);
        restriction.setType(QuotaRestrictionType.throttlemb);
        Map<String, String> config = new LinkedHashMap<>();
        config.put("period", "day");
        config.put("per", "1");
        config.put("mb", mb);
        restriction.setConfig(config);
        return restriction;
    }

    @Test
    public void identicalRestrictionsProduceNoOperations() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", API_A, "*", "1000"));
        List<QuotaRestriction> desired = Arrays.asList(throttle(null, API_A, "*", "1000"));
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, desired);
        Assert.assertTrue(operations.isEmpty(), "Expected no operations, but got: " + operations);
    }

    @Test
    public void changedConfigProducesSingleUpdateCarryingTheServerId() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", API_A, "*", "1000"));
        List<QuotaRestriction> desired = Arrays.asList(throttle(null, API_A, "*", "2000"));
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, desired);
        Assert.assertEquals(operations.getToUpdate().size(), 1);
        Assert.assertTrue(operations.getToCreate().isEmpty());
        Assert.assertTrue(operations.getToDelete().isEmpty());
        QuotaRestriction update = operations.getToUpdate().get(0);
        Assert.assertEquals(update.getId(), "id-1", "The update must address the restriction by its server assigned ID");
        Assert.assertEquals(update.getConfig().get("messages"), "2000");
        Assert.assertEquals(update.getApiId(), API_A, "The api must be taken over unchanged, as it cannot be modified");
        Assert.assertEquals(update.getMethod(), "*", "The method must be taken over unchanged, as it cannot be modified");
    }

    @Test
    public void addedRestrictionProducesSingleCreate() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", API_A, "*", "1000"));
        List<QuotaRestriction> desired = Arrays.asList(throttle(null, API_A, "*", "1000"), throttle(null, API_B, "*", "50"));
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, desired);
        Assert.assertEquals(operations.getToCreate().size(), 1);
        Assert.assertEquals(operations.getToCreate().get(0).getApiId(), API_B);
        Assert.assertTrue(operations.getToUpdate().isEmpty());
        Assert.assertTrue(operations.getToDelete().isEmpty());
    }

    @Test
    public void removedRestrictionProducesSingleDelete() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", API_A, "*", "1000"), throttle("id-2", API_B, "*", "50"));
        List<QuotaRestriction> desired = Arrays.asList(throttle(null, API_A, "*", "1000"));
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, desired);
        Assert.assertEquals(operations.getToDelete().size(), 1);
        Assert.assertEquals(operations.getToDelete().get(0).getId(), "id-2");
        Assert.assertTrue(operations.getToCreate().isEmpty());
        Assert.assertTrue(operations.getToUpdate().isEmpty());
    }

    /**
     * The API-Manager does not allow the method of a restriction to be changed, so it must become a delete plus a
     * create instead of an update.
     */
    @Test
    public void changedMethodProducesDeleteAndCreateInsteadOfUpdate() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", API_A, "*", "1000"));
        List<QuotaRestriction> desired = Arrays.asList(throttle(null, API_A, "method-1", "1000"));
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, desired);
        Assert.assertTrue(operations.getToUpdate().isEmpty(), "A scope change must never become an update");
        Assert.assertEquals(operations.getToDelete().size(), 1);
        Assert.assertEquals(operations.getToDelete().get(0).getId(), "id-1");
        Assert.assertEquals(operations.getToCreate().size(), 1);
        Assert.assertEquals(operations.getToCreate().get(0).getMethod(), "method-1");
    }

    @Test
    public void changedTypeProducesDeleteAndCreate() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", API_A, "*", "1000"));
        List<QuotaRestriction> desired = Arrays.asList(throttlemb(null, API_A, "*", "100"));
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, desired);
        Assert.assertTrue(operations.getToUpdate().isEmpty());
        Assert.assertEquals(operations.getToDelete().size(), 1);
        Assert.assertEquals(operations.getToCreate().size(), 1);
        Assert.assertEquals(operations.getToCreate().get(0).getType(), QuotaRestrictionType.throttlemb);
    }

    /**
     * A single API-Method may carry a throttle and a throttlemb restriction at the same time. They must not
     * collapse into one another.
     */
    @Test
    public void twoTypesOnTheSameMethodAreIndependent() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", API_A, "*", "1000"), throttlemb("id-2", API_A, "*", "100"));
        List<QuotaRestriction> desired = Arrays.asList(throttle(null, API_A, "*", "1000"), throttlemb(null, API_A, "*", "100"));
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, desired);
        Assert.assertTrue(operations.isEmpty(), "Expected no operations, but got: " + operations);
    }

    /**
     * The document level update was able to create duplicates. The surplus must be removed.
     */
    @Test
    public void duplicateActualRestrictionIsDeleted() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", API_A, "*", "1000"), throttle("id-2", API_A, "*", "1000"));
        List<QuotaRestriction> desired = Arrays.asList(throttle(null, API_A, "*", "1000"));
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, desired);
        Assert.assertEquals(operations.getToDelete().size(), 1);
        Assert.assertEquals(operations.getToDelete().get(0).getId(), "id-2");
        Assert.assertTrue(operations.getToCreate().isEmpty());
        Assert.assertTrue(operations.getToUpdate().isEmpty());
    }

    /**
     * This is the behaviour that replaces the "do not overwrite manually maintained quota" guarantee: restrictions
     * belonging to other APIs travel through the document unchanged and must not cause a single HTTP call.
     */
    @Test
    public void restrictionsOfOtherApisAreLeftAlone() {
        List<QuotaRestriction> actual = new ArrayList<>();
        List<QuotaRestriction> desired = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String otherApi = "cccccccc-0000-0000-0000-00000000000" + i;
            actual.add(throttle("id-" + i, otherApi, "*", "1000"));
            desired.add(throttle(null, otherApi, "*", "1000"));
        }
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, desired);
        Assert.assertTrue(operations.isEmpty(), "Expected no operations, but got: " + operations);
    }

    @Test
    public void emptyDesiredDeletesEverything() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", API_A, "*", "1000"), throttle("id-2", API_B, "*", "50"));
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actual, Collections.emptyList());
        Assert.assertEquals(operations.getToDelete().size(), 2);
        Assert.assertEquals(operations.size(), 2);
    }

    @Test
    public void nullListsAreTreatedAsEmpty() {
        Assert.assertTrue(QuotaConstraintDiff.diff(null, null).isEmpty());
        Assert.assertEquals(QuotaConstraintDiff.diff(null, Arrays.asList(throttle(null, API_A, "*", "1000"))).getToCreate().size(), 1);
        Assert.assertEquals(QuotaConstraintDiff.diff(Arrays.asList(throttle("id-1", API_A, "*", "1000")), null).getToDelete().size(), 1);
    }

    /**
     * The API-Manager returns the numbers as strings while a config file may provide them padded.
     */
    @Test
    public void configValuesAreComparedTrimmed() {
        QuotaRestriction actual = throttle("id-1", API_A, "*", "1000");
        QuotaRestriction desired = throttle(null, API_A, "*", " 1000 ");
        Assert.assertTrue(QuotaConstraintDiff.diff(Arrays.asList(actual), Arrays.asList(desired)).isEmpty());
    }

    /**
     * A null api or method means "all", exactly as QuotaRestrictionDeserializer normalises it.
     */
    @Test
    public void nullApiAndMethodMatchTheStarNotation() {
        List<QuotaRestriction> actual = Arrays.asList(throttle("id-1", null, null, "1000"));
        List<QuotaRestriction> desired = Arrays.asList(throttle(null, "*", "*", "1000"));
        Assert.assertTrue(QuotaConstraintDiff.diff(actual, desired).isEmpty());
    }

    /**
     * A key that is only present in the API-Manager response must not trigger an endless stream of updates.
     */
    @Test
    public void unmanagedConfigKeysAreIgnored() {
        QuotaRestriction actual = throttle("id-1", API_A, "*", "1000");
        actual.getConfig().put("someFutureKey", "someValue");
        QuotaRestriction desired = throttle(null, API_A, "*", "1000");
        Assert.assertTrue(QuotaConstraintDiff.diff(Arrays.asList(actual), Arrays.asList(desired)).isEmpty());
    }
}
