package com.axway.apim.config;

import com.axway.apim.api.API;
import com.axway.apim.api.apiSpecification.APISpecification;
import com.axway.apim.api.apiSpecification.OAS3xSpecification;
import com.axway.apim.api.model.*;
import com.axway.apim.config.model.APISecurity;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"name", "path", "state", "version", "organization", "apiSpecification", "summary", "descriptionType", "descriptionManual", "vhost", "remoteHost",
        "backendBasepath", "image", "inboundProfiles", "outboundProfiles", "securityProfiles", "authenticationProfiles", "tags", "customProperties",
        "corsProfiles", "caCerts"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIConfig {

    private API api = null;
    private String apiDefinition;
    private Map<String, Object> securityProfiles;

    public APIConfig(API api, String apiDefinition, Map<String, Object> securityProfiles) {
        this.api = api;
        this.apiDefinition = apiDefinition;
        this.securityProfiles = securityProfiles;
    }

    public Map<String, OutboundProfile> getOutboundProfiles() throws AppException {
        if (api.getOutboundProfiles() == null) return null;
        if (api.getOutboundProfiles().isEmpty()) return null;
        if (api.getOutboundProfiles().size() == 1) {
            OutboundProfile defaultProfile = api.getOutboundProfiles().get("_default");
            if (defaultProfile.getRouteType().equals("proxy")
                    && defaultProfile.getAuthenticationProfile().equals("_default")
                    && defaultProfile.getRequestPolicy() == null
                    && defaultProfile.getResponsePolicy() == null
            ) return null;
        }
        for (OutboundProfile profile : api.getOutboundProfiles().values()) {
            profile.setApiId(null);
            // If the AuthenticationProfile is _default there is no need to export it, hence null is returned
            if ("_default".equals(profile.getAuthenticationProfile())) {
                profile.setAuthenticationProfile(null);
            }
        }
        return api.getOutboundProfiles();
    }


    public Map<String, Object> getSecurityProfiles() {
        if (securityProfiles.size() == 1) {
            List<APISecurity> apiSecurities = (List<APISecurity>) securityProfiles.get("devices");
            if (apiSecurities.get(0).getType().equals(DeviceType.passThrough.getName()))
                return null;
        }
        return securityProfiles;
    }


    public List<AuthenticationProfile> getAuthenticationProfiles() {
        if (api.getAuthenticationProfiles().size() == 1) {
            if (api.getAuthenticationProfiles().get(0).getType() == AuthType.none)
                return null;
        }
        for (AuthenticationProfile profile : api.getAuthenticationProfiles()) {
            if (profile.getType() == AuthType.oauth) {
                String providerProfile = (String) profile.getParameters().get("providerProfile");
                if (providerProfile.startsWith("<key")) {
                    providerProfile = providerProfile.substring(providerProfile.indexOf("<key type='OAuthAppProfile'>"));
                    providerProfile = providerProfile.substring(providerProfile.indexOf("value='") + 7, providerProfile.lastIndexOf("'/></key>"));
                }
                profile.getParameters().put("providerProfile", providerProfile);
            }
        }
        return api.getAuthenticationProfiles();
    }

    public Map<String, InboundProfile> getInboundProfiles() {
        if (api.getInboundProfiles() == null) return null;
        if (api.getInboundProfiles().isEmpty()) return null;
        if (api.getInboundProfiles().size() == 1) {
            InboundProfile defaultProfile = api.getInboundProfiles().get("_default");
            if (defaultProfile.getSecurityProfile().equals("_default")
                /*&& defaultProfile.getCorsProfile().equals("_default")*/) return null;
        }
        return api.getInboundProfiles();
    }


    public List<CorsProfile> getCorsProfiles() {

        return api.getCorsProfiles();
    }


    public String getVhost() {
        return api.getVhost();
    }


    public TagMap<String, String[]> getTags() {
        return api.getTags();
    }


    public String getState() {
        return api.getState();
    }


    public String getVersion() {
        return api.getVersion();
    }


    public String getSummary() {
        return api.getSummary();
    }

    public String getName() {
        return api.getName();
    }


    public String getOrganization() {
        Organization organization = api.getOrganization();
        if (organization == null)
            return "API Development";
        return api.getOrganization().getName();
    }


    public Map<String, String> getCustomProperties() {
        if (api.getCustomProperties() == null || api.getCustomProperties().size() == 0)
            return null;
        Iterator<String> it = api.getCustomProperties().values().iterator();
        boolean propertyFound = false;
        while (it.hasNext()) {
            String propValue = it.next();
            if (propValue != null) {
                propertyFound = true;
                break;
            }
        }
        if (!propertyFound) return null; // If no property is declared for this API return null
        return api.getCustomProperties();
    }

    public String getDescriptionType() {
        if (api.getDescriptionType().equals("original")) return null;
        return api.getDescriptionType();
    }


    public String getDescriptionManual() {
        return api.getDescriptionManual();
    }


    public String getDescriptionMarkdown() {
        return api.getDescriptionMarkdown();
    }


    public String getDescriptionUrl() {
        return api.getDescriptionUrl();
    }


    public List<CaCert> getCaCerts() {
        if (api.getCaCerts() == null) return null;
        if (api.getCaCerts().size() == 0) return null;
        return api.getCaCerts();
    }


    @JsonIgnore
    public Map<String, ServiceProfile> getServiceProfiles() {
        return api.getServiceProfiles();
    }


    public String getBackendBasepath() {
        return api.getResourcePath();
    }

    public Map<String, String> getApiSpecification() {
        Map<String, String> apiSpec = new HashMap<>();
        apiSpec.put("resource", apiDefinition);
        return apiSpec;
    }

}
