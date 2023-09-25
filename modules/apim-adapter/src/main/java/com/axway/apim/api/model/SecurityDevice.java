package com.axway.apim.api.model;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SecurityDevice {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityDevice.class);
    public static final String TOKENSTORES = "tokenstores";
    public static final String TOKEN_STORE = "tokenStore";
    public static final String NOT_CONFIGURED_IN_THIS_API_MANAGER = "' is not configured in this API-Manager";
    private Map<String, String> oauthTokenStores;
    private Map<String, String> oauthInfoPolicies;
    private Map<String, String> authenticationPolicies;
    private String name;
    private DeviceType type;
    int order;
    private Map<String, String> properties;

    /**
     * Flag to control if Policy-Names should be translated or not - Currently used by the API-Export
     */
    @JsonIgnore
    boolean convertPolicies = true;

    public SecurityDevice() {
        super();
        this.properties = new LinkedHashMap<>();
    }

    public Map<String, String> initCustomPolicies(String type) throws AppException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> policyMap = new HashMap<>();
        CoreParameters cmd = CoreParameters.getInstance();
        JsonNode jsonResponse;
        URI uri;
        try {
            if (type.equals(TOKENSTORES)) {
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/tokenstores").build();
            } else {
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/policies")
                    .setParameter("type", type).build();
            }
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                LOG.debug("Status code : {}", httpResponse.getStatusLine());
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    jsonResponse = mapper.readTree(response);
                    for (JsonNode node : jsonResponse) {
                        policyMap.put(node.get("name").asText(), node.get("id").asText());
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Response : {}", response);
                    }
                    throw new AppException("Error reading data from api manager", ErrorCode.API_MANAGER_COMMUNICATION);
                }
            }
        } catch (Exception e) {
            throw new AppException("Can't read " + type + " from response Please make sure that you use an Admin-Role user.",
                ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
        return policyMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "SecurityDevice{" +
            "name='" + name + '\'' +
            ", type=" + type +
            ", order=" + order +
            ", properties=" + properties +
            ", convertPolicies=" + convertPolicies +
            '}';
    }

    public Map<String, String> getProperties() throws AppException {
        if (type == DeviceType.oauth) {
            if (oauthTokenStores == null)
                oauthTokenStores = initCustomPolicies(TOKENSTORES);
            String tokenStore = properties.get(TOKEN_STORE);
            if (tokenStore.startsWith("<key")) return properties;
            String esTokenStore = oauthTokenStores.get(tokenStore);
            if (esTokenStore == null) {
                LOG.error("The token store: {} is not configured in this API-Manager", tokenStore);
                LOG.error("Available token stores: {}", oauthTokenStores.keySet());
                throw new AppException("The token store: '" + tokenStore + NOT_CONFIGURED_IN_THIS_API_MANAGER, ErrorCode.UNKNOWN_CUSTOM_POLICY);
            } else {
                properties.put(TOKEN_STORE, esTokenStore);
            }
        } else if (type == DeviceType.oauthExternal && this.convertPolicies) {
            if (oauthInfoPolicies == null)
                oauthInfoPolicies = initCustomPolicies("oauthtokeninfo");
            if (oauthTokenStores == null)
                oauthTokenStores = initCustomPolicies(TOKENSTORES);
            String infoPolicy = properties.get(TOKEN_STORE); // The token-info-policy is stored in the tokenStore as well
            if (infoPolicy.startsWith("<key")) return properties;
            String esInfoPolicy = oauthInfoPolicies.get(infoPolicy);
            if (esInfoPolicy == null) {
                LOG.error("The Information-Policy: {} is not configured in this API-Manager", infoPolicy);
                LOG.error("Available information policies: {}", oauthInfoPolicies.keySet());
                throw new AppException("The Information-Policy: '" + infoPolicy + NOT_CONFIGURED_IN_THIS_API_MANAGER, ErrorCode.UNKNOWN_CUSTOM_POLICY);
            } else {
                properties.put(TOKEN_STORE, esInfoPolicy);
                properties.put("oauth.token.client_id", "${oauth.token.client_id}");
                properties.put("oauth.token.scopes", "${oauth.token.scopes}");
                properties.put("oauth.token.valid", "${oauth.token.valid}");
            }
        } else if (type == DeviceType.authPolicy && this.convertPolicies) {
            if (authenticationPolicies == null)
                authenticationPolicies = initCustomPolicies("authentication");
            String authPolicy = properties.get("authenticationPolicy");
            if (authPolicy.startsWith("<key")) return properties;
            String esAuthPolicy = authenticationPolicies.get(authPolicy);
            if (esAuthPolicy == null) {
                LOG.error("The Authentication-Policy: {} is not configured in this API-Manager", authPolicy);
                LOG.error("Available authentication policies: {}", authenticationPolicies.keySet());
                throw new AppException("The Authentication-Policy: '" + authPolicy + NOT_CONFIGURED_IN_THIS_API_MANAGER, ErrorCode.UNKNOWN_CUSTOM_POLICY);
            } else {
                properties.put("authenticationPolicy", esAuthPolicy);
            }
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }


    public void setConvertPolicies(boolean convertPolicies) {
        this.convertPolicies = convertPolicies;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof SecurityDevice) {
            SecurityDevice otherSecurityDevice = (SecurityDevice) other;
            if (!StringUtils.equals(otherSecurityDevice.getName(), this.getName())) return false;
            if (!StringUtils.equals(otherSecurityDevice.getType().getName(), this.getType().getName())) return false;
            //Ignore order check as 7.7.20211130 returning order id as  1 n whereas 7.7.20220830 returning order id as 0
            try {
                if (!otherSecurityDevice.getProperties().equals(this.getProperties())) return false;
            } catch (AppException e) {
                LOG.error("Cant compare SecurityDevices", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, properties);
    }
}
