package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.jackson.PolicySerializerModifier;
import com.axway.apim.api.model.Config;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import java.net.URISyntaxException;

public class APIManagerConfigAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerConfigAdapter.class);

    ObjectMapper mapper = APIManagerAdapter.mapper;

    private final CoreParameters cmd;

    public APIManagerConfigAdapter() {
        cmd = CoreParameters.getInstance();
    }

    public Config getConfig() throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/config").build();
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    LOG.error("Error loading configuration from API-Manager. Response-Code: {} Got response: {}", statusCode, response);
                    throw new AppException("Error loading configuration from API-Manager. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
                return mapper.readValue(response, Config.class);
            }
        } catch (Exception e) {
            throw new AppException("Can't read configuration from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION_ERR, e);
        }
    }

    public void updateConfiguration(Config desiredConfig) throws AppException {
        try {
            if (!APIManagerAdapter.getInstance().hasAdminAccount()) {
                throw new AppException("An Admin Account is required to update the API-Manager configuration.", ErrorCode.NO_ADMIN_ROLE_USER);
            }
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/config").build();
            mapper.registerModule(new SimpleModule().setSerializerModifier(new PolicySerializerModifier(false)));
            mapper.setSerializationInclusion(Include.NON_NULL);
            RestAPICall request;
            String json = mapper.writeValueAsString(desiredConfig);
            HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            request = new PUTRequest(entity, uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    String errorResponse = EntityUtils.toString(httpResponse.getEntity());
                    LOG.error("Error updating API-Manager configuration. Response-Code: {} Got response: {}", statusCode, errorResponse);
                    throw new AppException("Error updating API-Manager configuration. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new AppException("Error updating API-Manager configuration.", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }
}
