package com.axway.apim.api.model;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.axway.apim.api.API;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFilter("QuotaRestrictionFilter")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotaRestriction {

    /**
     * The constraint ID assigned by the API-Manager. It is readOnly in the API-Manager REST-API: it is ignored on
     * POST /quotas/{id}/constraints and taken from the URL on PUT, hence it must never be serialized. It must also
     * not leak into exported API-/Application-Config files.
     * Access.WRITE_ONLY removes the property from the BeanSerializer itself, which covers all serialization paths
     * at once - including the ones that do NOT go through the QuotaRestrictionFilter (ExportHelper and
     * JsonAPIManagerSetupExporter serialize this class with the default bean serializer).
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String id;

    /**
     * Contains the ID of the API this restriction is applied to or a "*" if applied to all APIs.
     * The JsonAlias is required for Deserialization as the API-Manager returns the information in field api
     */
    @JsonAlias({"api"})
    String apiId;

    /**
     * The API-Method either is the API-Method-ID or a "*" if applied to all API-Methods.
     */
    String method;

    /**
     * The underlying API, if any, this restriction is applied too
     */
    @JsonIgnore
    API restrictedAPI;

    QuotaRestrictionType type;

    Map<String, String> config;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    /**
     * This getter is used when the Quota-Restriction is send to the API-Manager REST-API, which expects a field api
     *
     * @return the ID of the API or a "*" if applied for all APIs.
     */
    public String getApi() {
        return apiId;
    }

    public API getRestrictedAPI() {
        return restrictedAPI;
    }

    public void setRestrictedAPI(API restrictedAPI) {
        this.restrictedAPI = restrictedAPI;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public QuotaRestrictionType getType() {
        return type;
    }

    public void setType(QuotaRestrictionType type) {
        this.type = type;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }


    /**
     * Note: id and apiId are deliberately NOT part of equals/hashCode.
     * This equals is the desired-vs-actual change detector reached from APIQuota.equals via Utils.compareValues
     * and finally APIChangeState. Desired restrictions are read from a config file and therefore never carry the
     * API-Manager assigned id, so including it here would report every API as changed on every single run.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof QuotaRestriction) {
            QuotaRestriction quotaRestriction = (QuotaRestriction) other;
            return
                StringUtils.equals(quotaRestriction.getMethod(), this.getMethod()) &&
                    quotaRestriction.getType() == this.getType() &&
                    Objects.equals(quotaRestriction.getConfig(), this.getConfig());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, type, config);
    }

    @Override
    public String toString() {
        return "QuotaRestriction [id=" + id + ", api=" + apiId + ", method=" + method + ", type=" + type + ", config=" + config + "]";
    }
}
