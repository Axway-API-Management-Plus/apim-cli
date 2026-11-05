package com.axway.apim.adapter.apis;

import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictionType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Turns a desired quota document into the set of per-constraint operations required to reach it, so that the
 * API-Manager endpoints below /quotas/{id}/constraints can be used instead of replacing the entire quota document.
 * <p>
 * This class is deliberately free of any HTTP or state, which makes the migration testable without a running
 * API-Manager.
 */
public class QuotaConstraintDiff {

    public static final String PERIOD = "period";
    public static final String PER = "per";
    public static final String MESSAGES = "messages";
    public static final String MB = "mb";

    private static final String ALL = "*";

    private QuotaConstraintDiff() {
    }

    /**
     * The operations required to turn actual into desired.
     * They must be executed in the order DELETE, UPDATE, CREATE. A change of the restriction scope surfaces as a
     * delete of the old scope plus a create of the new one, and if the API-Manager enforces uniqueness on
     * (api, method, type) creating before deleting would be rejected.
     */
    public static class QuotaConstraintOperations {
        private final List<QuotaRestriction> toDelete = new ArrayList<>();
        private final List<QuotaRestriction> toUpdate = new ArrayList<>();
        private final List<QuotaRestriction> toCreate = new ArrayList<>();

        public List<QuotaRestriction> getToDelete() {
            return toDelete;
        }

        public List<QuotaRestriction> getToUpdate() {
            return toUpdate;
        }

        public List<QuotaRestriction> getToCreate() {
            return toCreate;
        }

        public boolean isEmpty() {
            return toDelete.isEmpty() && toUpdate.isEmpty() && toCreate.isEmpty();
        }

        public int size() {
            return toDelete.size() + toUpdate.size() + toCreate.size();
        }

        @Override
        public String toString() {
            return "QuotaConstraintOperations [delete=" + toDelete.size() + ", update=" + toUpdate.size()
                + ", create=" + toCreate.size() + "]";
        }
    }

    /**
     * Compares the restrictions currently known to the API-Manager against the desired ones.
     *
     * @param actual  the restrictions as returned by GET /quotas/{id}/constraints. These carry the constraint ID.
     * @param desired the complete desired quota document. These do not need to carry a constraint ID.
     * @return the operations required, which is empty when nothing has to be changed.
     */
    public static QuotaConstraintOperations diff(List<QuotaRestriction> actual, List<QuotaRestriction> desired) {
        QuotaConstraintOperations operations = new QuotaConstraintOperations();
        List<QuotaRestriction> actualRestrictions = actual == null ? Collections.emptyList() : actual;
        List<QuotaRestriction> desiredRestrictions = desired == null ? Collections.emptyList() : desired;

        // A Deque per key, as the legacy replace-all PUT was able to create duplicates. Any surplus is deleted.
        Map<Key, Deque<QuotaRestriction>> actualByKey = new HashMap<>();
        for (QuotaRestriction restriction : actualRestrictions) {
            actualByKey.computeIfAbsent(new Key(restriction), key -> new ArrayDeque<>()).add(restriction);
        }

        for (QuotaRestriction desiredRestriction : desiredRestrictions) {
            Deque<QuotaRestriction> candidates = actualByKey.get(new Key(desiredRestriction));
            QuotaRestriction match = candidates == null ? null : candidates.poll();
            if (match == null) {
                operations.getToCreate().add(desiredRestriction);
            } else if (!configEquals(match, desiredRestriction)) {
                // The key already pins api, method and type, so an update can never attempt to change the
                // restriction scope, which the API-Manager does not allow.
                operations.getToUpdate().add(merge(match, desiredRestriction));
            }
        }

        for (Deque<QuotaRestriction> remaining : actualByKey.values()) {
            operations.getToDelete().addAll(remaining);
        }
        return operations;
    }

    /**
     * Builds the body for a PUT: the identity and the scope are taken from the restriction known to the API-Manager,
     * only the configuration comes from the desired state.
     */
    private static QuotaRestriction merge(QuotaRestriction actual, QuotaRestriction desired) {
        QuotaRestriction merged = new QuotaRestriction();
        merged.setId(actual.getId());
        merged.setApiId(normalize(actual.getApiId()));
        merged.setMethod(normalize(actual.getMethod()));
        merged.setRestrictedAPI(actual.getRestrictedAPI());
        merged.setType(desired.getType());
        merged.setConfig(desired.getConfig());
        return merged;
    }

    /**
     * Compares only the configuration keys this CLI actually manages, which is the exact set written by
     * APIQuotaManager.mergeRestriction and built by QuotaRestrictionDeserializer. Comparing the complete map would
     * mean that a configuration key added by a future API-Manager causes a pointless update on every single run.
     */
    static boolean configEquals(QuotaRestriction actual, QuotaRestriction desired) {
        Map<String, String> actualConfig = actual.getConfig();
        Map<String, String> desiredConfig = desired.getConfig();
        if (actualConfig == null || desiredConfig == null) return actualConfig == desiredConfig;
        if (!valueEquals(actualConfig.get(PERIOD), desiredConfig.get(PERIOD))) return false;
        if (!valueEquals(actualConfig.get(PER), desiredConfig.get(PER))) return false;
        if (desired.getType() == QuotaRestrictionType.throttlemb) {
            return valueEquals(actualConfig.get(MB), desiredConfig.get(MB));
        }
        return valueEquals(actualConfig.get(MESSAGES), desiredConfig.get(MESSAGES));
    }

    /**
     * The API-Manager returns the numbers as strings, while a config file may provide them as numbers.
     */
    private static boolean valueEquals(String actual, String desired) {
        return Objects.equals(actual == null ? null : actual.trim(), desired == null ? null : desired.trim());
    }

    private static String normalize(String value) {
        return value == null ? ALL : value;
    }

    /**
     * Identifies a restriction by its scope.
     * <p>
     * api is part of the key because the whole quota document is compared, covering every API, unlike
     * APIQuotaManager.isSameRestrictionScope which only works on a list already narrowed to a single API.
     * type is part of the key because a single API-Method may carry a throttle and a throttlemb restriction at the
     * same time. The consequence is that changing the type results in a delete plus a create.
     */
    static class Key {
        private final String api;
        private final String method;
        private final QuotaRestrictionType type;

        Key(QuotaRestriction restriction) {
            this.api = normalize(restriction.getApiId());
            this.method = normalize(restriction.getMethod());
            this.type = restriction.getType();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Key)) return false;
            Key key = (Key) other;
            return Objects.equals(api, key.api) && Objects.equals(method, key.method) && type == key.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(api, method, type);
        }

        @Override
        public String toString() {
            return "[api=" + api + ", method=" + method + ", type=" + type + "]";
        }
    }
}