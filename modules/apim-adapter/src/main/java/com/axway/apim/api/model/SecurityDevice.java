package com.axway.apim.api.model;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SecurityDevice {

    protected static Logger LOG = LoggerFactory.getLogger(SecurityDevice.class);

    private static Map<String, String> oauthTokenStores;
    private static Map<String, String> oauthInfoPolicies;
    private static Map<String, String> authenticationPolicies;

    String name;

    DeviceType type;

    int order;

    Map<String, String> properties;

    /**
     * Flag to control if Policy-Names should be translated or not - Currently used by the API-Export
     */
    @JsonIgnore
    boolean convertPolicies = true;

    public SecurityDevice() {
        super();
        this.properties = new LinkedHashMap<>();
    }

    private static Map<String, String> initCustomPolicies(String type) throws AppException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> policyMap = new HashMap<>();
        CoreParameters cmd = CoreParameters.getInstance();
        HttpResponse httpResponse = null;
        InputStream is = null;
        JsonNode jsonResponse = null;
        URI uri;
        try {
            if (type.equals("tokenstores")) {
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/tokenstores").build();
            } else {
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/policies")
                        .setParameter("type", type).build();
            }
            RestAPICall getRequest = new GETRequest(uri);
            httpResponse = getRequest.execute();
            try {
                is = httpResponse.getEntity().getContent();
                jsonResponse = mapper.readTree(is);
                for (JsonNode node : jsonResponse) {
                    policyMap.put(node.get("name").asText(), node.get("id").asText());
                }
            } catch (IOException e) {
                throw new AppException("Can't read " + type + " from response: '" + jsonResponse + "'. "
                        + "Please make sure that you use an Admin-Role user.",
                        ErrorCode.API_MANAGER_COMMUNICATION, e);
            }
        } catch (Exception e) {
            throw new AppException("Can't read " + type + " from response: '" + jsonResponse + "'. "
                    + "Please make sure that you use an Admin-Role user.",
                    ErrorCode.API_MANAGER_COMMUNICATION, e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Exception ignore) {
            }
            try {
                if (httpResponse != null)
                    ((CloseableHttpResponse) httpResponse).close();
            } catch (Exception ignore) {
            }
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
            if (SecurityDevice.oauthTokenStores == null)
                SecurityDevice.oauthTokenStores = initCustomPolicies("tokenstores");
            String tokenStore = properties.get("tokenStore");
            if (tokenStore.startsWith("<key")) return properties;
            String esTokenStore = oauthTokenStores.get(tokenStore);
            if (esTokenStore == null) {
                LOG.error("The tokenstore: '" + tokenStore + "' is not configured in this API-Manager");
                LOG.error("Available token stores: " + oauthTokenStores.keySet());
                throw new AppException("The tokenstore: '" + tokenStore + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
            } else {
                properties.put("tokenStore", esTokenStore);
            }
        } else if (type == DeviceType.oauthExternal && this.convertPolicies) {
            if (SecurityDevice.oauthInfoPolicies == null)
                SecurityDevice.oauthInfoPolicies = initCustomPolicies("oauthtokeninfo");
            if (SecurityDevice.oauthTokenStores == null)
                SecurityDevice.oauthTokenStores = initCustomPolicies("tokenstores");
            String infoPolicy = properties.get("tokenStore"); // The token-info-policy is stored in the tokenStore as well
            if (infoPolicy.startsWith("<key")) return properties;
            String esInfoPolicy = oauthInfoPolicies.get(infoPolicy);
            if (esInfoPolicy == null) {
                LOG.error("The Information-Policy: '" + infoPolicy + "' is not configured in this API-Manager");
                LOG.error("Available information policies: " + oauthInfoPolicies.keySet());
                throw new AppException("The Information-Policy: '" + infoPolicy + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
            } else {
                properties.put("tokenStore", esInfoPolicy);
                properties.put("oauth.token.client_id", "${oauth.token.client_id}");
                properties.put("oauth.token.scopes", "${oauth.token.scopes}");
                properties.put("oauth.token.valid", "${oauth.token.valid}");
            }
        } else if (type == DeviceType.authPolicy && this.convertPolicies) {
            if (SecurityDevice.authenticationPolicies == null)
                SecurityDevice.authenticationPolicies = initCustomPolicies("authentication");
            String authPolicy = properties.get("authenticationPolicy");
            if (authPolicy.startsWith("<key")) return properties;
            String esAuthPolicy = authenticationPolicies.get(authPolicy);
            if (esAuthPolicy == null) {
                LOG.error("The Authentication-Policy: '" + authPolicy + "' is not configured in this API-Manager");
                LOG.error("Available authentication policies: " + authenticationPolicies.keySet());
                throw new AppException("The Authentication-Policy: '" + authPolicy + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
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
            //if(otherSecurityDevice.getOrder()!=this.getOrder()) return false; Ignore order check as 7.7.20211130 returning order id as  1 n whereas 7.7.20220830 returning order id as 0
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
