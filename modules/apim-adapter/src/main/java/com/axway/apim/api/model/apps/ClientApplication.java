package com.axway.apim.api.model.apps;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.axway.apim.adapter.jackson.APIAccessSerializer;
import com.axway.apim.adapter.jackson.OrganizationDeserializer;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.AbstractEntity;
import com.axway.apim.api.model.CustomPropertiesEntity;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author ADMIN
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("ApplicationFilter")
public class ClientApplication extends AbstractEntity implements CustomPropertiesEntity {

    public enum ApplicationState {
        approved,
        pending
    }


    private String email;
    private String phone;
    private boolean enabled;

    private ApplicationState state;

    @JsonProperty("image")
    private String imageUrl;

    private String createdBy;

    private Long createdOn;

    @JsonIgnore
    private Image image;

    private String oauthClientId;
    private String extClientId;
    private String apiKey;

    @JsonSerialize(using = APIAccessSerializer.class)
    @JsonProperty("apis")
    private List<APIAccess> apiAccess = new ArrayList<>();

    @JsonProperty("permissions")
    private List<ApplicationPermission> permissions = new ArrayList<>();

    private List<ClientAppCredential> credentials = new ArrayList<>();

    private APIQuota appQuota;

    @JsonProperty("appScopes")
    private List<ClientAppOauthResource> oauthResources = new ArrayList<>();

    @JsonDeserialize(using = OrganizationDeserializer.class)
    @JsonAlias({"organization", "organizationId"})
    private Organization organization;

    private Map<String, String> customProperties = null;
    @JsonIgnore
    private List<String> customPropertiesKeys = null;

    public String getOrganizationId() {
        if (this.organization == null) return null;
        return this.organization.getId();
    }

    public String getEmail() {
        if (StringUtils.isEmpty(email)) return null;
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        if (StringUtils.isEmpty(phone)) return null;
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ApplicationState getState() {
        if (this.state == null) return ApplicationState.approved;
        return state;
    }

    public void setState(ApplicationState state) {
        this.state = state;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    public String getExtClientId() {
        return extClientId;
    }

    public void setExtClientId(String extClientId) {
        this.extClientId = extClientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.credentials.add(new APIKey());
        this.apiKey = apiKey;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public APIQuota getAppQuota() {
        return appQuota;
    }

    public void setAppQuota(APIQuota appQuota) {
        this.appQuota = appQuota;
    }

    public List<APIAccess> getApiAccess() {
        return apiAccess;
    }

    public void setApiAccess(List<APIAccess> apiAccess) {
        this.apiAccess = apiAccess;
    }

    public List<ClientAppCredential> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<ClientAppCredential> credentials) {
        this.credentials = credentials;
    }

    public List<ClientAppOauthResource> getOauthResources() {
        return oauthResources;
    }

    public void setOauthResources(List<ClientAppOauthResource> oauthResources) {
        this.oauthResources = oauthResources;
    }

    public List<ApplicationPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<ApplicationPermission> permissions) {
        this.permissions = permissions;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * This avoids, that custom properties are wrapped within customProperties { ... }
     * See http://www.cowtowncoder.com/blog/archives/2011/07/entry_458.html
     *
     * @return custom properties map
     */
    @JsonAnyGetter
    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    @JsonAnySetter
    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
        if (this.customProperties != null) {
            this.customPropertiesKeys = new ArrayList<>(customProperties.keySet());
        }
    }

    public List<String> getCustomPropertiesKeys() {
        return customPropertiesKeys;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof ClientApplication) {
            ClientApplication otherApp = (ClientApplication) other;
            Comparator<ClientAppCredential> comparator = Comparator.comparing(ClientAppCredential::getCredentialType).thenComparing(ClientAppCredential::getId);
            return
                StringUtils.equals(otherApp.getName(), this.getName()) &&
                    StringUtils.equals(otherApp.getEmail(), this.getEmail()) &&
                    StringUtils.equals(otherApp.getDescription(), this.getDescription()) &&
                    StringUtils.equals(otherApp.getPhone(), this.getPhone()) &&
                    otherApp.getState().equals(this.getState()) &&
                    otherApp.getOrganization().equals(this.getOrganization()) &&
                    (otherApp.getCredentials() == null || otherApp.getCredentials().stream().sorted(comparator).collect(Collectors.toList()).equals(this.getCredentials().stream().sorted(comparator).collect(Collectors.toList()))) &&
                    (otherApp.getOauthResources() == null || otherApp.getOauthResources().stream().sorted(Comparator.comparing(ClientAppOauthResource::getScope)).collect(Collectors.toList()).equals(this.getOauthResources().stream().sorted(Comparator.comparing(ClientAppOauthResource::getScope)).collect(Collectors.toList()))) &&
                    (otherApp.getImage() == null || otherApp.getImage().equals(this.getImage())) &&
                    (otherApp.getCustomProperties() == null || otherApp.getCustomProperties().equals(this.getCustomProperties()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, phone, state, image, organization);
    }

    @Override
    public String toString() {
        return "ClientApplication{" +
            "name=" + getName() +
            ", id=" + getId() +
            ", enabled=" + enabled +
            ", state=" + state +
            ", organization=" + organization +
            '}';
    }
}
