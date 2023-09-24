package com.axway.apim.organization.lib;

import java.util.List;
import java.util.Map;

import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;

public class ExportOrganization {

    Organization org;

    public ExportOrganization(Organization org) {
        this.org = org;
    }

    public String getName() {
        return this.org.getName();
    }

    public String getDescription() {
        return this.org.getDescription();
    }

    public boolean isRestricted() {
        return this.org.isRestricted();
    }

    public boolean isEnabled() {
        return this.org.isEnabled();
    }

    public boolean isDevelopment() {
        return this.org.isDevelopment();
    }

    public String getEmail() {
        return this.org.getEmail();
    }

    public Image getImage() {
        return this.org.getImage();
    }

    public Map<String, String> getCustomProperties() {
        return this.org.getCustomProperties();
    }

    @JsonProperty("apis")
    public List<APIAccess> getAPIAccess() {
        if (org.getApiAccess() == null || org.getApiAccess().isEmpty()) return Collections.emptyList();
        return org.getApiAccess();
    }
}
