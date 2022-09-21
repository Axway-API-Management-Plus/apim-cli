package com.axway.apim.api.model;

import java.util.Map;

import com.axway.apim.adapter.jackson.UserOrgName2IdConverter;
import org.apache.commons.lang3.StringUtils;

import com.axway.apim.adapter.jackson.OrganizationDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("UserFilter")
@JsonDeserialize(converter = UserOrgName2IdConverter.class)
public class User implements CustomPropertiesEntity {
    String id;
    @JsonDeserialize(using = OrganizationDeserializer.class)
    @JsonAlias({"organizationId", "organization"}) // Alias to read Organization based on the id as given by the API-Manager
    Organization organization;
    String name;
    String description;
    String loginName;
    String password;
    String email;
    String role;
    Boolean enabled;
    Long createdOn;
    String state;
    String type;
    String phone;
    String mobile;

    AuthenticatedUserAttributes authNUserAttributes;

    String dn;

    Map<String, String> customProperties = null;

    Map<String, String> orgs2Role;
    Map<String, String> orgs2Name;

    @JsonIgnore
    private Image image;

    @JsonIgnore
    Map<String, String> name2OrgId;

    @JsonProperty("image")
    private String imageUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getOrganizationId() {
        if (this.organization == null) return null;
        return this.organization.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public AuthenticatedUserAttributes getAuthNUserAttributes() {
        return authNUserAttributes;
    }

    public void setAuthNUserAttributes(AuthenticatedUserAttributes authNUserAttributes) {
        this.authNUserAttributes = authNUserAttributes;
    }

    // This avoids, that custom properties are wrapped within customProperties { ... }
    // See http://www.cowtowncoder.com/blog/archives/2011/07/entry_458.html
    @JsonAnyGetter
    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    // This avoids, that custom properties are wrapped within customProperties { ... }
    // See http://www.cowtowncoder.com/blog/archives/2011/07/entry_458.html
    @JsonAnySetter
    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public Map<String, String> getOrgs2Role() {
        return orgs2Role;
    }

    public void setOrgs2Role(Map<String, String> orgs2Role) {
        this.orgs2Role = orgs2Role;
    }

    public Map<String, String> getOrgs2Name() {
        return orgs2Name;
    }

    public void setOrgs2Name(Map<String, String> orgs2Name) {
        this.orgs2Name = orgs2Name;
    }

    public Map<String, String> getName2OrgId() {
        return name2OrgId;
    }

    public void setName2OrgId(Map<String, String> name2OrgId) {
        this.name2OrgId = name2OrgId;
    }

    public boolean deepEquals(Object other) {
        if (other == null) return false;
        if (other instanceof User) {
            User otherUser = (User) other;
            return
                    StringUtils.equals(otherUser.getName(), this.getName()) &&
                            StringUtils.equals(otherUser.getRole(), this.getRole()) &&
                            StringUtils.equals(otherUser.getLoginName(), this.getLoginName()) &&
                            StringUtils.equals(otherUser.getMobile(), this.getMobile()) &&
                            otherUser.getOrganization().equals(this.getOrganization()) &&
                            StringUtils.equals(otherUser.getPhone(), this.getPhone()) &&
                            StringUtils.equals(otherUser.getEmail().toLowerCase(), this.getEmail().toLowerCase()) &&
                            (otherUser.isEnabled() == this.isEnabled()) &&
                            StringUtils.equals(otherUser.getDescription(), this.getDescription()) &&
                            (this.getImage() == null || this.getImage().equals(otherUser.getImage())) &&
                            (this.getCustomProperties() == null || this.getCustomProperties().equals(otherUser.getCustomProperties()));
        }
        return false;
    }
}