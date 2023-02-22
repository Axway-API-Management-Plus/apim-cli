package com.axway.apim.api.export;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.specification.APISpecification;
import com.axway.apim.api.specification.WSDLSpecification;
import com.axway.apim.api.model.*;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"name", "path", "state", "version", "apiRoutingKey", "organization", "apiSpecification", "summary", "descriptionType", "descriptionManual", "vhost", "remoteHost",
        "backendBasepath", "image", "inboundProfiles", "outboundProfiles", "securityProfiles", "authenticationProfiles", "tags", "customProperties",
        "corsProfiles", "caCerts", "applicationQuota", "systemQuota", "apiMethods"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ExportAPI {
    API actualAPIProxy = null;

    public String getPath() {
        return this.actualAPIProxy.getPath();
    }

    public ExportAPI() {
    }

    public ExportAPI(API actualAPIProxy) {
        super();
        this.actualAPIProxy = actualAPIProxy;
    }

    @JsonIgnore
    public APISpecification getAPIDefinition() {
        return this.actualAPIProxy.getApiDefinition();
    }

    public Map<String, OutboundProfile> getOutboundProfiles() {
        if (this.actualAPIProxy.getOutboundProfiles() == null) return null;
        if (this.actualAPIProxy.getOutboundProfiles().isEmpty()) return null;
        if (this.actualAPIProxy.getOutboundProfiles().size() == 1) {
            OutboundProfile defaultProfile = this.actualAPIProxy.getOutboundProfiles().get("_default");
            if (defaultProfile.getRouteType().equals("proxy")
                    && defaultProfile.getAuthenticationProfile().equals("_default")
                    && defaultProfile.getRequestPolicy() == null
                    && defaultProfile.getResponsePolicy() == null
                    && defaultProfile.getFaultHandlerPolicy() == null)
                return null;
        }
        for (OutboundProfile profile : this.actualAPIProxy.getOutboundProfiles().values()) {
            profile.setApiId(null);
            // If the AuthenticationProfile is _default there is no need to export it, hence null is returned
            if ("_default".equals(profile.getAuthenticationProfile())) {
                profile.setAuthenticationProfile(null);
            }
        }
        return this.actualAPIProxy.getOutboundProfiles();
    }


    public List<SecurityProfile> getSecurityProfiles() throws AppException {
        if (this.actualAPIProxy.getSecurityProfiles().size() == 1) {
            if (this.actualAPIProxy.getSecurityProfiles().get(0).getDevices().isEmpty())
                return null;
            if (this.actualAPIProxy.getSecurityProfiles().get(0).getDevices().get(0).getType() == DeviceType.passThrough)
                return null;
        }
        for (SecurityProfile profile : this.actualAPIProxy.getSecurityProfiles()) {
            for (SecurityDevice device : profile.getDevices()) {
                if (device.getType().equals(DeviceType.oauthExternal)) {
                    String tokenStore = device.getProperties().get("tokenStore");
                    if (tokenStore != null) {
                        device.getProperties().put("tokenStore", Utils.getExternalPolicyName(tokenStore));
                    }
                } else if (device.getType().equals(DeviceType.authPolicy)) {
                    String authenticationPolicy = device.getProperties().get("authenticationPolicy");
                    if (authenticationPolicy != null) {
                        device.getProperties().put("authenticationPolicy", Utils.getExternalPolicyName(authenticationPolicy));
                    }
                }
                device.setConvertPolicies(false);
            }
        }
        return this.actualAPIProxy.getSecurityProfiles();
    }


    public List<AuthenticationProfile> getAuthenticationProfiles() {
        if (this.actualAPIProxy.getAuthenticationProfiles().size() == 1) {
            if (this.actualAPIProxy.getAuthenticationProfiles().get(0).getType() == AuthType.none)
                return null;
        }
        for (AuthenticationProfile profile : this.actualAPIProxy.getAuthenticationProfiles()) {
            if (profile.getType() == AuthType.oauth) {
                String providerProfile = (String) profile.getParameters().get("providerProfile");
                if (providerProfile.startsWith("<key")) {
                    providerProfile = providerProfile.substring(providerProfile.indexOf("<key type='OAuthAppProfile'>"));
                    providerProfile = providerProfile.substring(providerProfile.indexOf("value='") + 7, providerProfile.lastIndexOf("'/></key>"));
                }
                profile.getParameters().put("providerProfile", providerProfile);
            }
        }
        return this.actualAPIProxy.getAuthenticationProfiles();
    }

    public Map<String, InboundProfile> getInboundProfiles() {
        if (this.actualAPIProxy.getInboundProfiles() == null) return null;
        if (this.actualAPIProxy.getInboundProfiles().isEmpty()) return null;
        if (this.actualAPIProxy.getInboundProfiles().size() == 1) {
            InboundProfile defaultProfile = this.actualAPIProxy.getInboundProfiles().get("_default");
            if (defaultProfile.getSecurityProfile().equals("_default")
                    && defaultProfile.getCorsProfile().equals("_default")) return null;
        }
        return this.actualAPIProxy.getInboundProfiles();
    }


    public List<CorsProfile> getCorsProfiles() {
        if (this.actualAPIProxy.getCorsProfiles() == null) return null;
        if (this.actualAPIProxy.getCorsProfiles().isEmpty()) return null;
        if (this.actualAPIProxy.getCorsProfiles().size() == 1) {
            CorsProfile corsProfile = this.actualAPIProxy.getCorsProfiles().get(0);
            if (corsProfile.equals(CorsProfile.getDefaultCorsProfile())) return null;
        }
        return this.actualAPIProxy.getCorsProfiles();
    }


    public String getVhost() {
        return this.actualAPIProxy.getVhost();
    }

    public String getRemoteHost() {
        if (this.actualAPIProxy.getRemotehost() == null) return null;
        RemoteHost remoteHost = this.actualAPIProxy.getRemotehost();
        if (remoteHost.getPort() == 443 || remoteHost.getPort() == 80) {
            return remoteHost.getName();
        } else {
            return remoteHost.getName() + ":" + remoteHost.getPort();
        }
    }


    public TagMap getTags() {
        if (this.actualAPIProxy.getTags() == null) return null;
        if (this.actualAPIProxy.getTags().isEmpty()) return null;
        return this.actualAPIProxy.getTags();
    }


    public String getState() {
        return this.actualAPIProxy.getState();
    }


    public String getVersion() {
        return this.actualAPIProxy.getVersion();
    }


    public String getSummary() {
        return this.actualAPIProxy.getSummary();
    }


    public String getImage() {
        if (this.actualAPIProxy.getImage() == null) return null;
        // We don't have an Image provided from the API-Manager
        return "api-image" + this.actualAPIProxy.getImage().getFileExtension();
    }

    @JsonIgnore
    public Image getAPIImage() {
        if (this.actualAPIProxy.getImage() == null) return null;
        return this.actualAPIProxy.getImage();
    }


    public String getName() {
        return this.actualAPIProxy.getName();
    }

    public String getApiRoutingKey() {
        return this.actualAPIProxy.getApiRoutingKey();
    }


    public String getOrganization() {
        return this.actualAPIProxy.getOrganization().getName();
    }

    @JsonIgnore
    public String getOrganizationId() {
        return this.actualAPIProxy.getOrganization().getId();
    }


    @JsonIgnore
    public String getDeprecated() {
        return this.actualAPIProxy.getDeprecated();
    }

    public Map<String, String> getCustomProperties() {
        if (this.actualAPIProxy.getCustomProperties() == null || this.actualAPIProxy.getCustomProperties().size() == 0)
            return null;
        Iterator<String> it = this.actualAPIProxy.getCustomProperties().values().iterator();
        boolean propertyFound = false;
        while (it.hasNext()) {
            String propValue = it.next();
            if (propValue != null) {
                propertyFound = true;
                break;
            }
        }
        if (!propertyFound) return null; // If no property is declared for this API return null
        return this.actualAPIProxy.getCustomProperties();
    }

    public String getDescriptionType() {
        if (this.actualAPIProxy.getDescriptionType().equals("original")) return null;
        return this.actualAPIProxy.getDescriptionType();
    }


    public String getDescriptionManual() {
        return this.actualAPIProxy.getDescriptionManual();
    }


    public String getDescriptionMarkdown() {
        return this.actualAPIProxy.getDescriptionMarkdown();
    }


    public String getDescriptionUrl() {
        return this.actualAPIProxy.getDescriptionUrl();
    }


    public List<CaCert> getCaCerts() {
        if (this.actualAPIProxy.getCaCerts() == null) return null;
        if (this.actualAPIProxy.getCaCerts().isEmpty()) return null;
        return this.actualAPIProxy.getCaCerts();
    }


    public APIQuota getApplicationQuota() throws AppException {
        return translateMethodIds(this.actualAPIProxy.getApplicationQuota());
    }


    public APIQuota getSystemQuota() throws AppException {
        return translateMethodIds(this.actualAPIProxy.getSystemQuota());
    }

    private APIQuota translateMethodIds(APIQuota apiQuota) throws AppException {
        if (apiQuota == null || apiQuota.getRestrictions() == null) return apiQuota;
        for (QuotaRestriction restriction : apiQuota.getRestrictions()) {
            if ("*".equals(restriction.getMethod())) continue;
            restriction.setMethod(APIManagerAdapter.getInstance().methodAdapter.getMethodForId(this.actualAPIProxy.getId(), restriction.getMethod()).getName());
        }
        return apiQuota;
    }

    @JsonIgnore
    public Map<String, ServiceProfile> getServiceProfiles() {
        return this.actualAPIProxy.getServiceProfiles();
    }

    public List<String> getClientOrganizations() throws AppException {
        if (!APIManagerAdapter.hasAdminAccount()) return null;
        if (this.actualAPIProxy.getClientOrganizations().isEmpty()) return null;
        if (this.actualAPIProxy.getClientOrganizations().size() == 1 &&
                this.actualAPIProxy.getClientOrganizations().get(0).getName().equals(getOrganization()))
            return null;
        List<String> orgs = new ArrayList<>();
        for (Organization org : this.actualAPIProxy.getClientOrganizations()) {
            orgs.add(org.getName());
        }
        return orgs;
    }

    public List<ClientApplication> getApplications() {
        if (this.actualAPIProxy.getApplications().isEmpty()) return null;
        List<ClientApplication> exportApps = new ArrayList<>();
        for (ClientApplication app : this.actualAPIProxy.getApplications()) {
            ClientApplication exportApp = new ClientApplication();
            exportApp.setEnabled(app.isEnabled());
            exportApp.setName(app.getName());
            exportApp.setOrganization(null);
            exportApp.setCredentials(null);
            exportApp.setApiAccess(null);
            exportApps.add(exportApp);
        }
        return exportApps;
    }

    @JsonProperty("apiSpecification")
    public DesiredAPISpecification getApiDefinitionImport() {
        DesiredAPISpecification spec = new DesiredAPISpecification();
        if (this.getAPIDefinition() instanceof WSDLSpecification && EnvironmentProperties.RETAIN_BACKED_URL) {
            spec.setResource(actualAPIProxy.getBackendImportedUrl());
        } else
            spec.setResource(this.getAPIDefinition().getApiSpecificationFile());
        return spec;
    }


    public String getBackendBasepath() {

        // The API Manager composes the actual backend path from the host + path and backend resource path
        // specified in the frontend.
        // So if the backend was imported with the resourcepath /v2 and the backend is configured with
        // https://my.backend.host.com/another/path, the following backend results: https://my.backend.host.com/another/path/v2.
        // So, in order for the exported backendBasepath to exactly match the configured backend, it must be
        // composed of both properties.
        // See issue: https://github.com/Axway-API-Management-Plus/apim-cli/issues/158
        // https://github.com/Axway-API-Management-Plus/apim-cli/blob/develop/misc/images/behavior-useFEAPIDefinition.png

        //ISSUE-299
        // Resource path is part of API specification (like open api servers.url or swagger basePath) and we don't need to manage it in config file.
        return this.getServiceProfiles().get("_default").getBasePath();
    }

    public List<APIMethod> getApiMethods() {
        List<APIMethod> apiMethods = this.actualAPIProxy.getApiMethods();
        if (apiMethods == null || apiMethods.isEmpty()) return null;
        List<APIMethod> apiMethodsTransformed = new ArrayList<>();
        for (APIMethod actualMethod : apiMethods) {
            APIMethod apiMethod = new APIMethod();
            apiMethod.setName(actualMethod.getName());
            apiMethod.setSummary(actualMethod.getSummary());
            TagMap tagMap = actualMethod.getTags();
            if (tagMap != null && tagMap.size() > 0)
                apiMethod.setTags(actualMethod.getTags());
            apiMethodsTransformed.add(apiMethod);
            String descriptionType = actualMethod.getDescriptionType();
            if (descriptionType.equals("manual")) {
                apiMethod.setDescriptionManual(actualMethod.getDescriptionManual());
            } else if (descriptionType.equals("url")) {
                apiMethod.setDescriptionUrl(actualMethod.getDescriptionUrl());
            } else if (descriptionType.equals("markdown")) {
                apiMethod.setDescriptionMarkdown(actualMethod.getDescriptionMarkdown());
            }
            apiMethod.setDescriptionType(descriptionType);
        }
        return apiMethodsTransformed;
    }
}
