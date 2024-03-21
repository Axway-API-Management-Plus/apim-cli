package com.axway.apim.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiApplicationSubscription {

    private String applicationName;
    private String applicationId;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public String toString() {
        return "ApiApplicationSubscription{" +
            "applicationName='" + applicationName + '\'' +
            ", applicationId='" + applicationId + '\'' +
            '}';
    }
}
