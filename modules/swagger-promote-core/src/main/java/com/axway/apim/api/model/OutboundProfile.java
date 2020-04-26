package com.axway.apim.api.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
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
	
	List<Object> parameters = new ArrayList<Object>();

	public OutboundProfile() throws AppException {
		super();
		if(OutboundProfile.apimRoutingPolicies == null) {
			OutboundProfile.apimRoutingPolicies = initPolicyies("routing");
			OutboundProfile.apimRequestPolicies = initPolicyies("request");
			OutboundProfile.apimResponsePolicies = initPolicyies("response");
			if(APIManagerAdapter.hasAPIManagerVersion("7.6.2")) {
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
		setRequestPolicy(requestPolicy, true);
	}

	public void setRequestPolicy(String requestPolicy, boolean parseInternal) throws AppException {
		if(requestPolicy==null) return;
		if(!parseInternal) {
			this.requestPolicy = requestPolicy;
		} else {
			if(requestPolicy.startsWith("<")) {
				this.requestPolicy = requestPolicy;
				return;
			}
			if(requestPolicy.equals("")) 
				return;
			this.requestPolicy = getPolicy(apimRequestPolicies, requestPolicy, "request");
		}
	}

	public String getResponsePolicy() {
		return responsePolicy;
	}

	public void setResponsePolicy(String responsePolicy) throws AppException {
		setResponsePolicy(responsePolicy, true);
	}

	public void setResponsePolicy(String responsePolicy, boolean parseInternal) throws AppException {
		if(responsePolicy==null) return;
		if(!parseInternal) {
			this.responsePolicy = responsePolicy;
		} else {
			if(responsePolicy.startsWith("<")) {
				this.responsePolicy = responsePolicy;
				return;
			}
			if(responsePolicy.equals("")) 
				return;
			this.responsePolicy = getPolicy(apimResponsePolicies, responsePolicy, "response");
		}
	}

	public String getRoutePolicy() {
		return routePolicy;
	}

	public void setRoutePolicy(String routePolicy) throws AppException {
		setRoutePolicy(routePolicy, true);
	}

	public void setRoutePolicy(String routePolicy, boolean parseInternal) throws AppException {
		if(routePolicy==null) return;
		if(!parseInternal) {
			this.routePolicy = routePolicy;
		} else {
			if(routePolicy!=null && routePolicy.startsWith("<")) {
				this.routePolicy = routePolicy;
				return;
			}
			if(routePolicy!=null && routePolicy.equals("")) return;
			this.routePolicy = getPolicy(apimRoutingPolicies, routePolicy, "routing");
			
		}
	}

	public String getFaultHandlerPolicy() {
		return faultHandlerPolicy;
	}

	public void setFaultHandlerPolicy(String faultHandlerPolicy) throws AppException {
		setFaultHandlerPolicy(faultHandlerPolicy, true);
	}

	public void setFaultHandlerPolicy(String faultHandlerPolicy, boolean parseInternal) throws AppException {
		if(faultHandlerPolicy==null) return;
		if(!parseInternal) {
			this.faultHandlerPolicy = faultHandlerPolicy;
		} else {
			if(faultHandlerPolicy!=null && faultHandlerPolicy.startsWith("<")) {
				this.faultHandlerPolicy = faultHandlerPolicy;
				return;
			}
			if(faultHandlerPolicy!=null && faultHandlerPolicy.equals("")) return;
			this.faultHandlerPolicy = getPolicy(apimFaultHandlerPolicies, faultHandlerPolicy, "fault handler");
		}
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
