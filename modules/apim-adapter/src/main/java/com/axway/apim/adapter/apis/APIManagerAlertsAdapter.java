package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Alerts;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class APIManagerAlertsAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerAlertsAdapter.class);

    ObjectMapper mapper = APIManagerAdapter.mapper;

    private final CoreParameters cmd;

    public APIManagerAlertsAdapter() {
        cmd = CoreParameters.getInstance();
    }

    String apiManagerResponse = null;

    Alerts managerAlerts = null;

    private void readAlertsFromAPIManager() throws AppException {
        if (apiManagerResponse != null) return;
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/alerts").build();
            RestAPICall getRequest = new GETRequest(uri);
            LOG.debug("Load configured alerts from API-Manager using filter");
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    LOG.error("Error loading alerts from API-Manager. Response-Code: {} Response Body: {}", statusCode, response);
                    throw new AppException("Error loading alerts from API-Manager. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
                apiManagerResponse = response;
            }
        } catch (Exception e) {
            LOG.error("Error cant read alerts from API-Manager. Can't parse response: ", e);
            throw new AppException("Can't read alerts from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public Alerts getAlerts() throws AppException {
        if (managerAlerts != null) return managerAlerts;
        readAlertsFromAPIManager();
        try {
            Alerts alerts = mapper.readValue(apiManagerResponse, Alerts.class);
            managerAlerts = alerts;
            return alerts;
        } catch (IOException e) {
            throw new AppException("Error parsing API-Manager alerts", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public void updateAlerts(Alerts alerts) throws AppException {
        try {
            if (!APIManagerAdapter.hasAdminAccount()) {
                throw new AppException("An Admin Account is required to update the API-Manager alerts configuration.", ErrorCode.NO_ADMIN_ROLE_USER);
            }
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/alerts").build();
            FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                    SimpleBeanPropertyFilter.serializeAllExcept());
            mapper.setFilterProvider(filter);
            mapper.setSerializationInclusion(Include.NON_NULL);
            try {
                RestAPICall request;
                String json = mapper.writeValueAsString(alerts);
                HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                request = new POSTRequest(entity, uri);
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error updating API-Manager alert configuration. Response-Code: {} Response Body: {}", statusCode, EntityUtils.toString(httpResponse.getEntity()) + "'");
                        throw new AppException("Error updating API-Manager alert configuration. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                }
            } catch (Exception e) {
                throw new AppException("Error updating API-Manager alert configuration.", ErrorCode.API_MANAGER_COMMUNICATION, e);
            }
        } catch (Exception e) {
            throw new AppException("Error updating API-Manager alert configuration.", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }
}
