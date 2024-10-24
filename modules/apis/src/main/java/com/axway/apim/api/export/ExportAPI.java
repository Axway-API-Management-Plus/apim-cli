package com.axway.apim.api.export;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.*;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.specification.APISpecification;
import com.axway.apim.api.specification.WSDLSpecification;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.*;

@JsonPropertyOrder({"name", "path", "state", "version", "apiRoutingKey", "organization", "apiSpecification", "summary", "descriptionType", "descriptionManual", "vhost", "remoteHost", "backendBasepath", "image", "inboundProfiles", "outboundProfiles", "securityProfiles", "authenticationProfiles", "tags", "customProperties", "corsProfiles", "caCerts", "applicationQuota", "systemQuota", "apiMethods", "clientOrganizations", "applications"})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class ExportAPI {
    private static final String DEFAULT = "_default";
    public static final String TOKEN_STORE = "tokenStore";
    public static final String AUTHENTICATION_POLICY = "authenticationPolicy";
    private final API actualAPIProxy;

    public ExportAPI(API actualAPIProxy) {
        this.actualAPIProxy = actualAPIProxy;
    }

    public String getPath() {
        return actualAPIProxy.getPath();
    }

    @JsonIgnore
    public APISpecification getAPIDefinition() {
        return actualAPIProxy.getApiDefinition();
    }

    public Map<String, OutboundProfile> getOutboundProfiles() {
        if (actualAPIProxy.getOutboundProfiles() == null || actualAPIProxy.getOutboundProfiles().isEmpty())
            return Collections.emptyMap();
        if (actualAPIProxy.getOutboundProfiles().size() == 1) {
            OutboundProfile defaultProfile = actualAPIProxy.getOutboundProfiles().get(DEFAULT);
            if (defaultProfile.getRouteType().equals("proxy") && defaultProfile.getAuthenticationProfile().equals(DEFAULT) && defaultProfile.getRequestPolicy() == null && defaultProfile.getResponsePolicy() == null && defaultProfile.getFaultHandlerPolicy() == null)
                return Collections.emptyMap();
        }
        for (OutboundProfile profile : actualAPIProxy.getOutboundProfiles().values()) {
            profile.setApiId(null);
            // If the AuthenticationProfile is _default there is no need to export it, hence null is returned
            if (DEFAULT.equals(profile.getAuthenticationProfile())) {
                profile.setAuthenticationProfile(null);
            }
        }
        return actualAPIProxy.getOutboundProfiles();
    }


    public List<SecurityProfile> getSecurityProfiles() {
        if (actualAPIProxy.getSecurityProfiles().size() == 1) {
            if (actualAPIProxy.getSecurityProfiles().get(0).getDevices().isEmpty()) return Collections.emptyList();
            if (actualAPIProxy.getSecurityProfiles().get(0).getDevices().get(0).getType() == DeviceType.passThrough)
                return Collections.emptyList();
        }
        expandTokenStoreAndAuthPolicy();
        return actualAPIProxy.getSecurityProfiles();
    }

    private void expandTokenStoreAndAuthPolicy() {
        for (SecurityProfile profile : actualAPIProxy.getSecurityProfiles()) {
            for (SecurityDevice device : profile.getDevices()) {

                DeviceType type = device.getType();
                Map<String, String> properties = device.getProperties();

                if (type == DeviceType.oauth) {
                    String tokenStore = properties.get(TOKEN_STORE);
                    String tokenStoreName = Utils.getExternalPolicyName(tokenStore, Utils.FedKeyType.OAuthTokenProfile);
                    properties.put(TOKEN_STORE, tokenStoreName);
                } else if (type == DeviceType.oauthExternal) {
                    String tokenStore = properties.get(TOKEN_STORE);
                    String tokenStoreName = Utils.getExternalPolicyName(tokenStore);
                    properties.put(TOKEN_STORE, tokenStoreName);
                } else if (type == DeviceType.authPolicy) {
                    String authenticationPolicy = properties.get(AUTHENTICATION_POLICY);
                    String authenticationPolicyName = Utils.getExternalPolicyName(authenticationPolicy);
                    properties.put(AUTHENTICATION_POLICY, authenticationPolicyName);
                }

            }
        }
    }


    public List<AuthenticationProfile> getAuthenticationProfiles() {
        if (actualAPIProxy.getAuthenticationProfiles().size() == 1 && actualAPIProxy.getAuthenticationProfiles().get(0).getType() == AuthType.none)
            return Collections.emptyList();
        for (AuthenticationProfile profile : actualAPIProxy.getAuthenticationProfiles()) {
            if (profile.getType() == AuthType.oauth) {
                String providerProfile = (String) profile.getParameters().get("providerProfile");
                if (providerProfile.startsWith("<key")) {
                    providerProfile = providerProfile.substring(providerProfile.indexOf("<key type='OAuthAppProfile'>"));
                    providerProfile = providerProfile.substring(providerProfile.indexOf("value='") + 7, providerProfile.lastIndexOf("'/></key>"));
                }
                profile.getParameters().put("providerProfile", providerProfile);
            }
        }
        return actualAPIProxy.getAuthenticationProfiles();
    }

    public Map<String, InboundProfile> getInboundProfiles() {
        if (actualAPIProxy.getInboundProfiles() == null || actualAPIProxy.getInboundProfiles().isEmpty())
            return Collections.emptyMap();
        return actualAPIProxy.getInboundProfiles();
    }


    public List<CorsProfile> getCorsProfiles() {
        if (actualAPIProxy.getCorsProfiles() == null || actualAPIProxy.getCorsProfiles().isEmpty())
            return Collections.emptyList();
        if (actualAPIProxy.getCorsProfiles().size() == 1) {
            CorsProfile corsProfile = actualAPIProxy.getCorsProfiles().get(0);
            if (corsProfile.equals(CorsProfile.getDefaultCorsProfile())) return Collections.emptyList();
        }
        return actualAPIProxy.getCorsProfiles();
    }


    public String getVhost() {
        return actualAPIProxy.getVhost();
    }

    public String getRemoteHost() {
        if (actualAPIProxy.getRemotehost() == null) return null;
        RemoteHost remoteHost = actualAPIProxy.getRemotehost();
        if (remoteHost.getPort() == 443 || remoteHost.getPort() == 80) {
            return remoteHost.getName();
        } else {
            return remoteHost.getName() + ":" + remoteHost.getPort();
        }
    }


    public TagMap getTags() {
        if (actualAPIProxy.getTags() == null || actualAPIProxy.getTags().isEmpty()) return new TagMap();
        return actualAPIProxy.getTags();
    }


    public String getState() {
        return actualAPIProxy.getState();
    }


    public String getVersion() {
        return actualAPIProxy.getVersion();
    }


    public String getSummary() {
        return actualAPIProxy.getSummary();
    }


    public String getImage() {
        if (actualAPIProxy.getImage() == null) return null;
        // We don't have an Image provided from the API-Manager
        if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            byte[] imageData = actualAPIProxy.getImage().getImageContent();
            Base64.Encoder encoder = Base64.getEncoder();
            String encodedData = encoder.encodeToString(imageData);
            String contentType = actualAPIProxy.getImage().getContentType();
            return "data:" + contentType + ";base64," + encodedData;

        } else {
            return "api-image" + actualAPIProxy.getImage().getFileExtension();
        }
    }

    @JsonIgnore
    public Image getAPIImage() {
        if (actualAPIProxy.getImage() == null) return null;
        return actualAPIProxy.getImage();
    }


    public String getName() {
        return actualAPIProxy.getName();
    }

    public String getApiRoutingKey() {
        return actualAPIProxy.getApiRoutingKey();
    }


    public String getOrganization() {
        return actualAPIProxy.getOrganization().getName();
    }

    @JsonIgnore
    public String getOrganizationId() {
        return actualAPIProxy.getOrganization().getId();
    }


    @JsonIgnore
    public String getDeprecated() {
        return actualAPIProxy.getDeprecated();
    }

    public Map<String, String> getCustomProperties() {
        if (actualAPIProxy.getCustomProperties() == null || actualAPIProxy.getCustomProperties().isEmpty())
            return Collections.emptyMap();
        Iterator<String> it = actualAPIProxy.getCustomProperties().values().iterator();
        boolean propertyFound = false;
        while (it.hasNext()) {
            String propValue = it.next();
            if (propValue != null) {
                propertyFound = true;
                break;
            }
        }
        if (!propertyFound) return Collections.emptyMap(); // If no property is declared for this API return null
        return actualAPIProxy.getCustomProperties();
    }

    public String getDescriptionType() {
        if (actualAPIProxy.getDescriptionType().equals("original")) return null;
        return actualAPIProxy.getDescriptionType();
    }


    public String getDescriptionManual() {
        return actualAPIProxy.getDescriptionManual();
    }


    public String getDescriptionMarkdown() {
        return actualAPIProxy.getDescriptionMarkdown();
    }


    public String getDescriptionUrl() {
        return actualAPIProxy.getDescriptionUrl();
    }


    public List<CaCert> getCaCerts() {
        if (actualAPIProxy.getCaCerts() == null) return Collections.emptyList();
        if (actualAPIProxy.getCaCerts().isEmpty()) return Collections.emptyList();
        return actualAPIProxy.getCaCerts();
    }


    public APIQuota getApplicationQuota() {
        return actualAPIProxy.getApplicationQuota();
    }


    public APIQuota getSystemQuota() {
        return actualAPIProxy.getSystemQuota();
    }

    @JsonIgnore
    public Map<String, ServiceProfile> getServiceProfiles() {
        return actualAPIProxy.getServiceProfiles();
    }

    public List<String> getClientOrganizations() throws AppException {
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) return Collections.emptyList();
        if (actualAPIProxy.getClientOrganizations().isEmpty()) return Collections.emptyList();
        if (actualAPIProxy.getClientOrganizations().size() == 1 && actualAPIProxy.getClientOrganizations().get(0).getName().equals(getOrganization()))
            return Collections.emptyList();
        List<String> organizations = new ArrayList<>();
        for (Organization org : actualAPIProxy.getClientOrganizations()) {
            if (!org.getName().equals(actualAPIProxy.getOrganization().getName())) // Ignore the development organization
                organizations.add(org.getName());
        }
        return organizations;
    }

    public List<Map<String, String>> getApplications() {
        if (actualAPIProxy.getApplications().isEmpty()) return Collections.emptyList();
        List<Map<String, String>> exportApps = new ArrayList<>();
        for (ClientApplication app : actualAPIProxy.getApplications()) {
            Map<String, String> applications = new HashMap<>();
            applications.put("name", app.getName());
            applications.put("organization", app.getOrganization().getName());
            exportApps.add(applications);
        }
        return exportApps;
    }

    @JsonProperty("apiSpecification")
    public DesiredAPISpecification getApiDefinitionImport() {
        DesiredAPISpecification spec = new DesiredAPISpecification();
        if (getAPIDefinition() instanceof WSDLSpecification && EnvironmentProperties.RETAIN_BACKEND_URL) {
            spec.setResource(actualAPIProxy.getBackendImportedUrl());
        } else if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            String filename = getAPIDefinition().getApiSpecificationFile();
            String contentType = "data:text/plain;base64,";
            if (filename.endsWith("json")) {
                contentType = "data:application/json;base64,";
            } else if (filename.endsWith("yaml") || filename.endsWith("yml")) {
                contentType = "data:application/x-yaml;base64,";
            }
            spec.setResource(contentType + Base64.getEncoder().encodeToString(getAPIDefinition().getApiSpecificationContent()));
        } else {
            spec.setResource(getAPIDefinition().getApiSpecificationFile());
        }
        return spec;
    }


    public String getBackendBasepath() {
        //ISSUE-299
        // Resource path is part of API specification (like open api servers.url or swagger basePath) and we don't need to manage it in config file.
        String backendBasePath = getServiceProfiles().get(DEFAULT).getBasePath();
        if (CoreParameters.getInstance().isOverrideSpecBasePath() && actualAPIProxy.getResourcePath() != null) { //Issue 354
            backendBasePath = backendBasePath + actualAPIProxy.getResourcePath();
        }
        return backendBasePath;
    }

    public List<APIMethod> getApiMethods() {
        List<APIMethod> apiMethods = actualAPIProxy.getApiMethods();
        if (apiMethods == null || apiMethods.isEmpty()) return Collections.emptyList();
        List<APIMethod> apiMethodsTransformed = new ArrayList<>();
        for (APIMethod actualMethod : apiMethods) {
            APIMethod apiMethod = new APIMethod();
            apiMethod.setName(actualMethod.getName());
            apiMethod.setSummary(actualMethod.getSummary());
            TagMap tagMap = actualMethod.getTags();
            if (tagMap != null && !tagMap.isEmpty()) apiMethod.setTags(actualMethod.getTags());
            apiMethodsTransformed.add(apiMethod);
            String descriptionType = actualMethod.getDescriptionType();
            switch (descriptionType) {
                case "manual":
                    apiMethod.setDescriptionManual(actualMethod.getDescriptionManual());
                    break;
                case "url":
                    apiMethod.setDescriptionUrl(actualMethod.getDescriptionUrl());
                    break;
                case "markdown":
                    apiMethod.setDescriptionMarkdown(actualMethod.getDescriptionMarkdown());
                    break;
                default:
                    break;
            }
            apiMethod.setDescriptionType(descriptionType);
        }
        return apiMethodsTransformed;
    }
}
