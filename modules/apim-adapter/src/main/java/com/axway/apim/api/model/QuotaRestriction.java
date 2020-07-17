package com.axway.apim.api.model;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class QuotaRestriction {
	String api;
	String method;
	QuotaRestrictiontype type;
	
	Map<String, String> config;

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
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
	
	public boolean isSameRestriction(QuotaRestriction otherRestriction) {
		if(otherRestriction == null) return false;
		return 
				StringUtils.equals(otherRestriction.getMethod(), this.getMethod()) &&
				StringUtils.equals(otherRestriction.getApi(), this.getApi()) &&
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
		return "QuotaRestriction [api=" + api + ", method=" + method + ", type=" + type + ", config=" + config + "]";
	}
}
