package com.axway.apim.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.errorHandling.AppException;

public class OutboundProfile {
	
	protected static Logger LOG = LoggerFactory.getLogger(OutboundProfile.class);
	
	String routeType;
	
	String requestPolicy;
	
	String responsePolicy;
	
	String routePolicy;
	
	String faultHandlerPolicy;
	
	String apiMethodId;
	
	String apiId;
	
	String authenticationProfile;
	
	List<Object> parameters = new ArrayList<Object>();

	public OutboundProfile() throws AppException {
		super();
	}

	public String getAuthenticationProfile() {
		return this.authenticationProfile;
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
	
	public void setRequestPolicy(String requestPolicy) throws AppException {
		this.requestPolicy = requestPolicy;
	}

	public String getResponsePolicy() {
		return responsePolicy;
	}

	public void setResponsePolicy(String responsePolicy) throws AppException {
		this.responsePolicy = responsePolicy;
	}

	public String getRoutePolicy() {
		return routePolicy;
	}

	public void setRoutePolicy(String routePolicy) throws AppException {
		this.routePolicy = routePolicy;
	}

	public String getFaultHandlerPolicy() {
		return faultHandlerPolicy;
	}

	public void setFaultHandlerPolicy(String faultHandlerPolicy) throws AppException {
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

	public List<Object> getParameters() {
		return parameters;
	}

	@SuppressWarnings("unchecked")
	public void setParameters(List<Object> parameters) {
		if(APIManagerAdapter.hasAPIManagerVersion("7.7.20200130")) {
			// We need to inject the format as default
			for(Object params : parameters) {
				if(params instanceof Map<?, ?>) {
					if(!((Map<?, ?>)params).containsKey("format")) {
						((Map<String, ?>) params).put("format", null);
					}
				}
			}
		}
		this.parameters = parameters;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof OutboundProfile) {
			OutboundProfile otherOutboundProfile = (OutboundProfile)other;
			List<Object> otherParameters = otherOutboundProfile.getParameters();
			List<Object> thisParameters = this.getParameters();
			if(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1") || APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP5")) {
				// Passwords no longer exposed by API-Manager REST-API - Can't use it anymore to compare the state
				otherParameters.remove("password");
				thisParameters.remove("password");
			}
			boolean rc = 
				StringUtils.equals(otherOutboundProfile.getFaultHandlerPolicy(), this.getFaultHandlerPolicy()) &&
				StringUtils.equals(otherOutboundProfile.getRequestPolicy(), this.getRequestPolicy()) &&
				StringUtils.equals(otherOutboundProfile.getResponsePolicy(), this.getResponsePolicy()) &&
				StringUtils.equals(otherOutboundProfile.getRoutePolicy(), this.getRoutePolicy()) &&
				StringUtils.equals(otherOutboundProfile.getRouteType(), this.getRouteType()) &&
				otherParameters.equals(thisParameters);
			return rc;
		} else {
			return false;
		}
	}
}
