package com.axway.apim.api.model;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"apiMethodId", "apiId", "apiMethodName"})
public class InboundProfile extends Profile {
	
	boolean monitorAPI = true;
	
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

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof InboundProfile) {
			InboundProfile otherInboundProfile = (InboundProfile)other;
			return
					otherInboundProfile.getMonitorAPI()==this.getMonitorAPI() &&
					StringUtils.equals(otherInboundProfile.getMonitorSubject(), this.getMonitorSubject()) &&
					StringUtils.equals(otherInboundProfile.getSecurityProfile(), this.getSecurityProfile()) &&
					StringUtils.equals(otherInboundProfile.getCorsProfile(), this.getCorsProfile());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "monitorAPI: " + monitorAPI + ", monitorSubject: " + monitorSubject + ", securityProfile: " + securityProfile + ", corsProfile: " + corsProfile;
	}
}
