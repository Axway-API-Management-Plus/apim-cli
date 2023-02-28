package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Policy;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIManagerPoliciesAdapter {

    public enum PolicyType {
        ROUTING("routing", "routePolicy", "Routing policy"),
        REQUEST("request", "requestPolicy", "Request policy"),
        RESPONSE("response", "responsePolicy", "Response policy"),
        FAULT_HANDLER("faulthandler", "faultHandlerPolicy", "Fault-Handler"),
        GLOBAL_FAULT_HANDLER("faulthandler", "globalFaultHandlerPolicy", "Global Fault-Handler"),
        GLOBAL_REQUEST_HANDLER("globalrequest", "globalRequestPolicy", "Global Request Policy"),
        GLOBAL_RESPONSE_HANDLER("globalresponse", "globalResponsePolicy", "Global Response Policy");

        private final String restAPIKey;
        private final String jsonKey;
        private final String niceName;

        private static Map<String, PolicyType> jsonKeyToTypeMapping = null;

        PolicyType(String restAPIKey, String jsonKey, String niceName) {
            this.restAPIKey = restAPIKey;
            this.jsonKey = jsonKey;
            this.niceName = niceName;
        }

        public String getRestAPIKey() {
            return restAPIKey;
        }

        public String getJsonKey() {
            return jsonKey;
        }

        public String getNiceName() {
            return niceName;
        }

        private static void initMapping() {
            jsonKeyToTypeMapping = new HashMap<>();
            for (PolicyType type : values()) {
                jsonKeyToTypeMapping.put(type.getJsonKey(), type);
            }
        }

        public static PolicyType getTypeForJsonKey(String jsonKey) {
            if (jsonKeyToTypeMapping == null)
                initMapping();
            return jsonKeyToTypeMapping.get(jsonKey);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerPoliciesAdapter.class);

    public APIManagerPoliciesAdapter() {
        super();
    }

    public Map<PolicyType, String> apiManagerResponse = new HashMap<>();

    private final Map<PolicyType, List<Policy>> mappedPolicies = new HashMap<>();
    private final List<Policy> allPolicies = new ArrayList<>();

    private void readPoliciesFromAPIManager(PolicyType type) throws AppException {
        if (apiManagerResponse.get(type) != null) return;
        CoreParameters cmd = CoreParameters.getInstance();
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/policies")
                    .setParameter("type", type.getRestAPIKey()).build();
            LOG.debug("Load policies with type: {} from API-Manager", type);
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                apiManagerResponse.put(type, EntityUtils.toString(httpResponse.getEntity()));
            }
        } catch (Exception e) {
            throw new AppException("Can't initialize policies for type: " + type, ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    private void initPoliciesType(PolicyType type) throws AppException {
        if (this.mappedPolicies.get(type) == null) {
            readPoliciesFromAPIManager(type);
        }
        try {
            List<Policy> policies = APIManagerAdapter.mapper.readValue(apiManagerResponse.get(type), new TypeReference<List<Policy>>() {
            });
            for (Policy policy : policies) {
                policy.setType(type);
            }
            mappedPolicies.put(type, policies);
            allPolicies.addAll(policies);
        } catch (Exception e) {
            LOG.error("Error reading configured custom-policies. Can't parse response: {}", apiManagerResponse.get(type), e);
            throw new AppException("Can't initialize policies for type: " + type, ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public Policy getPolicyForName(PolicyType type, String name) throws AppException {
        initPoliciesType(type);
        List<Policy> policies = this.mappedPolicies.get(type);

        for (Policy policy : policies) {
            LOG.info("Policy Name : {}", policy.getName());
            if (policy.getName().equals(name)) {
                return policy;
            }
        }
        LOG.error("Available {} policies: {}", type.getRestAPIKey(), policies);
        throw new AppException("The " + type.getRestAPIKey() + " policy: '" + name + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
    }

    public List<Policy> getAllPolicies() throws AppException {
        for (PolicyType type : PolicyType.values()) {
            initPoliciesType(type);
        }
        return allPolicies;
    }
}
