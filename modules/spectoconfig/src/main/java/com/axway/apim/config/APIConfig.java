package com.axway.apim.config;

import com.axway.apim.api.API;
import com.axway.apim.api.model.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"name", "path", "state", "version", "organization", "apiSpecification", "summary", "descriptionType", "descriptionManual", "vhost", "remoteHost",
    "backendBasepath", "image", "inboundProfiles", "outboundProfiles", "securityProfiles", "authenticationProfiles", "tags", "customProperties",
    "corsProfiles", "caCerts"})
public class APIConfig {

    public static final String DEFAULT = "_default";
    private final API api;
    private final String apiDefinition;

    public APIConfig(API api, String apiDefinition) {
        this.api = api;
        this.apiDefinition = apiDefinition;
    }

    public Map<String, OutboundProfile> getOutboundProfiles() {
        if (api.getOutboundProfiles() == null) return Collections.emptyMap();
        if (api.getOutboundProfiles().isEmpty()) return Collections.emptyMap();
        if (api.getOutboundProfiles().size() == 1) {
            OutboundProfile defaultProfile = api.getOutboundProfiles().get(DEFAULT);
            if (defaultProfile.getRouteType().equals("proxy")
                && defaultProfile.getAuthenticationProfile().equals(DEFAULT)
                && defaultProfile.getRequestPolicy() == null
                && defaultProfile.getResponsePolicy() == null
            ) return Collections.emptyMap();
        }
        for (OutboundProfile profile : api.getOutboundProfiles().values()) {
            profile.setApiId(null);
            // If the AuthenticationProfile is _default there is no need to export it, hence null is returned
            if (DEFAULT.equals(profile.getAuthenticationProfile())) {
                profile.setAuthenticationProfile(null);
            }
        }
        return api.getOutboundProfiles();
    }


    public List<SecurityProfile> getSecurityProfiles() {
        return api.getSecurityProfiles();
    }

    public String getPath() {
        return api.getPath();
    }


    public List<AuthenticationProfile> getAuthenticationProfiles() {
        if (api.getAuthenticationProfiles().size() == 1 && api.getAuthenticationProfiles().get(0).getType() == AuthType.none) {
            return Collections.emptyList();
        }
        return api.getAuthenticationProfiles();
    }

    public Map<String, InboundProfile> getInboundProfiles() {
        if (api.getInboundProfiles() == null) return Collections.emptyMap();
        if (api.getInboundProfiles().isEmpty()) return Collections.emptyMap();
        if (api.getInboundProfiles().size() == 1) {
            InboundProfile defaultProfile = api.getInboundProfiles().get(DEFAULT);
            if (defaultProfile.getSecurityProfile().equals(DEFAULT)
                && defaultProfile.getCorsProfile().equals(DEFAULT)) return Collections.emptyMap();
        }
        return api.getInboundProfiles();
    }

    public List<CorsProfile> getCorsProfiles() {

        return api.getCorsProfiles();
    }


    public String getVhost() {
        return api.getVhost();
    }


    public TagMap getTags() {
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
        if (api.getCaCerts() == null) return Collections.emptyList();
        if (api.getCaCerts().isEmpty()) return Collections.emptyList();
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
