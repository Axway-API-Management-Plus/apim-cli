package com.axway.apim.swagger.api.properties.outboundprofiles;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.tasks.props.PropertyHandler;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ImportOutboundProfiles extends OutboundProfiles implements PropertyHandler {
	
	
	
	private Map<String, String> apimRoutingPolicies = initPolicyies("routing");
	private Map<String, String> apimRequestPolicies = initPolicyies("request");
	private Map<String, String> apimResponsePolicies = initPolicyies("response");
	private Map<String, String> apimFaultHandlerPolicies = initPolicyies("faulthandler");

	public ImportOutboundProfiles(JsonNode config) throws AppException {
		if(config instanceof MissingNode) { // Default handling!
			 this.outboundProfiles = new LinkedHashMap<String, OutboundProfile>();
			return;
		}
		try {
			this.outboundProfiles = objectMapper.readValue( config.toString(), new TypeReference<Map<String,OutboundProfile>>(){} );
		} catch (Exception e) {
			throw new AppException("Cant process outbound profiles", ErrorCode.UNXPECTED_ERROR, e);
		}
		Iterator<String> it = outboundProfiles.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			OutboundProfile profile = this.outboundProfiles.get(name);
			profile.setRequestPolicy(getPolicy(this.apimRequestPolicies, profile.getRequestPolicy(), "request"));
			profile.setResponsePolicy(getPolicy(this.apimResponsePolicies, profile.getResponsePolicy(), "response"));
			profile.setRoutePolicy(getPolicy(this.apimRoutingPolicies, profile.getRoutePolicy(), "routing"));
			profile.setFaultHandlerPolicy(getPolicy(this.apimFaultHandlerPolicies, profile.getFaultHandlerPolicy(), "fault handler"));
		}
	}

	@Override
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) throws AppException {
		if(this.outboundProfiles.size()!=0) {
			((ObjectNode)response).replace("outboundProfiles", objectMapper.valueToTree(this.outboundProfiles));
		}
		return response;
	}
	
	private Map<String, String> initPolicyies(String type) throws AppException { 
		Map<String, String> policies = new HashMap<String, String>();
		CommandParameters cmd = CommandParameters.getInstance();
		try {
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/policies")
					.setParameter("type", type).build();
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			
			JsonNode jsonResponse;
			try {
				jsonResponse = objectMapper.readTree(response);
				for(JsonNode node : jsonResponse) {
					policies.put(node.get("name").asText(), node.get("id").asText());
				}
			} catch (IOException e) {
				throw new AppException("Can't find Policy: ....", ErrorCode.API_MANAGER_COMMUNICATION, e);
			}
		} catch (Exception e) {
			throw new AppException("Can't find Policy: ....", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return policies;
	}
	
	private String getPolicy(Map<String, String> policies, String policyName, String type) throws AppException {
		if(policyName == null) return policyName; // Do nothing if no policy is configured
		String policy = policies.get(policyName);
		if(policy == null) {
			LOG.error("Available "+type+" policies: " + policies.keySet());
			throw new AppException("The policy: '" + policyName + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY, false);
		}
		return policy;
	}

}
