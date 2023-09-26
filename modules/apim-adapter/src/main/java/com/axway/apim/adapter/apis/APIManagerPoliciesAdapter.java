package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.DeviceType;
import com.axway.apim.api.model.Policy;
import com.axway.apim.api.model.SecurityDevice;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

public class APIManagerPoliciesAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerPoliciesAdapter.class);

    public static final String TOKEN_STORE = "tokenStore";
    public static final String AUTHENTICATION_POLICY = "authenticationPolicy";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public enum PolicyType {
        ROUTING("routing", "routePolicy", "Routing policy"),
        REQUEST("request", "requestPolicy", "Request policy"),
        RESPONSE("response", "responsePolicy", "Response policy"),
        FAULT_HANDLER("faulthandler", "faultHandlerPolicy", "Fault-Handler"),
        GLOBAL_FAULT_HANDLER("faulthandler", "globalFaultHandlerPolicy", "Global Fault-Handler"),
        GLOBAL_REQUEST_HANDLER("globalrequest", "globalRequestPolicy", "Global Request Policy"),
        GLOBAL_RESPONSE_HANDLER("globalresponse", "globalResponsePolicy", "Global Response Policy"),

        AUTHENTICATION("authentication", AUTHENTICATION_POLICY, "Authentication Policy"),
        OAUTH_TOKEN_INFO("oauthtokeninfo", "oauthtokeninfoPolicy", "OAuth Token Info policy");

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


    public APIManagerPoliciesAdapter() {
        super();
    }

    public final Map<PolicyType, String> apiManagerResponse = new EnumMap<>(PolicyType.class);

    private final Map<PolicyType, List<Policy>> mappedPolicies = new EnumMap<>(PolicyType.class);
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

    public String getEntityStorePolicyFormat(PolicyType type, String name) throws AppException {
        String response = apiManagerResponse.get(type);
        if (apiManagerResponse.get(type) != null) return response;
        readPoliciesFromAPIManager(type);
        response = apiManagerResponse.get(type);
        try {
            JsonNode jsonResponse = objectMapper.readTree(response);
            for (JsonNode node : jsonResponse) {
                if (node.get("name").asText().equals(name))
                    return node.get("id").asText();
            }
        } catch (JsonProcessingException e) {
            throw new AppException("Can't read " + type.restAPIKey + " from response: ",
                ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
        return null;
    }

    public String getOauthTokenStore() throws AppException {
        CoreParameters cmd = CoreParameters.getInstance();
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/tokenstores").build();
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                LOG.debug("Get token stores Response code : {}", httpResponse.getStatusLine().getStatusCode());
                String response = EntityUtils.toString(httpResponse.getEntity());
                JsonNode jsonResponse = objectMapper.readTree(response);
                for (JsonNode node : jsonResponse) {
                    if (node.get("name").asText().equals(TOKEN_STORE))
                        return node.get("id").asText();
                }
            }
        } catch (Exception e) {
            throw new AppException("Can't read oauth toke store", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
        return null;
    }

    public void updateSecurityProfiles(API api) throws AppException {
        List<SecurityProfile> securityProfiles = api.getSecurityProfiles();
        if (securityProfiles != null && !securityProfiles.isEmpty()) {
            for (SecurityProfile securityProfile : securityProfiles) {
                for (SecurityDevice securityDevice : securityProfile.getDevices()) {
                    if (securityDevice.getType() == DeviceType.authPolicy) {
                        String authPolicy = securityDevice.getProperties().get(AUTHENTICATION_POLICY);
                        String entityStorePolicy = getEntityStorePolicyFormat(APIManagerPoliciesAdapter.PolicyType.AUTHENTICATION, authPolicy);
                        LOG.debug("Changing Auth policy : {} with {}", authPolicy, entityStorePolicy);
                        securityDevice.getProperties().put(AUTHENTICATION_POLICY, entityStorePolicy);
                    } else if (securityDevice.getType() == DeviceType.oauth) {
                        String oauthTokenStore = getOauthTokenStore();
                        securityDevice.getProperties().put(TOKEN_STORE, oauthTokenStore);
                    } else if (securityDevice.getType() == DeviceType.oauthExternal) {
                        String oauthTokenInfo = securityDevice.getProperties().get("oauthtokeninfo");
                        String entityStoreOauthTokenInfo = getEntityStorePolicyFormat(PolicyType.OAUTH_TOKEN_INFO, oauthTokenInfo);
                        Map<String, String>  properties = securityDevice.getProperties();
                        properties.put(TOKEN_STORE, entityStoreOauthTokenInfo);
                        properties.put("oauth.token.client_id", "${oauth.token.client_id}");
                        properties.put("oauth.token.scopes", "${oauth.token.scopes}");
                        properties.put("oauth.token.valid", "${oauth.token.valid}");
                    }
                }
            }
        }
    }

    public List<Policy> getAllPolicies() throws AppException {
        for (PolicyType type : PolicyType.values()) {
            initPoliciesType(type);
        }
        return allPolicies;
    }
}
