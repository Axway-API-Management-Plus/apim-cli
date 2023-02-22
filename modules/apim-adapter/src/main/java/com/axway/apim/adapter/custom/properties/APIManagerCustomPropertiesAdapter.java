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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIManagerCustomPropertiesAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerCustomPropertiesAdapter.class);

    ObjectMapper mapper = APIManagerAdapter.mapper;

    CoreParameters cmd = CoreParameters.getInstance();

    public APIManagerCustomPropertiesAdapter() {
    }

    String apiManagerResponse;

    CustomProperties customProperties;

    private void readCustomPropertiesFromAPIManager() throws AppException {
        if (apiManagerResponse != null) return;
        URI uri;
        HttpResponse httpResponse = null;
        try {
            uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/config/customproperties").build();
            RestAPICall getRequest = new GETRequest(uri);
            LOG.debug("Read configured custom properties from API-Manager");
            httpResponse = getRequest.execute();
            String response = EntityUtils.toString(httpResponse.getEntity());
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode > 299) {
                LOG.error("Error loading custom-properties from API-Manager. Response-Code: {} Response Body: {}", statusCode, response);
                throw new AppException("Error loading custom-properties from API-Manager. Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
            }
            apiManagerResponse = response;
        } catch (Exception e) {
            LOG.error("Error cant read configuration from API-Manager. Can't parse response: {}", httpResponse, e);
            throw new AppException("Can't read configuration from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        } finally {
            try {
                if (httpResponse != null)
                    ((CloseableHttpResponse) httpResponse).close();
            } catch (Exception ignore) {
            }
        }
    }

    public CustomProperties getCustomProperties() throws AppException {
        if (customProperties != null) return customProperties;
        readCustomPropertiesFromAPIManager();
        try {
            CustomProperties props = mapper.readValue(apiManagerResponse, CustomProperties.class);
            customProperties = props;
            return props;
        } catch (IOException e) {
            throw new AppException("Error parsing API-Manager custom properties", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public Map<String, CustomProperty> getRequiredCustomProperties(Type type) throws AppException {
        Map<String, CustomProperty> allCustomProps = getCustomProperties(type);
        if (allCustomProps == null) return null;
        Map<String, CustomProperty> requiredCustomProps = new HashMap<>();
        for (String propName : allCustomProps.keySet()) {
            CustomProperty prop = allCustomProps.get(propName);
            if (prop.getRequired()) {
                requiredCustomProps.put(propName, prop);
            }
        }
        return requiredCustomProps;
    }

    public Map<String, CustomProperty> getCustomProperties(Type type) throws AppException {
        CustomProperties customProperties = getCustomProperties();
        if (customProperties == null) return null;
        switch (type) {
            case api:
                return customProperties.getApi();
            case application:
                return customProperties.getApplication();
            case user:
                return customProperties.getUser();
            case organization:
                return customProperties.getOrganization();
            default:
                throw new AppException("Unknown custom properties type: " + type, ErrorCode.UNXPECTED_ERROR);
        }
    }

    public List<String> getCustomPropertyNames(Type type) throws AppException {
        Map<String, CustomProperty> customProperties = getCustomProperties(type);
        if (customProperties == null) return new ArrayList<>();
        return new ArrayList<>(customProperties.keySet());
    }
}
