package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.jackson.OrganizationDeserializer;
import com.axway.apim.adapter.jackson.OrganizationSerializerModifier;
import com.axway.apim.adapter.jackson.UserDeserializer;
import com.axway.apim.adapter.jackson.UserSerializerModifier;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIManagerRemoteHostsAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerRemoteHostsAdapter.class);

    ObjectMapper mapper = new ObjectMapper();

    private final CoreParameters cmd;

    public APIManagerRemoteHostsAdapter() {
        cmd = CoreParameters.getInstance();
    }

    Map<RemoteHostFilter, String> apiManagerResponse = new HashMap<>();

    private void readRemoteHostsFromAPIManager(RemoteHostFilter filter) throws AppException {
        if (apiManagerResponse.get(filter) != null) return;
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/remotehosts").build();
            RestAPICall getRequest = new GETRequest(uri);
            LOG.debug("Load remote hosts from API-Manager using filter: {}", filter);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getCode();
                if (statusCode < 200 || statusCode > 299) {
                    LOG.error("Error loading remoteHosts from API-Manager. Response-Code: {} Got Response Body:{}", statusCode, response);
                    throw new AppException("Error loading remoteHosts from API-Manager. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
                apiManagerResponse.put(filter, response);
            }
        } catch (Exception e) {
            throw new AppException("Can't read remoteHosts from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public Map<String, RemoteHost> getRemoteHosts(RemoteHostFilter filter) throws AppException {
        readRemoteHostsFromAPIManager(filter);
        try {
            // The users and organizations are only resolved for the remote hosts, which are requested. Otherwise, they are loaded
            // one by one for all remote hosts, even if only one is requested (issue #462).
            // If the remote hosts are filtered by organization, the organization is required to filter.
            boolean organizationRequiredToFilter = filter.getOrganization() != null;
            ObjectReader reader = mapper.readerFor(new TypeReference<List<RemoteHost>>() {})
                .withAttribute(UserDeserializer.Params.SKIP_LOOKUP, true)
                .withAttribute(OrganizationDeserializer.Params.SKIP_LOOKUP, !organizationRequiredToFilter);
            List<RemoteHost> remoteHostsList = reader.readValue(apiManagerResponse.get(filter));
            remoteHostsList.removeIf(filter::filter);
            Map<String, RemoteHost> remoteHosts = new HashMap<>();
            for (RemoteHost remoteHost : remoteHostsList) {
                resolveReferences(remoteHost, !organizationRequiredToFilter);
                remoteHosts.put(remoteHost.getName(), remoteHost);
            }
            return remoteHosts;
        } catch (Exception e) {
            throw new AppException("Error parsing API-Manager remote hosts", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    /**
     * Loads the details of the user and the organization, which are only given by their ID.
     */
    private void resolveReferences(RemoteHost remoteHost, boolean resolveOrganization) throws AppException {
        User createdBy = remoteHost.getCreatedBy();
        if (createdBy != null && createdBy.getId() != null) {
            User user = null;
            try {
                user = APIManagerAdapter.getInstance().getUserAdapter().getUserForId(createdBy.getId());
            } catch (AppException e) {
                LOG.error("Error reading user with ID: {} from API-Manager.", createdBy.getId());
            }
            if (user != null) {
                remoteHost.setCreatedBy(user);
            } else {
                LOG.error("User with ID: {} not found.", createdBy.getId());
            }
        }
        Organization organization = remoteHost.getOrganization();
        if (resolveOrganization && organization != null && organization.getId() != null) {
            remoteHost.setOrganization(APIManagerAdapter.getInstance().getOrgAdapter().getOrgForId(organization.getId()));
        }
    }

    public RemoteHost getRemoteHost(String name, Integer port) throws AppException {
        RemoteHostFilter remoteHostFilter = new RemoteHostFilter.Builder().hasName(name).hasPort(port).build();
        Map<String, RemoteHost> remoteHosts = getRemoteHosts(remoteHostFilter);
        return uniqueRemoteHost(remoteHosts, remoteHostFilter);
    }

    private RemoteHost uniqueRemoteHost(Map<String, RemoteHost> remoteHosts, RemoteHostFilter filter) throws AppException {
        if (remoteHosts.size() > 1) {
            throw new AppException("No unique Remote host found. Found " + remoteHosts.size() + " remote hosts based on filter: " + filter, ErrorCode.NO_UNIQUE_REMOTE_HOST);
        }
        if (remoteHosts.isEmpty()) return null;
        return remoteHosts.values().iterator().next();
    }

    public void createOrUpdateRemoteHost(RemoteHost desiredRemoteHost, RemoteHost actualRemoteHost) throws AppException {
        RemoteHost createdRemoteHost;
        try {
            URI uri;
            FilterProvider filter;
            if (actualRemoteHost == null) {
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/remotehosts").build();
                filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept("createdBy", "organization"));
            } else {
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/remotehosts/" + actualRemoteHost.getId()).build();
                filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept("organization"));
            }
            mapper.setFilterProvider(filter);
            mapper.registerModule(new SimpleModule().setSerializerModifier(new OrganizationSerializerModifier(false)));
            mapper.registerModule(new SimpleModule().setSerializerModifier(new UserSerializerModifier(false)));
            mapper.setSerializationInclusion(Include.NON_NULL);
            RestAPICall request;
            if (actualRemoteHost == null) {
                String json = mapper.writeValueAsString(desiredRemoteHost);
                HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                request = new POSTRequest(entity, uri);
            } else {
                desiredRemoteHost.setId(actualRemoteHost.getId());
                desiredRemoteHost.setCreatedOn(actualRemoteHost.getCreatedOn());
                desiredRemoteHost.setCreatedBy(actualRemoteHost.getCreatedBy());
                String json = mapper.writeValueAsString(desiredRemoteHost);
                HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                request = new PUTRequest(entity, uri);
            }
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getCode();
                if (statusCode < 200 || statusCode > 299) {
                    String errorResponse = EntityUtils.toString(httpResponse.getEntity());
                    LOG.error("Error creating/updating remote host. Response-Code: {}  Response Body: {}", statusCode, errorResponse);
                    throw new AppException("Error creating/updating remote host. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
                createdRemoteHost = mapper.readValue(httpResponse.getEntity().getContent(), RemoteHost.class);
            }
            desiredRemoteHost.setId(createdRemoteHost.getId());
        } catch (Exception e) {
            throw new AppException("Error creating/updating remote host.", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }
}
