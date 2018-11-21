package com.axway.apim.swagger.api.properties.outboundprofiles;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

public class OutboundProfile {
	String routeType;
	
	String requestPolicy;
	
	String responsePolicy;
	
	String routePolicy;
	
	String faultHandlerPolicy;
	
	String apiMethodId;
	
	String apiId;
	
	String authenticationProfile;
	
	String[] parameters = new String[] {};

	public String getAuthenticationProfile() {
		return "_default"; // For now, nothing else is supported!
	}

	public void setAuthenticationProfile(String authenticationProfile) {
		this.authenticationProfile = authenticationProfile;
	}

	public String getRouteType() {
		if(this.routePolicy!=null && !this.routePolicy.equals("")) {
			return "policy";
		} else {
			return "proxy";
		}
	}

	public void setRouteType(String routeType) {
		this.routeType = routeType;
	}

	public String getRequestPolicy() {
		return requestPolicy;
	}

	public void setRequestPolicy(String requestPolicy) {
		this.requestPolicy = requestPolicy;
	}

	public String getResponsePolicy() {
		return responsePolicy;
	}

	public void setResponsePolicy(String responsePolicy) {
		this.responsePolicy = responsePolicy;
	}

	public String getRoutePolicy() {
		return routePolicy;
	}

	public void setRoutePolicy(String routePolicy) {
		this.routePolicy = routePolicy;
	}

	public String getFaultHandlerPolicy() {
		return faultHandlerPolicy;
	}

	public void setFaultHandlerPolicy(String faultHandlerPolicy) {
		this.faultHandlerPolicy = faultHandlerPolicy;
	}

	public String getApiMethodId() {
		return apiMethodId;
	}

	public void setApiMethodId(String apiMethodId) {
		this.apiMethodId = apiMethodId;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof OutboundProfile) {
			
			OutboundProfile otherOutboundProfile = (OutboundProfile)other;

			return
				StringUtils.equals(otherOutboundProfile.getFaultHandlerPolicy(), this.getFaultHandlerPolicy()) &&
				StringUtils.equals(otherOutboundProfile.getRequestPolicy(), this.getRequestPolicy()) &&
				StringUtils.equals(otherOutboundProfile.getResponsePolicy(), this.getResponsePolicy()) &&
				StringUtils.equals(otherOutboundProfile.getRoutePolicy(), this.getRoutePolicy()) &&
				StringUtils.equals(otherOutboundProfile.getRouteType(), this.getRouteType()) &&
				Arrays.equals(otherOutboundProfile.getParameters(), this.getParameters());
		} else {
			return false;
		}
	}
}
