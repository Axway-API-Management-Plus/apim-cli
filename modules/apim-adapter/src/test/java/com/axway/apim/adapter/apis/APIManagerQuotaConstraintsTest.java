package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictionType;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.CoreParameters.QuotaUpdateMode;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

/**
 * Covers the migration of the quota write path to the /quotas/{id}/constraints endpoints.
 * The API-Manager version is forced per test rather than changed in the Wiremock config.json fixture, so that both
 * the legacy and the constraints path stay covered.
 */
public class APIManagerQuotaConstraintsTest extends WiremockWrapper {

    private static final String SYSTEM_QUOTA_ID = APIManagerQuotaAdapter.Quota.SYSTEM_DEFAULT.getQuotaId();
    private static final String SYSTEM_QUOTA_URL = "/api/portal/v1.4/quotas/" + SYSTEM_QUOTA_ID;
    private static final String CONSTRAINTS_URL_PATTERN = "/api/portal/v1\\.4/quotas/[0-9a-f-]+/constraints/[0-9a-f-]+";
    private static final String API_WEEK = "9170e76e-ee7a-45ae-8afe-32a32a5da65c";
    private static final String API_SECOND = "e4ded8c8-0a40-4b50-bc13-552fb7209150";

    private String originalVersion;

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        try {
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("test");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            originalVersion = APIManagerAdapter.getInstance().getApiManagerVersion();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @BeforeMethod
    public void resetRequests() {
        getWireMockServer().resetRequests();
    }

    @AfterMethod
    public void restoreVersion() throws AppException {
        APIManagerAdapter.getInstance().setApiManagerVersion(originalVersion);
        CoreParameters.getInstance().setQuotaUpdateMode(QuotaUpdateMode.auto);
    }

    /**
     * A fresh adapter per test, as it memoises the capability check and the constraints it has read.
     */
    private APIManagerQuotaAdapter newAdapter() throws AppException {
        return new APIManagerQuotaAdapter(APIManagerAdapter.getInstance());
    }

    private static QuotaRestriction throttle(String apiId, String method, String messages, String period, String per) {
        QuotaRestriction restriction = new QuotaRestriction();
        restriction.setApiId(apiId);
        restriction.setMethod(method);
        restriction.setType(QuotaRestrictionType.throttle);
        Map<String, String> config = new LinkedHashMap<>();
        config.put("period", period);
        config.put("per", per);
        config.put("messages", messages);
        restriction.setConfig(config);
        return restriction;
    }

    private static APIQuota quotaWith(List<QuotaRestriction> restrictions) {
        APIQuota quota = new APIQuota();
        quota.setId(SYSTEM_QUOTA_ID);
        quota.setName("System");
        quota.setType("API");
        quota.setRestrictions(new ArrayList<>(restrictions));
        return quota;
    }

    /**
     * The three restrictions served by the constraints stub, expressed as a desired state.
     */
    private static List<QuotaRestriction> unchangedDesiredState() {
        return Arrays.asList(
            throttle(API_WEEK, "*", "1000", "week", "2"),
            throttle(API_SECOND, "*", "1000", "second", "2"),
            throttle("*", "*", "10000", "second", "1"));
    }

    @Test
    public void getConstraintsPopulatesTheServerAssignedId() throws Exception {
        List<QuotaRestriction> constraints = newAdapter().getConstraints(SYSTEM_QUOTA_ID);
        Assert.assertEquals(constraints.size(), 3);
        Assert.assertEquals(constraints.get(0).getId(), "c0000000-0000-0000-0000-000000000001");
        Assert.assertEquals(constraints.get(2).getId(), "c0000000-0000-0000-0000-000000000003");
        Assert.assertEquals(constraints.get(2).getApiId(), "*");
    }

    @Test
    public void createConstraintReturnsTheServerAssignedId() throws Exception {
        QuotaRestriction created = newAdapter().createConstraint(SYSTEM_QUOTA_ID, throttle("*", "*", "10000", "second", "1"));
        Assert.assertNotNull(created);
        Assert.assertEquals(created.getId(), "c0000000-0000-0000-0000-0000000000ff");
        getWireMockServer().verify(1, postRequestedFor(urlEqualTo(SYSTEM_QUOTA_URL + "/constraints")));
    }

    @Test
    public void deleteConstraintAddressesTheConstraintById() throws Exception {
        newAdapter().deleteConstraint(SYSTEM_QUOTA_ID, "c0000000-0000-0000-0000-000000000002");
        getWireMockServer().verify(1, deleteRequestedFor(
            urlEqualTo(SYSTEM_QUOTA_URL + "/constraints/c0000000-0000-0000-0000-000000000002")));
    }

    /**
     * The constraint id must never be sent in the body: it is ignored on create and taken from the URL on update.
     */
    @Test
    public void constraintIdIsNeverSerializedIntoTheBody() throws Exception {
        QuotaRestriction restriction = throttle("*", "*", "10000", "second", "1");
        restriction.setId("c0000000-0000-0000-0000-000000000003");
        newAdapter().updateConstraint(SYSTEM_QUOTA_ID, restriction);
        String body = getWireMockServer().findAll(putRequestedFor(urlPathMatching(CONSTRAINTS_URL_PATTERN)))
            .get(0).getBodyAsString();
        Assert.assertFalse(body.contains("\"id\""), "The constraint ID must not be part of the request body: " + body);
        Assert.assertTrue(body.contains("\"api\""), "The api field is expected in the request body: " + body);
    }

    /**
     * The whole point of the migration: a changed restriction must not rewrite the complete quota document.
     */
    @Test
    public void saveQuotaUsesConstraintsAndNeverReplacesTheWholeDocument() throws Exception {
        APIManagerAdapter.getInstance().setApiManagerVersion(APIManagerQuotaAdapter.QUOTA_CONSTRAINTS_MIN_VERSION);
        // Drop the second restriction and raise the message rate of the third one.
        List<QuotaRestriction> desired = Arrays.asList(
            throttle(API_WEEK, "*", "1000", "week", "2"),
            throttle("*", "*", "20000", "second", "1"));

        newAdapter().saveQuota(quotaWith(desired), SYSTEM_QUOTA_ID);

        getWireMockServer().verify(0, putRequestedFor(urlEqualTo(SYSTEM_QUOTA_URL)));
        getWireMockServer().verify(1, deleteRequestedFor(
            urlEqualTo(SYSTEM_QUOTA_URL + "/constraints/c0000000-0000-0000-0000-000000000002")));
        getWireMockServer().verify(1, putRequestedFor(
            urlEqualTo(SYSTEM_QUOTA_URL + "/constraints/c0000000-0000-0000-0000-000000000003")));
        getWireMockServer().verify(0, postRequestedFor(urlEqualTo(SYSTEM_QUOTA_URL + "/constraints")));
    }

    /**
     * Restrictions that did not change must not cause a single write.
     */
    @Test
    public void saveQuotaIssuesNoWriteWhenNothingChanged() throws Exception {
        APIManagerAdapter.getInstance().setApiManagerVersion(APIManagerQuotaAdapter.QUOTA_CONSTRAINTS_MIN_VERSION);
        newAdapter().saveQuota(quotaWith(unchangedDesiredState()), SYSTEM_QUOTA_ID);
        getWireMockServer().verify(0, putRequestedFor(urlEqualTo(SYSTEM_QUOTA_URL)));
        getWireMockServer().verify(0, putRequestedFor(urlPathMatching(CONSTRAINTS_URL_PATTERN)));
        getWireMockServer().verify(0, deleteRequestedFor(urlPathMatching(CONSTRAINTS_URL_PATTERN)));
        getWireMockServer().verify(0, postRequestedFor(urlEqualTo(SYSTEM_QUOTA_URL + "/constraints")));
    }

    /**
     * An API-Manager older than the constraints endpoints must keep replacing the complete quota document.
     */
    @Test
    public void olderApiManagerKeepsUsingTheDocumentLevelUpdate() throws Exception {
        APIManagerAdapter.getInstance().setApiManagerVersion("7.7.20221130");
        newAdapter().saveQuota(quotaWith(unchangedDesiredState()), SYSTEM_QUOTA_ID);
        getWireMockServer().verify(1, putRequestedFor(urlEqualTo(SYSTEM_QUOTA_URL)));
        getWireMockServer().verify(0, putRequestedFor(urlPathMatching(CONSTRAINTS_URL_PATTERN)));
    }

    /**
     * The escape hatch must win over the version check.
     */
    @Test
    public void quotaUpdateModeLegacyForcesTheDocumentLevelUpdate() throws Exception {
        APIManagerAdapter.getInstance().setApiManagerVersion(APIManagerQuotaAdapter.QUOTA_CONSTRAINTS_MIN_VERSION);
        CoreParameters.getInstance().setQuotaUpdateMode(QuotaUpdateMode.legacy);
        newAdapter().saveQuota(quotaWith(unchangedDesiredState()), SYSTEM_QUOTA_ID);
        getWireMockServer().verify(1, putRequestedFor(urlEqualTo(SYSTEM_QUOTA_URL)));
        getWireMockServer().verify(0, putRequestedFor(urlPathMatching(CONSTRAINTS_URL_PATTERN)));
    }

    /**
     * Application specific quotas are served by /applications/{id}/quota, which has no per-constraint sub resource,
     * so they must stay on the document level update even on a new API-Manager.
     */
    @Test
    public void applicationSpecificQuotaIsNotMigrated() throws Exception {
        APIManagerAdapter.getInstance().setApiManagerVersion(APIManagerQuotaAdapter.QUOTA_CONSTRAINTS_MIN_VERSION);
        String appQuotaId = "54c4358c-2036-4082-a0b6-7eb44b6dd60d";
        APIQuota quota = quotaWith(unchangedDesiredState());
        quota.setId(appQuotaId);
        try {
            newAdapter().saveQuota(quota, appQuotaId);
        } catch (AppException e) {
            // There is no stub for this quota ID, the point of the test is which URL was called.
        }
        getWireMockServer().verify(0, postRequestedFor(urlPathMatching("/api/portal/v1\\.4/quotas/[0-9a-f-]+/constraints")));
        getWireMockServer().verify(0, putRequestedFor(urlPathMatching(CONSTRAINTS_URL_PATTERN)));
        // It must have gone down the document level path instead.
        getWireMockServer().verify(1, putRequestedFor(urlEqualTo("/api/portal/v1.4/quotas/" + appQuotaId)));
    }
}
