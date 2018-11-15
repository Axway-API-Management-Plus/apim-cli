package com.axway.apim.swagger.api.properties;

public class OutboundProfile {
	String routeType;
	
	String requestPolicy;
	
	String responsePolicy;
	
	String routePolicy;
	
	String faultHandlerPolicy;
	
	String apiMethodId;
	
	String authenticationProfile;

	public String getAuthenticationProfile() {
		return "_default"; // For now, nothing else is supported!
	}

	public String getRouteType() {
		if(this.routePolicy!=null && !this.routePolicy.equals("null")) {
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

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof OutboundProfile) {
			OutboundProfile otherOutboundProfile = (OutboundProfile)other;
			if(!otherOutboundProfile.getFaultHandlerPolicy().equals(this.getFaultHandlerPolicy())) return false;
			if(!otherOutboundProfile.getRequestPolicy().equals(this.getRequestPolicy())) return false;
			if(!otherOutboundProfile.getResponsePolicy().equals(this.getResponsePolicy())) return false;
			if(!otherOutboundProfile.getRoutePolicy().equals(this.getRoutePolicy())) return false;
		} else {
			return false;
		}
		return true;
	}
	
	
}
