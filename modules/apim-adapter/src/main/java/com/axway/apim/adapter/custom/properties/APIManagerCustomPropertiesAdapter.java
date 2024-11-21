package com.axway.apim.adapter.custom.properties;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.CustomProperties;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.CustomProperty;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class APIManagerCustomPropertiesAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerCustomPropertiesAdapter.class);

    public APIManagerCustomPropertiesAdapter() {
        // Default constructor
    }

    private String readCustomPropertiesFromAPIManager() throws AppException {
        try {
            CoreParameters cmd = CoreParameters.getInstance();
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/config/customproperties").build();
            RestAPICall getRequest = new GETRequest(uri);
            LOG.debug("Read configured custom properties from API-Manager");
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    LOG.error("Error loading custom-properties from API-Manager. Response-Code: {} Response Body: {}", statusCode, response);
                    throw new AppException("Error loading custom-properties from API-Manager. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
                return response;
            }
        } catch (Exception e) {
            throw new AppException("Can't read configuration from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public CustomProperties getCustomProperties() throws AppException {
        String apiManagerResponse = readCustomPropertiesFromAPIManager();
        try {
            ObjectMapper mapper = APIManagerAdapter.mapper;
            return mapper.readValue(apiManagerResponse, CustomProperties.class);
        } catch (IOException e) {
            throw new AppException("Error parsing API-Manager custom properties", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public Map<String, CustomProperty> getRequiredCustomProperties(Type type) throws AppException {
        Map<String, CustomProperty> allCustomProps = getCustomProperties(type);
        if (allCustomProps == null) return Collections.emptyMap();
        Map<String, CustomProperty> requiredCustomProps = new HashMap<>();
        for (Map.Entry<String, CustomProperty> value : allCustomProps.entrySet()) {
            CustomProperty prop = value.getValue();
            if (prop.getRequired()) {
                requiredCustomProps.put(value.getKey(), prop);
            }
        }
        return requiredCustomProps;
    }

    public Map<String, CustomProperty> getCustomProperties(Type type) throws AppException {
        CustomProperties customPropertiesLocal = getCustomProperties();
        if (customPropertiesLocal == null) return Collections.emptyMap();
        switch (type) {
            case api:
                return customPropertiesLocal.getApi();
            case application:
                return customPropertiesLocal.getApplication();
            case user:
                return customPropertiesLocal.getUser();
            case organization:
                return customPropertiesLocal.getOrganization();
            default:
                throw new AppException("Unknown custom properties type: " + type, ErrorCode.UNXPECTED_ERROR);
        }
    }

    public List<String> getCustomPropertyNames(Type type) throws AppException {
        Map<String, CustomProperty> customPropertiesLocal = getCustomProperties(type);
        if (customPropertiesLocal == null) return new ArrayList<>();
        return new ArrayList<>(customPropertiesLocal.keySet());
    }
}
