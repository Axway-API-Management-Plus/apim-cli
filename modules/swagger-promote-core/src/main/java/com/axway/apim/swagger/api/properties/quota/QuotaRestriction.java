package com.axway.apim.swagger.api.properties.quota;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class QuotaRestriction {
	String api;
	String method;
	String type;
	
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
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
				StringUtils.equals(otherRestriction.getApi(), this.getApi());
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof QuotaRestriction) {
			QuotaRestriction quotaRestriction = (QuotaRestriction)other;
			
			return
					StringUtils.equals(quotaRestriction.getMethod(), this.getMethod()) &&
					StringUtils.equals(quotaRestriction.getType(), this.getType()) &&
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
