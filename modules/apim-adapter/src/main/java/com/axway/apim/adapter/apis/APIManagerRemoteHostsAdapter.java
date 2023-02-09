package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.jackson.OrganizationSerializerModifier;
import com.axway.apim.adapter.jackson.UserSerializerModifier;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
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
            LOG.debug("Load remote hosts from API-Manager using filter: " + filter);
            try(CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    LOG.error("Error loading remoteHosts from API-Manager. Response-Code: " + statusCode + ". Got response: '" + response + "'");
                    throw new AppException("Error loading remoteHosts from API-Manager. Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                }
                apiManagerResponse.put(filter, response);
            }
        } catch (Exception e) {
            LOG.error("Error cant read remoteHosts from API-Manager. Can't parse response: ", e);
            throw new AppException("Can't read remoteHosts from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public Map<String, RemoteHost> getRemoteHosts(RemoteHostFilter filter) throws AppException {
        readRemoteHostsFromAPIManager(filter);
        try {
            List<RemoteHost> remoteHostsList = mapper.readValue(apiManagerResponse.get(filter), new TypeReference<List<RemoteHost>>() {
            });
            remoteHostsList.removeIf(filter::filter);
            Map<String, RemoteHost> remoteHosts = new HashMap<>();
            for (RemoteHost remoteHost : remoteHostsList) {
                remoteHosts.put(remoteHost.getName(), remoteHost);
            }
            return remoteHosts;
        } catch (Exception e) {
            throw new AppException("Error parsing API-Manager remote hosts", ErrorCode.API_MANAGER_COMMUNICATION, e);
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
        if (remoteHosts.size() == 0) return null;
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
            try {
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
                try(CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error creating/updating remote host. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
                        throw new AppException("Error creating/updating remote host. Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                    createdRemoteHost = mapper.readValue(httpResponse.getEntity().getContent(), RemoteHost.class);
                }
            } catch (Exception e) {
                throw new AppException("Error creating/updating remote host.", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
            }
            desiredRemoteHost.setId(createdRemoteHost.getId());
        } catch (Exception e) {
            throw new AppException("Error creating/updating remote host.", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }
}
