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

    public final Map<PolicyType, String> apiManagerResponse = new EnumMap<>(PolicyType.class);

    private static final Map<String, PolicyType> jsonKeyToTypeMapping = new HashMap<>();
    private final Map<PolicyType, List<Policy>> mappedPolicies = new EnumMap<>(PolicyType.class);
    private final List<Policy> allPolicies = new ArrayList<>();

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

        public static PolicyType getTypeForJsonKey(String jsonKey) {
            return jsonKeyToTypeMapping.get(jsonKey);
        }
    }

    public APIManagerPoliciesAdapter() {
        for (PolicyType type : PolicyType.values()) {
            jsonKeyToTypeMapping.put(type.getJsonKey(), type);
        }
    }

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
            LOG.debug("Policy Name : {}", policy.getName());
            if (policy.getName().equals(name)) {
                return policy;
            }
        }
        LOG.error("Available {} policies: {}", type.getRestAPIKey(), policies);
        throw new AppException("The " + type.getRestAPIKey() + " policy: '" + name + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
    }

    public String getEntityStorePolicyFormat(PolicyType type, String name) throws AppException {
        String response = apiManagerResponse.get(type);
        if (response == null)
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

    public String getOauthTokenStore(String name) throws AppException {
        CoreParameters cmd = CoreParameters.getInstance();
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/tokenstores").build();
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                LOG.debug("Get token stores Response code : {}", httpResponse.getStatusLine().getStatusCode());
                String response = EntityUtils.toString(httpResponse.getEntity());
                JsonNode jsonResponse = objectMapper.readTree(response);
                for (JsonNode node : jsonResponse) {
                    if (node.get("name").asText().equals(name))
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
                        handleAuthenticationPolicy(securityDevice);
                    } else if (securityDevice.getType() == DeviceType.oauth) {
                       handleOauth(securityDevice);
                    } else if (securityDevice.getType() == DeviceType.oauthExternal) {
                       handleExternalOauth(securityDevice);
                    }
                }
            }
        }
    }

    public void handleAuthenticationPolicy(SecurityDevice securityDevice) throws AppException {
        String authPolicy = securityDevice.getProperties().get(AUTHENTICATION_POLICY);
        String entityStorePolicy = getEntityStorePolicyFormat(APIManagerPoliciesAdapter.PolicyType.AUTHENTICATION, authPolicy);
        if (entityStorePolicy == null)
            throw new AppException("Invalid authentication policy : " + authPolicy, ErrorCode.INVALID_SECURITY_PROFILE_CONFIG);
        LOG.debug("Changing Auth policy : {} with {}", authPolicy, entityStorePolicy);
        securityDevice.getProperties().put(AUTHENTICATION_POLICY, entityStorePolicy);
    }

    public void handleOauth(SecurityDevice securityDevice) throws AppException {
        String tokenStore = securityDevice.getProperties().get(TOKEN_STORE);
        String oauthTokenStore = getOauthTokenStore(tokenStore);
        if (oauthTokenStore == null)
            throw new AppException("Oauth auth store is not configured", ErrorCode.UNXPECTED_ERROR);
        securityDevice.getProperties().put(TOKEN_STORE, oauthTokenStore);
    }

    public void handleExternalOauth(SecurityDevice securityDevice) throws AppException{
        String oauthTokenInfo = securityDevice.getProperties().get(TOKEN_STORE);
        String entityStoreOauthTokenInfo = getEntityStorePolicyFormat(PolicyType.OAUTH_TOKEN_INFO, oauthTokenInfo);
        if (entityStoreOauthTokenInfo == null)
            throw new AppException("Invalid Oauth token info policy : " + oauthTokenInfo, ErrorCode.INVALID_SECURITY_PROFILE_CONFIG);
        LOG.debug("Changing Auth policy : {} with {}", oauthTokenInfo, entityStoreOauthTokenInfo);
        Map<String, String> properties = securityDevice.getProperties();
        properties.put(TOKEN_STORE, entityStoreOauthTokenInfo);
    }

    public List<Policy> getAllPolicies() throws AppException {
        for (PolicyType type : PolicyType.values()) {
            initPoliciesType(type);
        }
        return allPolicies;
    }
}
