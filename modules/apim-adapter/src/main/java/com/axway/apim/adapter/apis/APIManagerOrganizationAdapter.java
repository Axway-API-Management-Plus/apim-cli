package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.CacheType;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter.Type;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class APIManagerOrganizationAdapter {

    public static final String ORGANIZATIONS = "/organizations/";
    private static final Logger LOG = LoggerFactory.getLogger(APIManagerOrganizationAdapter.class);

    private final CoreParameters cmd;

    ObjectMapper mapper = APIManagerAdapter.mapper;

    Map<OrgFilter, String> apiManagerResponse = new HashMap<>();

    Cache<String, String> organizationCache;

    public APIManagerOrganizationAdapter() {
        cmd = CoreParameters.getInstance();
        organizationCache = APIManagerAdapter.getCache(CacheType.organizationCache, String.class, String.class);
    }

    private void readOrgsFromAPIManager(OrgFilter filter) throws AppException {
        if (apiManagerResponse.get(filter) != null) return;
        if (!APIManagerAdapter.hasAdminAccount()) {
            LOG.warn("Using OrgAdmin only to load all organizations.");
        }
        String orgId = "";
        if (filter.getId() != null) {
            orgId = "/" + filter.getId();
        }

        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/organizations" + orgId)
                    .addParameters(filter.getFilters())
                    .build();
            RestAPICall getRequest = new GETRequest(uri);
            LOG.debug("Load organizations from API-Manager using filter: {}", filter);
            LOG.debug("Load organization with URI: {}", uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    LOG.error("Sent request: {}", uri);
                    LOG.error("Received Status-Code: {} Response: {}", httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
                    throw new AppException("", ErrorCode.API_MANAGER_COMMUNICATION);
                }
                String response = EntityUtils.toString(httpResponse.getEntity());
                if (!orgId.equals("")) {
                    // Store it as an Array
                    response = "[" + response + "]";
                    apiManagerResponse.put(filter, response);
                    LOG.debug("Organization id to be cached : {}", orgId);
                    organizationCache.put(orgId, response);
                } else {
                    // We got an Array from API-Manager
                    apiManagerResponse.put(filter, response);
                }
            }
        } catch (Exception e) {
            LOG.error("Error cant read orgs from API-Manager with filter: " + filter + ". Can't parse response: ", e);
            throw new AppException("Error cant read orgs from API-Manager with filter: " + filter, ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public void updateOrganization(Organization desiredOrg, Organization actualOrg) throws AppException {
        createOrUpdateOrganization(desiredOrg, actualOrg);
    }

    public void createOrganization(Organization desiredOrg) throws AppException {
        createOrUpdateOrganization(desiredOrg, null);
    }

    public void createOrUpdateOrganization(Organization desiredOrg, Organization actualOrg) throws AppException {
        Organization createdOrg;
        try {
            URI uri;
            if (actualOrg == null) {
                if (!APIManagerAdapter.hasAdminAccount()) {
                    throw new AppException("Admin account is required to create a new organization", ErrorCode.NO_ADMIN_ROLE_USER);
                }
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/organizations").build();
            } else {
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + ORGANIZATIONS + actualOrg.getId()).build();
            }
            FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                    SimpleBeanPropertyFilter.serializeAllExcept("image", "createdOn", "apis"));
            mapper.setFilterProvider(filter);
            mapper.setSerializationInclusion(Include.NON_NULL);
            try {
                RestAPICall request;
                if (actualOrg == null) {
                    String json = mapper.writeValueAsString(desiredOrg);
                    HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                    request = new POSTRequest(entity, uri);
                } else {
                    desiredOrg.setId(actualOrg.getId());
                    if (desiredOrg.getDn() == null) desiredOrg.setDn(actualOrg.getDn());
                    String json = mapper.writeValueAsString(desiredOrg);
                    HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                    request = new PUTRequest(entity, uri);
                }
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error creating/updating organization. Response-Code: {} Got response: {}", statusCode, EntityUtils.toString(httpResponse.getEntity()));
                        throw new AppException("Error creating/updating organization. Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                    createdOrg = mapper.readValue(httpResponse.getEntity().getContent(), Organization.class);
                }
            } catch (Exception e) {
                throw new AppException("Error creating/updating organization.", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
            }
            desiredOrg.setId(createdOrg.getId());
            saveImage(desiredOrg, actualOrg);
            saveAPIAccess(desiredOrg, actualOrg);
            // Force reload of this organization next time
            organizationCache.remove(createdOrg.getId());

        } catch (Exception e) {
            throw new AppException("Error creating/updating organization", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    public void deleteOrganization(Organization org) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + ORGANIZATIONS + org.getId()).build();
            RestAPICall request = new DELRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 204) {
                    LOG.error("Error deleting organization. Response-Code: {} Got response: {}", statusCode, EntityUtils.toString(httpResponse.getEntity()));
                    throw new AppException("Error deleting organization. Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                }
                // Deleted org should also be deleted from the cache
                organizationCache.remove(org.getId());
                LOG.info("Organization: {}  ( {} ) successfully deleted", org.getName(), org.getId());
            }
        } catch (Exception e) {
            throw new AppException("Error deleting organization", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
        }
    }

    private void saveImage(Organization org, Organization actualOrg) throws URISyntaxException, AppException {
        if (org.getImage() == null) return;
        if (actualOrg != null && org.getImage().equals(actualOrg.getImage())) return;
        URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + ORGANIZATIONS + org.getId() + "/image").build();
        InputStream is = org.getImage().getInputStream();
        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", is, ContentType.create("image/jpeg"), org.getImage().getBaseFilename())
                .build();
        try {
            RestAPICall apiCall = new POSTRequest(entity, uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) apiCall.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    LOG.error("Error saving/updating organization image. Response-Code: {} Got response: {}", statusCode, EntityUtils.toString(httpResponse.getEntity()));
                }
            }
        } catch (Exception e) {
            throw new AppException("Error uploading organization image", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    public List<Organization> getOrgs(OrgFilter filter) throws AppException {
        readOrgsFromAPIManager(filter);
        try {
            List<Organization> allOrgs = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<Organization>>() {
            });
            allOrgs.removeIf(filter::filter);
            for (Organization org : allOrgs) {
                addImage(org, filter.isIncludeImage());
                addAPIAccess(org, filter.isIncludeAPIAccess());
            }
            Utils.addCustomPropertiesForEntity(allOrgs, this.apiManagerResponse.get(filter), filter);
            return allOrgs;
        } catch (Exception e) {
            LOG.error("Error cant read orgs from API-Manager with filter: {} Returned response: {}", filter, apiManagerResponse);
            throw new AppException("Error cant read orgs from API-Manager with filter: " + filter, ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public List<Organization> getAllOrgs() throws AppException {
        return getOrgs(new OrgFilter.Builder().build());
    }

    public Organization getOrgForName(String orgName) throws AppException {
        return getOrg(new OrgFilter.Builder().hasName(orgName).build());
    }

    public Organization getOrgForId(String orgId) throws AppException {
        return getOrg(new OrgFilter.Builder().hasId(orgId).build());
    }

    public Organization getOrg(OrgFilter filter) throws AppException {
        List<Organization> orgs = getOrgs(filter);
        if (orgs.size() > 1) {
            throw new AppException("No unique Organization found for filter: " + filter, ErrorCode.UNKNOWN_API);
        }
        if (orgs.size() == 0) {
            LOG.info("No organization found using filter: {}", filter);
            return null;
        }
        return orgs.get(0);
    }

    void addAPIAccess(Organization org, boolean addAPIAccess) throws Exception {
        if (!addAPIAccess) return;
        try {
            List<APIAccess> apiAccess = APIManagerAdapter.getInstance().accessAdapter.getAPIAccess(org, Type.organizations, true);
            org.getApiAccess().addAll(apiAccess);
        } catch (Exception e) {
            throw new AppException("Error reading organizations API Access.", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    private void saveAPIAccess(Organization org, Organization actualOrg) throws AppException {
        if (org.getApiAccess() == null || org.getApiAccess().size() == 0) return;
        if (actualOrg != null && actualOrg.getApiAccess().size() == org.getApiAccess().size() && new HashSet<>(actualOrg.getApiAccess()).containsAll(org.getApiAccess()))
            return;
        if (!APIManagerAdapter.hasAdminAccount()) {
            LOG.warn("Ignoring API-Access, as no admin account is given");
            return;
        }
        APIManagerAPIAccessAdapter accessAdapter = APIManagerAdapter.getInstance().accessAdapter;
        accessAdapter.saveAPIAccess(org.getApiAccess(), org, Type.organizations);
    }

    void addImage(Organization org, boolean addImage) throws Exception {
        if (!addImage) return;
        if (org.getImageUrl() == null) return;
        URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + ORGANIZATIONS + org.getId() + "/image")
                .build();
        Image image = APIManagerAdapter.getImageFromAPIM(uri, "org-image");
        org.setImage(image);
    }

    public void setAPIManagerTestResponse(OrgFilter key, String response) {
        this.apiManagerResponse.put(key, response);
    }
}
