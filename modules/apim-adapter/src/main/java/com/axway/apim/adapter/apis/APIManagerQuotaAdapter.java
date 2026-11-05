package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.CacheType;
import com.axway.apim.adapter.HttpHelper;
import com.axway.apim.adapter.Response;
import com.axway.apim.adapter.apis.QuotaConstraintDiff.QuotaConstraintOperations;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer.DeserializeMode;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.CoreParameters.QuotaUpdateMode;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axway.apim.adapter.apis.APIManagerQuotaAdapter.Quota.isDefaultQuota;

public class APIManagerQuotaAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerQuotaAdapter.class);
    private static final HttpHelper httpHelper = new HttpHelper();

    /**
     * The first API-Manager version exposing the per-constraint endpoints below /quotas/{id}/constraints.
     */
    public static final String QUOTA_CONSTRAINTS_MIN_VERSION = "7.7.20261130";

    private static final String[] QUOTA_WRITE_EXCLUDES = {"apiId", "apiName", "apiVersion", "apiPath", "vhost", "queryVersion"};

    public enum Quota {
        SYSTEM_DEFAULT("00000000-0000-0000-0000-000000000000", "System default"),
        APPLICATION_DEFAULT("00000000-0000-0000-0000-000000000001", "Application default");

        private final String quotaId;
        private final String friendlyName;

        Quota(String quotaId, String friendlyName) {
            this.quotaId = quotaId;
            this.friendlyName = friendlyName;
        }

        public String getQuotaId() {
            return quotaId;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public static boolean isDefaultQuota(String quotaId) {
            return SYSTEM_DEFAULT.getQuotaId().equals(quotaId) || APPLICATION_DEFAULT.getQuotaId().equals(quotaId);
        }
    }

    /**
     * Signals that this API-Manager does not serve the /quotas/{id}/constraints endpoints, so the complete quota
     * document has to be replaced instead.
     */
    static class ConstraintsUnsupportedException extends Exception {
        private static final long serialVersionUID = 1L;

        ConstraintsUnsupportedException(String message) {
            super(message);
        }
    }

    Cache<String, String> applicationsQuotaCache;

    ObjectMapper mapper = new ObjectMapper();

    /**
     * Separate mappers for the constraint endpoints, so that the shared mapper above - which getQuota and
     * saveQuota mutate via registerModule and setFilterProvider - cannot influence them.
     */
    private ObjectMapper constraintReadMapper;
    private final ObjectMapper constraintWriteMapper = new ObjectMapper();

    private final CoreParameters cmd;
    private final Map<String, String> apiManagerResponse = new HashMap<>();
    private final Map<String, List<QuotaRestriction>> constraintsResponse = new HashMap<>();

    /**
     * Memoised result of the API-Manager version check.
     */
    private Boolean constraintsSupported;

    /**
     * Latched as soon as the API-Manager turns out not to serve the constraint endpoints, so that the probe is
     * done at most once per run.
     */
    private boolean constraintsUnavailable = false;

    public APIManagerQuotaAdapter(APIManagerAdapter apiManagerAdapter) {
        cmd = CoreParameters.getInstance();
        applicationsQuotaCache = apiManagerAdapter.getCache(CacheType.applicationsQuotaCache, String.class, String.class);
    }


    private void readQuotaFromAPIManager(String quotaId) throws AppException {
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) return;
        if (apiManagerResponse.get(quotaId) != null) return;
        URI uri;
        try {
            if (isDefaultQuota(quotaId)) {
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/quotas/" + quotaId).build();
            } else {
                if (applicationsQuotaCache.containsKey(quotaId)) {
                    if (LOG.isDebugEnabled())
                        LOG.debug("Found quota with ID: {} in cache: {}", quotaId, applicationsQuotaCache.get(quotaId));
                    apiManagerResponse.put(quotaId, applicationsQuotaCache.get(quotaId));
                    return;
                }
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + quotaId + "/quota/").build();
            }
            RestAPICall getRequest = new GETRequest(uri);
            LOG.debug("Load quotas with ID: {} from API-Manager URI : {}", quotaId, uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                int statusCode = httpResponse.getCode();
                String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                if (statusCode != 200) {
                    throw new AppException("Can't read API-Manager Quota-Configuration. Got status code: " + statusCode + " for request: " + uri, ErrorCode.API_MANAGER_COMMUNICATION);
                }
                apiManagerResponse.put(quotaId, response);
                if (!isDefaultQuota(quotaId)) {
                    applicationsQuotaCache.put(quotaId, response);
                }
            }
        } catch (URISyntaxException | UnsupportedOperationException | IOException | ParseException e) {
            throw new AppException("Can't get API-Manager Quota-Configuration.", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    /**
     * Returns the configured quotas for the quota ID from the API manager.
     *
     * @param quotaId            is either system or application default or a concrete quota ID
     * @param api                is the quote for an API, then it can be passed here directly. This is then bound to the quote as restrictedAPI
     * @param addRestrictedAPI   If false, then no effort is made to load the restricted api and bind it to the quote during deserialization. In this case the api should be passed. Defaults to true
     * @param ignoreSystemQuotas If true, then no quotas are returned with the switch: system: true. This is useful if quotas for applications are requested and they should not contain application default quotas.
     * @return the configured quotas
     * @throws AppException is something goes wrong.
     */
    public APIQuota getQuota(String quotaId, API api, boolean addRestrictedAPI, boolean ignoreSystemQuotas) throws AppException {
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) return null;
        readQuotaFromAPIManager(quotaId); // Quota-ID might be the System- or Application-Default Quota
        APIQuota quotaConfig;
        try {
            mapper.registerModule(new SimpleModule().addDeserializer(QuotaRestriction.class, new QuotaRestrictionDeserializer(DeserializeMode.apiManagerData, addRestrictedAPI)));
            quotaConfig = mapper.readValue(apiManagerResponse.get(quotaId), APIQuota.class);
            if (ignoreSystemQuotas && quotaConfig.getSystem()) return null;
            if (api != null) {
                quotaConfig = filterQuotaForAPI(quotaConfig, api);
            }
        } catch (IOException e) {
            throw new AppException("Error cant load API-Methods for API: '" + api.getId() + "' from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
        return quotaConfig;
    }

    /**
     * Replicates the given quota document in the API-Manager.
     * <p>
     * On an API-Manager serving the /quotas/{id}/constraints endpoints each restriction is added, updated or removed
     * individually, which leaves the restrictions of other APIs untouched. Otherwise the complete quota document is
     * replaced, as done before those endpoints existed.
     */
    public void saveQuota(APIQuota quotaConfig, String quotaId) throws AppException {
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) return;
        if (useConstraints(quotaId)) {
            try {
                saveQuotaUsingConstraints(quotaConfig, quotaId);
                return;
            } catch (ConstraintsUnsupportedException e) {
                // The reported version promised the constraint endpoints but the API-Manager does not serve them.
                // Falling back is safe even half way through the operations: the caller always hands over the
                // complete desired quota document, so replacing the whole document re-establishes exactly the
                // intended end state. The API-Manager supports both representations and does not deprecate the
                // document level update, which makes this self healing rather than destructive.
                LOG.warn("API-Manager does not support the quota constraints endpoints ({}). Falling back to updating the complete quota document for the rest of this run.", e.getMessage());
                constraintsUnavailable = true;
            }
        }
        saveQuotaLegacy(quotaConfig, quotaId);
    }

    /**
     * Decides whether the per-constraint endpoints can be used for the given quota.
     */
    public boolean useConstraints(String quotaId) {
        if (constraintsUnavailable) return false;
        QuotaUpdateMode quotaUpdateMode = cmd.getQuotaUpdateMode();
        if (quotaUpdateMode == QuotaUpdateMode.legacy) return false;
        // The API-Manager exposes the constraints only below /quotas/{id}. Application specific quotas are managed
        // through /applications/{id}/quota, which has no per-constraint sub resource.
        if (!isDefaultQuota(quotaId)) return false;
        if (quotaUpdateMode == QuotaUpdateMode.constraints) return true;
        if (constraintsSupported == null) {
            constraintsSupported = APIManagerAdapter.hasAPIManagerVersion(QUOTA_CONSTRAINTS_MIN_VERSION);
            LOG.debug("API-Manager quota constraints endpoints supported: {} (required version: {})", constraintsSupported, QUOTA_CONSTRAINTS_MIN_VERSION);
        }
        return constraintsSupported;
    }

    private void saveQuotaUsingConstraints(APIQuota quotaConfig, String quotaId) throws AppException, ConstraintsUnsupportedException {
        List<QuotaRestriction> actualRestrictions = getConstraints(quotaId);
        List<QuotaRestriction> desiredRestrictions = quotaConfig == null || quotaConfig.getRestrictions() == null
            ? Collections.emptyList() : quotaConfig.getRestrictions();
        QuotaConstraintOperations operations = QuotaConstraintDiff.diff(actualRestrictions, desiredRestrictions);
        if (operations.isEmpty()) {
            LOG.info("Quota with ID: {} is UN-CHANGED. Nothing to do.", quotaId);
            return;
        }
        LOG.debug("Updating quota with ID: {} using constraints: {}", quotaId, operations);
        // Delete first: a changed restriction scope becomes a delete of the old scope plus a create of the new one,
        // and the API-Manager may reject a create that collides with the restriction still to be deleted.
        for (QuotaRestriction restriction : operations.getToDelete()) {
            if (restriction.getId() == null) {
                throw new ConstraintsUnsupportedException("API-Manager did not return an ID for restriction: " + restriction);
            }
            deleteConstraint(quotaId, restriction.getId());
        }
        for (QuotaRestriction restriction : operations.getToUpdate()) {
            updateConstraint(quotaId, restriction);
        }
        for (QuotaRestriction restriction : operations.getToCreate()) {
            createConstraint(quotaId, restriction);
        }
        invalidateQuota(quotaId);
    }

    /**
     * The original document level update, kept for API-Managers without the constraint endpoints.
     */
    private void saveQuotaLegacy(APIQuota quotaConfig, String quotaId) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/quotas/" + quotaId).build();
            FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(QUOTA_WRITE_EXCLUDES));
            mapper.setFilterProvider(filter);
            HttpEntity entity = new StringEntity(mapper.writeValueAsString(quotaConfig), ContentType.APPLICATION_JSON);
            RestAPICall request = new PUTRequest(entity, uri);
            Response httpResponse = httpHelper.execute(request, true);
            int statusCode = httpResponse.getStatusCode();
            String response = httpResponse.getResponseBody();
            if (statusCode < 200 || statusCode > 299) {
                if ((statusCode == 400 && response.contains("API not found"))
                    || (statusCode == 500 && response.contains("Internal server error") || statusCode == 102 && response.contains("Quota constraint not found"))
                ) {
                    LOG.warn("Got unexpected error while saving quota configuration ... Try again in {} milliseconds. (you may set -retryDelay <milliseconds>)", cmd.getRetryDelay());
                    Thread.sleep(cmd.getRetryDelay());
                    retrySaveQuota(request);
                } else {
                    throw new AppException("Can't update API-Manager Quota-Configuration. Response: '" + response + "'", ErrorCode.API_MANAGER_COMMUNICATION);
                }
            }
            invalidateQuota(quotaId);
        } catch (URISyntaxException | IOException | ParseException e) {
            throw new AppException("Can't update Quota-Configuration in API-Manager.", ErrorCode.UNXPECTED_ERROR, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public APIQuota getDefaultQuota(Quota quotaType) throws AppException {
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) return null;
        readQuotaFromAPIManager(quotaType.getQuotaId());
        APIQuota quotaConfig;
        try {
            quotaConfig = mapper.readValue(apiManagerResponse.get(quotaType.getQuotaId()), APIQuota.class);
        } catch (IOException e) {
            throw new AppException("Error cant load default quotas from API-Manager.", ErrorCode.UNXPECTED_ERROR, e);
        }
        return quotaConfig;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Quota constraints: /quotas/{quotaId}/constraints[/{constraintId}]
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Returns every restriction of the given quota, each carrying the ID assigned by the API-Manager.
     *
     * @throws ConstraintsUnsupportedException if this API-Manager does not serve the constraint endpoints.
     */
    public List<QuotaRestriction> getConstraints(String quotaId) throws AppException, ConstraintsUnsupportedException {
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) return Collections.emptyList();
        List<QuotaRestriction> cached = constraintsResponse.get(quotaId);
        if (cached != null) return cached;
        URI uri = constraintsUri(quotaId, null);
        Response response = executeConstraintCall(new GETRequest(uri), "read quota constraints", uri);
        int statusCode = response.getStatusCode();
        // A 404 on the collection of an always existing default quota, or a 405, means the endpoint itself is absent.
        if (statusCode == 404 || statusCode == 405) {
            throw new ConstraintsUnsupportedException("Got status code: " + statusCode + " for request: " + uri);
        }
        if (statusCode < 200 || statusCode > 299) {
            throw new AppException("Can't read API-Manager quota constraints. Got status code: " + statusCode + " for request: " + uri + " Response: '" + response.getResponseBody() + "'", ErrorCode.API_MANAGER_COMMUNICATION);
        }
        List<QuotaRestriction> restrictions = readConstraintList(response.getResponseBody(), uri);
        constraintsResponse.put(quotaId, restrictions);
        return restrictions;
    }


    /**
     * Adds a single restriction to the given quota, leaving the other restrictions untouched.
     *
     * @return the created restriction including the ID assigned by the API-Manager.
     */
    public QuotaRestriction createConstraint(String quotaId, QuotaRestriction restriction) throws AppException {
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) return null;
        URI uri = constraintsUri(quotaId, null);
        HttpEntity entity = new StringEntity(writeConstraint(restriction), ContentType.APPLICATION_JSON);
        Response response = executeConstraintCall(new POSTRequest(entity, uri), "create quota constraint", uri);
        if (response.getStatusCode() < 200 || response.getStatusCode() > 299) {
            throw new AppException("Can't create API-Manager quota constraint. Got status code: " + response.getStatusCode() + " for request: " + uri + " Response: '" + response.getResponseBody() + "'", ErrorCode.CANT_UPDATE_QUOTA_CONFIG);
        }
        LOG.info("Created quota constraint: {}", restriction);
        invalidateQuota(quotaId);
        return readConstraint(response.getResponseBody(), uri);
    }

    /**
     * Updates a single restriction in place. The API-Manager does not allow the api and the method of a restriction
     * to be changed, which is why the caller must only change the configuration.
     */
    public QuotaRestriction updateConstraint(String quotaId, QuotaRestriction restriction) throws AppException {
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) return null;
        if (restriction.getId() == null) {
            throw new AppException("Can't update quota constraint without an ID: " + restriction, ErrorCode.CANT_UPDATE_QUOTA_CONFIG);
        }
        URI uri = constraintsUri(quotaId, restriction.getId());
        HttpEntity entity = new StringEntity(writeConstraint(restriction), ContentType.APPLICATION_JSON);
        Response response = executeConstraintCall(new PUTRequest(entity, uri), "update quota constraint", uri);
        // The restriction disappeared between reading and writing it. Re-create it instead of failing the run.
        if (response.getStatusCode() == 404 || isConstraintNotFound(response)) {
            LOG.debug("Quota constraint: {} no longer exists. Creating it instead.", restriction.getId());
            invalidateQuota(quotaId);
            return createConstraint(quotaId, restriction);
        }
        if (response.getStatusCode() < 200 || response.getStatusCode() > 299) {
            throw new AppException("Can't update API-Manager quota constraint. Got status code: " + response.getStatusCode() + " for request: " + uri + " Response: '" + response.getResponseBody() + "'", ErrorCode.CANT_UPDATE_QUOTA_CONFIG);
        }
        LOG.info("Updated quota constraint: {}", restriction);
        invalidateQuota(quotaId);
        return readConstraint(response.getResponseBody(), uri);
    }

    /**
     * Removes a single restriction from the given quota, leaving the other restrictions untouched.
     */
    public void deleteConstraint(String quotaId, String constraintId) throws AppException {
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) return;
        URI uri = constraintsUri(quotaId, constraintId);
        Response response = executeConstraintCall(new DELRequest(uri), "delete quota constraint", uri);
        // Somebody else removed it already, which is the state we wanted to reach anyway.
        if (response.getStatusCode() == 404 || isConstraintNotFound(response)) {
            LOG.debug("Quota constraint: {} was already removed.", constraintId);
            invalidateQuota(quotaId);
            return;
        }
        if (response.getStatusCode() < 200 || response.getStatusCode() > 299) {
            throw new AppException("Can't delete API-Manager quota constraint. Got status code: " + response.getStatusCode() + " for request: " + uri + " Response: '" + response.getResponseBody() + "'", ErrorCode.CANT_UPDATE_QUOTA_CONFIG);
        }
        LOG.info("Deleted quota constraint with ID: {}", constraintId);
        invalidateQuota(quotaId);
    }

    private URI constraintsUri(String quotaId, String constraintId) throws AppException {
        try {
            String path = cmd.getApiBasepath() + "/quotas/" + quotaId + "/constraints"
                + (constraintId == null ? "" : "/" + constraintId);
            return new URIBuilder(cmd.getAPIManagerURL()).setPath(path).build();
        } catch (URISyntaxException e) {
            throw new AppException("Can't build quota constraints URI.", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    /**
     * Executes the call and retries it once on the errors the API-Manager is known to report while a just created
     * API is not yet visible to the quota service. This mirrors the tolerance of the document level update.
     */
    private Response executeConstraintCall(RestAPICall request, String what, URI uri) throws AppException {
        try {
            LOG.debug("Request to {}: {}", what, uri);
            Response response = httpHelper.execute(request, true);
            if (isTransientError(response)) {
                LOG.warn("Got unexpected error while trying to {} ... Try again in {} milliseconds. (you may set -retryDelay <milliseconds>)", what, cmd.getRetryDelay());
                Thread.sleep(cmd.getRetryDelay());
                response = httpHelper.execute(request, true);
            }
            return response;
        } catch (IOException | ParseException e) {
            throw new AppException("Can't " + what + " in API-Manager.", ErrorCode.UNXPECTED_ERROR, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AppException("Interrupted while trying to " + what + " in API-Manager.", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    private static boolean isTransientError(Response response) {
        String body = response.getResponseBody() == null ? "" : response.getResponseBody();
        return (response.getStatusCode() == 400 && body.contains("API not found"))
            || (response.getStatusCode() == 500 && body.contains("Internal server error"));
    }

    /**
     * The API-Manager reports a missing restriction in the response body rather than through a dedicated status code.
     */
    private static boolean isConstraintNotFound(Response response) {
        String body = response.getResponseBody();
        return body != null && body.contains("Quota constraint not found");
    }

    private List<QuotaRestriction> readConstraintList(String responseBody, URI uri) throws AppException {
        if (responseBody == null || responseBody.trim().isEmpty()) return new ArrayList<>();
        try {
            ObjectMapper readMapper = constraintReadMapper();
            CollectionType listType = readMapper.getTypeFactory().constructCollectionType(List.class, QuotaRestriction.class);
            return readMapper.readValue(responseBody, listType);
        } catch (IOException e) {
            throw new AppException("Can't parse quota constraints returned by: " + uri, ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    private QuotaRestriction readConstraint(String responseBody, URI uri) throws AppException {
        if (responseBody == null || responseBody.trim().isEmpty()) return null;
        try {
            return constraintReadMapper().readValue(responseBody, QuotaRestriction.class);
        } catch (IOException e) {
            throw new AppException("Can't parse quota constraint returned by: " + uri, ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    private String writeConstraint(QuotaRestriction restriction) throws AppException {
        try {
            FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(QUOTA_WRITE_EXCLUDES));
            ObjectWriter writer = constraintWriteMapper.writer(filter);
            return writer.writeValueAsString(restriction);
        } catch (IOException e) {
            throw new AppException("Can't serialize quota constraint: " + restriction, ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    /**
     * Built lazily, as the deserializer resolves other adapters which are not available while this adapter is
     * being constructed. addRestrictedAPI is false on purpose: resolving the API per restriction would issue one
     * lookup per restriction and the system default quota can hold hundreds of them.
     */
    private ObjectMapper constraintReadMapper() {
        if (constraintReadMapper == null) {
            constraintReadMapper = new ObjectMapper();
            constraintReadMapper.registerModule(new SimpleModule().addDeserializer(QuotaRestriction.class,
                new QuotaRestrictionDeserializer(DeserializeMode.apiManagerData, false)));
        }
        return constraintReadMapper;
    }

    /**
     * Drops every cached representation of the quota. apiManagerResponse must be included, as otherwise a later
     * read in the same run returns the state from before the update.
     */
    private void invalidateQuota(String quotaId) {
        apiManagerResponse.remove(quotaId);
        constraintsResponse.remove(quotaId);
        applicationsQuotaCache.remove(quotaId);
    }

    private static void retrySaveQuota(RestAPICall request) throws IOException, ParseException {
        Response httpResponse = httpHelper.execute(request, true);
        String response = httpResponse.getResponseBody();
        int statusCode = httpResponse.getStatusCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new AppException("Can't update API-Manager Quota-Configuration. Response: '" + response + "'", ErrorCode.API_MANAGER_COMMUNICATION);
        } else {
            LOG.info("Successfully created API-Access on retry. Received Status-Code: {}", statusCode);
        }
    }

    private static APIQuota filterQuotaForAPI(APIQuota quotaConfig, API api) throws AppException {
        List<QuotaRestriction> apiRestrictions = new ArrayList<>();
        try {
            for (QuotaRestriction restriction : quotaConfig.getRestrictions()) {
                if (restriction.getApiId().equals(api.getId())) {
                    restriction.setRestrictedAPI(api);
                    restriction.setApiId(api.getId());
                    apiRestrictions.add(restriction);
                }
            }
            if (apiRestrictions.isEmpty()) return null;
            APIQuota apiQuota = new APIQuota();
            apiQuota.setDescription(quotaConfig.getDescription());
            apiQuota.setName(quotaConfig.getName());
            apiQuota.setRestrictions(apiRestrictions);
            return apiQuota;
        } catch (Exception e) {
            throw new AppException("Can't parse quota from API-Manager", ErrorCode.UNXPECTED_ERROR, e);
        }
    }
}
