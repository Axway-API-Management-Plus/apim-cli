package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIManagerAPIMethodAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerAPIMethodAdapter.class);

    ObjectMapper mapper = APIManagerAdapter.mapper;

    private final CoreParameters cmd;

    public APIManagerAPIMethodAdapter() {
        cmd = CoreParameters.getInstance();
    }

    Map<String, String> apiManagerResponse = new HashMap<>();

    private void readMethodsFromAPIManager(String apiId) throws AppException {
        if (this.apiManagerResponse.get(apiId) != null) return;
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/proxies/" + apiId + "/operations").build();
            LOG.debug("Load API-Methods for API: {} from API-Manager", apiId);
            RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                this.apiManagerResponse.put(apiId, EntityUtils.toString(httpResponse.getEntity()));
            }
        } catch (Exception e) {
            LOG.error("Error cant load API-Methods for API: " + apiId, e);
            throw new AppException("Error cant load API-Methods for API: '" + apiId + "' from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public List<APIMethod> getAllMethodsForAPI(String apiId) throws AppException {
        readMethodsFromAPIManager(apiId);
        List<APIMethod> apiMethods;
        try {
            apiMethods = mapper.readValue(this.apiManagerResponse.get(apiId), new TypeReference<List<APIMethod>>() {
            });
        } catch (IOException e) {
            throw new AppException("Error cant load API-Methods for API: '" + apiId + "' from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
        return apiMethods;
    }

    public APIMethod getMethodForName(String apiId, String methodName) throws AppException {
        List<APIMethod> apiMethods = getAllMethodsForAPI(apiId);
        if (apiMethods.size() == 0) {
            LOG.warn("No operations found for API with id: {}", apiId);
            return null;
        }
        for (APIMethod method : apiMethods) {
            String operationName = method.getName();
            if (operationName.equals(methodName)) {
                return method;
            }
        }
        throw new AppException("No operation found with name: '" + methodName + "'", ErrorCode.API_OPERATION_NOT_FOUND);
    }

    public APIMethod getMethodForId(String apiId, String methodId) throws AppException {
        List<APIMethod> apiMethods = getAllMethodsForAPI(apiId);
        if (apiMethods.size() == 0) {
            LOG.warn("No operations found for API with id: {}", apiId);
            return null;
        }
        for (APIMethod method : apiMethods) {
            String operationId = method.getId();
            if (operationId.equals(methodId)) {
                return method;
            }
        }
        LOG.warn("No operation found with ID: {} for API: {}", methodId, apiId);
        return null;
    }

    public void updateApiMethod(APIMethod apiMethod) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/proxies/" + apiMethod.getVirtualizedApiId() + "/operations/" + apiMethod.getId()).build();
            String json = mapper.writeValueAsString(apiMethod);
            HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            RestAPICall putRequest = new PUTRequest(entity, uri, APIManagerAdapter.hasAdminAccount());
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) putRequest.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                if (statusCode < 200 || statusCode > 299) {
                    throw new AppException("Can't update API-Manager Method. Response: '" + response + "'", ErrorCode.API_MANAGER_COMMUNICATION);
                } else {
                    LOG.info("Successfully updated API Method. Received Status-Code: {}", statusCode);
                }
            }
        } catch (Exception e) {
            LOG.error("Error cant update API-Methods for API: '" + apiMethod.getVirtualizedApiId() + "' from API-Manager", e);
            throw new AppException("Error cant load API-Methods for API: '" + apiMethod.getVirtualizedApiId() + "' from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    void setAPIManagerTestResponse(String apiId, String response) {
        this.apiManagerResponse.put(apiId, response);
    }
}
