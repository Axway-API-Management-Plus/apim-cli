package com.axway.apim.swagger.api.properties.outboundprofiles;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.swagger.APIManagerAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OutboundProfile {
	
	protected static Logger LOG = LoggerFactory.getLogger(OutboundProfile.class);
	
	private static Map<String, String> apimRoutingPolicies;
	private static Map<String, String> apimRequestPolicies;
	private static Map<String, String> apimResponsePolicies;
	private static Map<String, String> apimFaultHandlerPolicies;
	
	String routeType;
	
	String requestPolicy;
	
	String responsePolicy;
	
	String routePolicy;
	
	String faultHandlerPolicy;
	
	String apiMethodId;
	
	String apiId;
	
	String authenticationProfile;
	
	String[] parameters = new String[] {};

	public OutboundProfile() throws AppException {
		super();
		if(OutboundProfile.apimRoutingPolicies == null) {
			OutboundProfile.apimRoutingPolicies = initPolicyies("routing");
			OutboundProfile.apimRequestPolicies = initPolicyies("request");
			OutboundProfile.apimResponsePolicies = initPolicyies("response");
			if(!APIManagerAdapter.getApiManagerVersion().startsWith("7.5")) {
				OutboundProfile.apimFaultHandlerPolicies = initPolicyies("faulthandler");
			}
		}
	}
	
	private static Map<String, String> initPolicyies(String type) throws AppException { 
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> policies = new HashMap<String, String>();
		CommandParameters cmd = CommandParameters.getInstance();
		HttpResponse httpResponse = null;
		try {
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/policies")
					.setParameter("type", type).build();
			RestAPICall getRequest = new GETRequest(uri, null);
			httpResponse = getRequest.execute();
			
			JsonNode jsonResponse;
			String response = EntityUtils.toString(httpResponse.getEntity());
			try {
				jsonResponse = mapper.readTree(response);
				for(JsonNode node : jsonResponse) {
					policies.put(node.get("name").asText(), node.get("id").asText());
				}
			} catch (Exception e) {
				LOG.error("Error reading configured custom-policies. Can't parse response: " + response);
				throw new AppException("Can't initialize policies for type: " + type, ErrorCode.API_MANAGER_COMMUNICATION, e);
			}
		} catch (Exception e) {
			throw new AppException("Can't initialize policies for type: " + type, ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
		return policies;
	}
	
	public String getPolicy(Map<String, String> policies, String policyName, String type) throws AppException {
		if(policyName == null) return policyName; // Do nothing if no policy is configured
		String policy = policies.get(policyName);
		if(policy == null) {
			LOG.error("Available "+type+" policies: " + policies.keySet());
			ErrorState.getInstance().setError("The policy: '" + policyName + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY, false);
			throw new AppException("The policy: '" + policyName + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
		}
		return policy;
	}

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

	public void setRequestPolicy(String requestPolicy) throws AppException {
		if(requestPolicy!=null && !requestPolicy.startsWith("<")) 
			requestPolicy = getPolicy(apimRequestPolicies, requestPolicy, "request");
		this.requestPolicy = requestPolicy;
	}

	public String getResponsePolicy() {
		return responsePolicy;
	}

	public void setResponsePolicy(String responsePolicy) throws AppException {
		if(responsePolicy!=null && !responsePolicy.startsWith("<")) 
			responsePolicy = getPolicy(apimResponsePolicies, responsePolicy, "response");
		this.responsePolicy = responsePolicy;
	}

	public String getRoutePolicy() {
		return routePolicy;
	}

	public void setRoutePolicy(String routePolicy) throws AppException {
		if(routePolicy!=null && !routePolicy.startsWith("<")) 
			routePolicy = getPolicy(apimRoutingPolicies, routePolicy, "routing");
		this.routePolicy = routePolicy;
	}

	public String getFaultHandlerPolicy() {
		return faultHandlerPolicy;
	}

	public void setFaultHandlerPolicy(String faultHandlerPolicy) throws AppException {
		if(faultHandlerPolicy!=null && !faultHandlerPolicy.startsWith("<")) 
			faultHandlerPolicy = getPolicy(apimFaultHandlerPolicies, faultHandlerPolicy, "fault handler");
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
				//StringUtils.equals(otherOutboundProfile.getRoutePolicy(), this.getRoutePolicy()) &&
				StringUtils.equals(otherOutboundProfile.getRouteType(), this.getRouteType()) &&
				Arrays.equals(otherOutboundProfile.getParameters(), this.getParameters());
		} else {
			return false;
		}
	}
}
