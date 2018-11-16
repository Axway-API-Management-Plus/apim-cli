package com.axway.apim.swagger.api.properties.corsprofiles;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.swagger.api.properties.inboundprofiles.InboundProfile;

public class CorsProfile {
	
	String name;
	
	String isDefault;
	
	String[] origins;
	
	String[] allowedHeaders;
	
	String[] exposedHeaders;
	
	String supportCredentials;
	
	String maxAgeSeconds;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	public String[] getOrigins() {
		return origins;
	}

	public void setOrigins(String[] origins) {
		this.origins = origins;
	}

	public String[] getAllowedHeaders() {
		return allowedHeaders;
	}

	public void setAllowedHeaders(String[] allowedHeaders) {
		this.allowedHeaders = allowedHeaders;
	}

	public String[] getExposedHeaders() {
		return exposedHeaders;
	}

	public void setExposedHeaders(String[] exposedHeaders) {
		this.exposedHeaders = exposedHeaders;
	}

	public String getSupportCredentials() {
		return supportCredentials;
	}

	public void setSupportCredentials(String supportCredentials) {
		this.supportCredentials = supportCredentials;
	}

	public String getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	public void setMaxAgeSeconds(String maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof CorsProfile) {
			CorsProfile otherCorsProfile = (CorsProfile)other;
			return
					StringUtils.equals(otherCorsProfile.getName(), this.getName()) &&
					StringUtils.equals(otherCorsProfile.getIsDefault(), this.getIsDefault()) &&
					Arrays.equals(otherCorsProfile.getOrigins(), this.getOrigins()) &&
					Arrays.equals(otherCorsProfile.getAllowedHeaders(), this.getAllowedHeaders()) &&
					Arrays.equals(otherCorsProfile.getExposedHeaders(), this.getExposedHeaders()) &&
					StringUtils.equals(otherCorsProfile.getSupportCredentials(), this.getSupportCredentials()) &&
					StringUtils.equals(otherCorsProfile.getMaxAgeSeconds(), this.getMaxAgeSeconds());
		} else {
			return false;
		}
	}
}
