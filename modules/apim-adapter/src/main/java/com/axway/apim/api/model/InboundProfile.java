package com.axway.apim.api.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(value = {"apiMethodId", "apiId", "apiMethodName"})
public class InboundProfile extends Profile {
	
	boolean monitorAPI = true;
	
	boolean queryStringPassThrough = false;
	
	String monitorSubject = "authentication.subject.id";
	
	String securityProfile;
	
	String corsProfile;

	public boolean getMonitorAPI() {
		return monitorAPI;
	}

	public void setMonitorAPI(boolean monitorAPI) {
		this.monitorAPI = monitorAPI;
	}

	public String getMonitorSubject() {
		return monitorSubject;
	}

	public void setMonitorSubject(String monitorSubject) {
		this.monitorSubject = monitorSubject;
	}

	public String getSecurityProfile() {
		return securityProfile;
	}

	public void setSecurityProfile(String securityProfile) {
		this.securityProfile = securityProfile;
	}

	public String getCorsProfile() {
		return corsProfile;
	}

	public void setCorsProfile(String corsProfile) {
		this.corsProfile = corsProfile;
	}

	public boolean isQueryStringPassThrough() {
		return queryStringPassThrough;
	}

	public void setQueryStringPassThrough(boolean queryStringPassThrough) {
		this.queryStringPassThrough = queryStringPassThrough;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof InboundProfile) {
			InboundProfile otherInboundProfile = (InboundProfile)other;
			return
					otherInboundProfile.getMonitorAPI()==this.getMonitorAPI() &&
					otherInboundProfile.isQueryStringPassThrough()==this.isQueryStringPassThrough() &&
					StringUtils.equals(otherInboundProfile.getMonitorSubject(), this.getMonitorSubject()) &&
					StringUtils.equals(otherInboundProfile.getSecurityProfile(), this.getSecurityProfile()) &&
					StringUtils.equals(otherInboundProfile.getCorsProfile(), this.getCorsProfile());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(monitorAPI, queryStringPassThrough, monitorSubject, securityProfile, corsProfile);
	}

	@Override
	public String toString() {
		return "monitorAPI: " + monitorAPI + ", monitorSubject: " + monitorSubject + ", securityProfile: " + securityProfile + ", corsProfile: " + corsProfile;
	}
	
	
	public static InboundProfile getDefaultInboundProfile() {
		InboundProfile defaultInboundProfile = new InboundProfile();
		defaultInboundProfile.setMonitorAPI(true);
		defaultInboundProfile.setMonitorSubject("authentication.subject.id");
		defaultInboundProfile.setSecurityProfile("_default");
		defaultInboundProfile.setCorsProfile("_default");
		return defaultInboundProfile;
	}
}
