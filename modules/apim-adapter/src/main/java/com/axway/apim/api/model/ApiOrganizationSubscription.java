package com.axway.apim.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiOrganizationSubscription {

    private String organizationName;
    private String organizationId;

    @JsonProperty("applications")
    private List<ApiApplicationSubscription> apiApplicationSubscriptions;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public List<ApiApplicationSubscription> getApiApplicationSubscriptions() {
        return apiApplicationSubscriptions;
    }

    public void setApiApplicationSubscriptions(List<ApiApplicationSubscription> apiApplicationSubscriptions) {
        this.apiApplicationSubscriptions = apiApplicationSubscriptions;
    }

    @Override
    public String toString() {
        return "ApiOrganizationSubscription{" +
            "organizationName='" + organizationName + '\'' +
            ", organizationId='" + organizationId + '\'' +
            ", apiApplicationSubscriptions=" + apiApplicationSubscriptions +
            '}';
    }
}
