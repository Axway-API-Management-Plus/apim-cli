package com.axway.apim.api.model;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.axway.apim.api.API;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonFilter("QuotaRestrictionFilter")
public class QuotaRestriction {
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
	
	QuotaRestrictiontype type;
	
	Map<String, String> config;

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
	
	/**
	 * This getter is used when the Quota-Restriction is send to the API-Manager REST-API, which expects a field api
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

	public QuotaRestrictiontype getType() {
		return type;
	}

	public void setType(QuotaRestrictiontype type) {
		this.type = type;
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}
	
	public boolean isSameRestriction(QuotaRestriction otherRestriction, boolean ignoreAPI) {
		if(otherRestriction == null) return false;
		return 
				StringUtils.equals(otherRestriction.getMethod(), this.getMethod()) &&
				(ignoreAPI || StringUtils.equals(otherRestriction.getApiId(), this.getApiId() )) &&
				otherRestriction.getType()==this.getType() &&
				StringUtils.equals(otherRestriction.getConfig().get("period"), this.getConfig().get("period")) &&
				StringUtils.equals(otherRestriction.getConfig().get("per"), this.getConfig().get("per"));
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof QuotaRestriction) {
			QuotaRestriction quotaRestriction = (QuotaRestriction)other;
			return
					StringUtils.equals(quotaRestriction.getMethod(), this.getMethod()) &&
					quotaRestriction.getType()==this.getType() &&
					quotaRestriction.getConfig().equals(this.getConfig());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "QuotaRestriction [api=" + apiId + ", method=" + method + ", type=" + type + ", config=" + config + "]";
	}
}
