package com.axway.apim.api.model;

import com.axway.apim.adapter.jackson.APIAccessSerializer;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("OrganizationFilter")
public class Organization extends AbstractEntity implements CustomPropertiesEntity {

    private String email;

    @JsonProperty("image")
    private String imageUrl;

    @JsonIgnore
    private Image image;

    private boolean restricted;

    private String virtualHost;

    private String phone;

    private boolean enabled;

    private boolean development;

    private String dn;

    private Long createdOn;

    private String startTrialDate;

    private String endTrialDate;

    private String trialDuration;

    private String isTrial;

    private Map<String, String> customProperties = null;

    @JsonSerialize(using = APIAccessSerializer.class)
    @JsonProperty("apis")
    private List<APIAccess> apiAccess = new ArrayList<>();

    public Organization() {
        super();
    }

    public Organization(String name) {
        super();
        setName(name);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getPhone() {
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

    public boolean isDevelopment() {
        return development;
    }

    public void setDevelopment(boolean development) {
        this.development = development;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public String getStartTrialDate() {
        return startTrialDate;
    }

    public void setStartTrialDate(String startTrialDate) {
        this.startTrialDate = startTrialDate;
    }

    public String getEndTrialDate() {
        return endTrialDate;
    }

    public void setEndTrialDate(String endTrialDate) {
        this.endTrialDate = endTrialDate;
    }

    public String getTrialDuration() {
        return trialDuration;
    }

    public void setTrialDuration(String trialDuration) {
        this.trialDuration = trialDuration;
    }

    public String getIsTrial() {
        return isTrial;
    }

    public void setIsTrial(String isTrial) {
        this.isTrial = isTrial;
    }

    public List<APIAccess> getApiAccess() {
        return apiAccess;
    }

    public void setApiAccess(List<APIAccess> apiAccess) {
        this.apiAccess = apiAccess;
    }

    /**
     * This avoids, that custom properties are wrapped within customProperties { ... }
     * // See http://www.cowtowncoder.com/blog/archives/2011/07/entry_458.html
     *
     * @return custom properties
     */
    @JsonAnyGetter
    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    @JsonAnySetter
    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof Organization) {
            return StringUtils.equals(((Organization) other).getName(), this.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dn);
    }

    public boolean deepEquals(Object other) {
        if (other == null) return false;
        if (other instanceof Organization) {
            Organization otherOrg = (Organization) other;
            return
                StringUtils.equals(otherOrg.getName(), this.getName()) &&
                    StringUtils.equals(otherOrg.getEmail(), this.getEmail()) &&
                    StringUtils.equals(otherOrg.getDescription(), this.getDescription()) &&
                    StringUtils.equals(otherOrg.getPhone(), this.getPhone()) &&
                    Utils.compareValues(otherOrg.getApiAccess(), this.getApiAccess()) &&
                    (otherOrg.getImage() == null || otherOrg.getImage().equals(this.getImage())) &&
                    (otherOrg.getCustomProperties() == null || otherOrg.getCustomProperties().equals(this.getCustomProperties()))
                ;
        }
        return false;
    }

    @Override
    public String toString() {
        return "'" + getName() + "'";
    }

    public static class Builder {
        String name;
        String id;

        public Organization build() {
            Organization org = new Organization();
            org.setName(name);
            org.setId(id);
            return org;
        }

        public Builder hasName(String name) {
            this.name = name;
            return this;
        }

        public Builder hasId(String id) {
            this.id = id;
            return this;
        }
    }
}
