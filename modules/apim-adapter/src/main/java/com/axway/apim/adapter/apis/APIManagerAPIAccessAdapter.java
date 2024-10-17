package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.CacheType;
import com.axway.apim.adapter.HttpHelper;
import com.axway.apim.adapter.Response;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.AbstractEntity;
import com.axway.apim.api.model.ApiOrganizationSubscription;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class APIManagerAPIAccessAdapter {

    public static final String APIS = "/apis";

    public enum Type {
        organizations("Organization"),
        applications("Application");
        final String niceName;

        Type(String niceName) {
            this.niceName = niceName;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerAPIAccessAdapter.class);

    private static final ObjectMapper mapper = APIManagerAdapter.mapper;

    private final CoreParameters cmd;

    private final Map<Type, Cache<String, String>> caches = new EnumMap<>(Type.class);
    private static final HttpHelper httpHelper = new HttpHelper();

    private final APIManagerAdapter apiManagerAdapter;

    public APIManagerAPIAccessAdapter(APIManagerAdapter apiManagerAdapter) {
        this.apiManagerAdapter = apiManagerAdapter;
        cmd = CoreParameters.getInstance();
        caches.put(Type.applications, apiManagerAdapter.getCache(CacheType.applicationAPIAccessCache, String.class, String.class));
        caches.put(Type.organizations, apiManagerAdapter.getCache(CacheType.organizationAPIAccessCache, String.class, String.class));
    }

    Map<Type, Map<String, String>> apiManagerResponse = new EnumMap<>(Type.class);

    private void readAPIAccessFromAPIManager(Type type, String id) throws AppException {
        if (apiManagerResponse.get(type) != null && apiManagerResponse.get(type).get(id) != null) return;
        Map<String, String> mappedResponse = new HashMap<>();
        String cachedResponse = getFromCache(id, type);
        if (cachedResponse != null) {
            mappedResponse.put(id, cachedResponse);
            apiManagerResponse.put(type, mappedResponse);
            return;
        }
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/" + type + "/" + id + APIS).build();
            RestAPICall getRequest = new GETRequest(uri);
            LOG.debug("Load API-Access with type: {} from API-Manager with ID: {}", type, id);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    LOG.error("Error loading API-Access from API-Manager for {}. Response-Code: {}. Got response: {}", type, statusCode, response);
                    throw new AppException("Error loading API-Access from API-Manager for " + type + ". Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
                if (response.startsWith("{")) { // Got a single response!
                    response = "[" + response + "]";
                }
                mappedResponse.put(id, response);
                apiManagerResponse.put(type, mappedResponse);
                putToCache(id, type, response);
            }
        } catch (IOException | URISyntaxException e) {
            throw new AppException("Error loading API-Access from API-Manager for " + type + " from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public List<APIAccess> getAPIAccess(AbstractEntity entity, Type type) throws AppException {
        return getAPIAccess(entity, type, false);
    }

    public List<APIAccess> getAPIAccess(AbstractEntity entity, Type type, boolean includeAPIName) throws AppException {
        readAPIAccessFromAPIManager(type, entity.getId());
        String apiAccessResponse;
        try {
            apiAccessResponse = apiManagerResponse.get(type).get(entity.getId());
            List<APIAccess> allApiAccess = mapper.readValue(apiAccessResponse, new TypeReference<List<APIAccess>>() {
            });
            if (includeAPIName) {
                for (APIAccess apiAccess : allApiAccess) {
                    API api = APIManagerAdapter.getInstance().getApiAdapter().getAPI(new APIFilter.Builder().hasId(apiAccess.getApiId()).build(), false);
                    if (api == null) {
                        throw new AppException("Unable to find API with ID: " + apiAccess.getApiId() + " referenced by " + type.niceName + ": " + entity.getName() + ". You may try again with -clearCache", ErrorCode.UNKNOWN_API);
                    }
                    apiAccess.setApiName(api.getName());
                    apiAccess.setApiVersion(api.getVersion());
                }
            }
            return allApiAccess;
        } catch (Exception e) {
            throw new AppException("Error loading API-Access for " + type + " from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    private String getFromCache(String id, Type type) {
        Cache<String, String> usedCache = caches.get(type);
        if (usedCache != null && caches.get(type).get(id) != null) {
            LOG.trace("Return APIAccess for {} : {} from cache.", type, id);
            return caches.get(type).get(id);
        } else {
            LOG.trace("No cache hit for APIAccess {} : {}", type, id);
            return null;
        }
    }

    private void putToCache(String id, Type type, String allApiAccess) {
        Cache<String, String> usedCache = caches.get(type);
        if (usedCache != null) {
            usedCache.put(id, allApiAccess);
        }
    }

    private void removeFromCache(String id, Type type) {
        Cache<String, String> usedCache = caches.get(type);
        if (usedCache != null) {
            usedCache.remove(id);
        }
    }

    private void removeApplicationFromCache(String id) {
        Cache<String, String> usedCache = caches.get(Type.applications);
        if (usedCache != null && usedCache.containsKey(id))
                usedCache.remove(id);

    }

    public void saveAPIAccess(List<APIAccess> apiAccess, AbstractEntity entity, Type type) throws AppException {
        List<APIAccess> existingAPIAccess = getAPIAccess(entity, type, true);
        List<APIAccess> toBeRemovedAccesses = getMissingAPIAccesses(existingAPIAccess, apiAccess);
        List<APIAccess> toBeAddedAccesses = getMissingAPIAccesses(apiAccess, existingAPIAccess);
        for (APIAccess access : toBeRemovedAccesses) {
            populateApiId(access);
            deleteAPIAccess(access, entity, type);
        }
        for (APIAccess access : toBeAddedAccesses) {
            populateApiId(access);
            createAPIAccess(access, entity, type);
        }
    }

    public void populateApiId(APIAccess apiAccess) throws AppException {

        if (apiAccess.getApiId() == null) {
            LOG.debug("fetching Frontend Api id from API manager");
            APIManagerAPIAdapter apiAdapter = apiManagerAdapter.getApiAdapter();
            APIFilter apiFilter = new APIFilter.Builder().hasName(apiAccess.getApiName()).hasState(apiAccess.getState())
                .build();
            List<API> apis = apiAdapter.getAPIs(apiFilter);
            if (apis.size() > 1) {
                LOG.info("More than one version of Api available : {}", apis);
                LOG.info("API will be matched based on  API Version, If version is not available in config file, first api will be selected");
            }
            if (apiAccess.getApiVersion() == null) {
                apiAccess.setApiId(apis.get(0).getId());
                return;
            }
            for (API api : apis) {
                if (api.getVersion().equals(apiAccess.getApiVersion())) {
                    LOG.debug("Setting Front end API id : {} to API Access", api.getApiId());
                    apiAccess.setApiId(api.getId());
                    return;
                }
            }
            throw new AppException("Unable to find API", ErrorCode.UNKNOWN_API);
        }
    }

    public APIAccess createAPIAccessForApplication(APIAccess apiAccess, String applicationId) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + applicationId + APIS).build();
            mapper.setSerializationInclusion(Include.NON_NULL);
            FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                SimpleBeanPropertyFilter.serializeAllExcept("apiName", "apiVersion"));
            mapper.setFilterProvider(filter);
            HttpEntity entity = new StringEntity(mapper.writeValueAsString(apiAccess), ContentType.APPLICATION_JSON);
            RestAPICall request = new POSTRequest(entity, uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String response = EntityUtils.toString(httpResponse.getEntity());

                if (statusCode != 201) {
                    if (statusCode == 409 && response.contains("resource already exists")) {
                        LOG.info("Unexpected response while creating/updating API Access: {} Response-Code: {} Response Body: {} Ignoring this error.", apiAccess, statusCode, response);
                        return apiAccess;
                    }else {
                        LOG.error("Error granting access to application id : {} for  API-Proxy  : {} using URI: {} Received Status-Code: {} Response: {}", applicationId, apiAccess.getApiId(), uri, statusCode, response);
                        throw new AppException("Can't grant access to API.", ErrorCode.ERR_GRANTING_ACCESS_TO_API);
                    }
                }
                removeApplicationFromCache(applicationId);
                return mapper.readValue(response, APIAccess.class);
            }
        } catch (Exception e) {
            throw new AppException("Can't grant access to API.", ErrorCode.ERR_GRANTING_ACCESS_TO_API, e);
        }
    }

    public void createAPIAccess(APIAccess apiAccess, AbstractEntity parentEntity, Type type) throws AppException {
        List<APIAccess> existingAPIAccess = getAPIAccess(parentEntity, type, true);
        if (existingAPIAccess != null && existingAPIAccess.contains(apiAccess)) {
            apiAccess.setId(existingAPIAccess.get(0).getId());
            return;
        }
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/" + type + "/" + parentEntity.getId() + APIS).build();
            mapper.setSerializationInclusion(Include.NON_NULL);
            FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                SimpleBeanPropertyFilter.serializeAllExcept("apiName", "apiVersion"));
            mapper.setFilterProvider(filter);
            String json = mapper.writeValueAsString(apiAccess);
            HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            RestAPICall request = new POSTRequest(entity, uri);
            Response httpResponse = httpHelper.execute(request, true);
            int statusCode = httpResponse.getStatusCode();
            String response = httpResponse.getResponseBody();
            if (statusCode < 200 || statusCode > 299) {
                if ((statusCode == 403 || statusCode == 404) && (response.contains("Unknown API") || response.contains("The entity could not be found"))) {
                    LOG.warn("Got unexpected error: 'Unknown API' while creating API-Access ... Try again in {} milliseconds. (you may set -retryDelay <milliseconds>)", cmd.getRetryDelay());
                    Thread.sleep(cmd.getRetryDelay());
                    httpResponse = httpHelper.execute(request, true);
                    response = httpResponse.getResponseBody();
                    statusCode = httpResponse.getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error creating/updating API Access: {} Response-Code: {} Response Body: {}", apiAccess, statusCode, response);
                        throw new AppException("Error creating/updating API Access. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                    } else {
                        LOG.info("Successfully created API-Access on retry. Received Status-Code: {}", statusCode);
                    }
                } else if (statusCode == 409 && response.contains("resource already exists")) {
                    LOG.info("Unexpected response while creating/updating API Access: {} Response-Code: {} Response Body: {} Ignoring this error.", apiAccess, statusCode, response);
                    return;
                } else {
                    LOG.error("Error creating/updating API Access: {} Response-Code: {} Response Body: {}", apiAccess, statusCode, response);
                    throw new AppException("Error creating/updating API Access. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
            }
            // Clean cache for this ID (App/Org) to force reload next time
            removeFromCache(parentEntity.getId(), type);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | URISyntaxException e) {
            throw new AppException("Error creating/updating API Access.", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    public void deleteAPIAccess(APIAccess apiAccess, AbstractEntity parentEntity, Type type) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/" + type + "/" + parentEntity.getId() + "/apis/" + apiAccess.getId()).build();
            // Use an admin account for this request
            RestAPICall request = new DELRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    String errorResponse = EntityUtils.toString(httpResponse.getEntity());
                    LOG.error("Can't delete API access requests for application. Response-Code: {}. Got response: {}", statusCode, errorResponse);
                    throw new AppException("Can't delete API access requests for application. Response-Code: " + statusCode, ErrorCode.REVOKE_ACCESS_APPLICATION_ERR);
                }
                removeFromCache(parentEntity.getId(), type);
            }
        } catch (Exception e) {
            throw new AppException("Can't delete API access requests for application.", ErrorCode.REVOKE_ACCESS_APPLICATION_ERR, e);
        }
    }

    public void removeClientOrganization(List<Organization> removingActualOrgs, String apiId) throws AppException {
        for (Organization org : removingActualOrgs) {
            List<APIAccess> orgsApis = getAPIAccess(org, Type.organizations);
            for (APIAccess apiAccess : orgsApis) {
                if (apiAccess.getApiId().equals(apiId)) {
                    try {
                        deleteAPIAccess(apiAccess, org, Type.organizations);
                    } catch (Exception e) {
                        LOG.error("Can't delete API-Access for organization. ");
                        throw new AppException("Can't delete API-Access for organization.", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
                    }
                }
            }
        }
    }

    public List<APIAccess> getMissingAPIAccesses(List<APIAccess> apiAccess, List<APIAccess> otherApiAccess) {
        List<APIAccess> missingAccess = new ArrayList<>();
        if (otherApiAccess == null) otherApiAccess = new ArrayList<>();
        if (apiAccess == null) apiAccess = new ArrayList<>();
        for (APIAccess access : apiAccess) {
            if (otherApiAccess.contains(access)) {
                continue;
            }
            missingAccess.add(access);
        }
        return missingAccess;
    }


    public List<ApiOrganizationSubscription> getApiAccess(String apiId) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/proxies/" + apiId + "/apiaccess").build();
            RestAPICall request = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String response = EntityUtils.toString(httpResponse.getEntity());
                if (statusCode > 200) {
                    LOG.error("Can't get API access requests for API. Response-Code: {}. Got response: {}", statusCode, response);
                    throw new AppException("Can't get API access requests for API: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
                return mapper.readValue(response, new TypeReference<List<ApiOrganizationSubscription>>() {
                });
            }
        } catch (Exception e) {
            throw new AppException("Can't delete API access requests for application.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }
}
