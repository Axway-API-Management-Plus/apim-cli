package com.axway.apim.api.model;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class CorsProfile {
	
	String name;
	
	boolean isDefault;
	
	String[] origins;
	
	String[] allowedHeaders;
	
	String[] exposedHeaders;
	
	boolean supportCredentials;
	
	String maxAgeSeconds;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(boolean isDefault) {
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

	public boolean getSupportCredentials() {
		return supportCredentials;
	}

	public void setSupportCredentials(boolean supportCredentials) {
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
					otherCorsProfile.getIsDefault() == this.getIsDefault() &&
					Arrays.equals(otherCorsProfile.getOrigins(), this.getOrigins()) &&
					Arrays.equals(otherCorsProfile.getAllowedHeaders(), this.getAllowedHeaders()) &&
					Arrays.equals(otherCorsProfile.getExposedHeaders(), this.getExposedHeaders()) &&
					otherCorsProfile.getSupportCredentials() == this.getSupportCredentials() &&
					StringUtils.equals(otherCorsProfile.getMaxAgeSeconds(), this.getMaxAgeSeconds());
		} else {
			return false;
		}
	}
	
	public static CorsProfile getDefaultCorsProfile() {
		CorsProfile defaultCorsProfile = new CorsProfile();
		defaultCorsProfile.setName("_default");
		defaultCorsProfile.setIsDefault(true);
		defaultCorsProfile.setOrigins(new String[] {"*"});
		defaultCorsProfile.setAllowedHeaders(new String[]{});
		defaultCorsProfile.setExposedHeaders(new String[]{"X-CorrelationID"});
		defaultCorsProfile.setSupportCredentials(false);
		defaultCorsProfile.setMaxAgeSeconds("0");
		return defaultCorsProfile;
	}
}
