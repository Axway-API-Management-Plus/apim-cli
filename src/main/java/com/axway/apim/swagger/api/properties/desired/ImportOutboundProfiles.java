package com.axway.apim.swagger.api.properties.desired;

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
import com.axway.apim.swagger.api.properties.OutboundProfile;
import com.axway.apim.swagger.api.properties.OutboundProfiles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ImportOutboundProfiles extends OutboundProfiles implements PropertyHandler {
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	private Map<String, String> apimRoutingPolicies = initPolicyies("routing");
	private Map<String, String> apimRequestPolicies = initPolicyies("request");
	private Map<String, String> apimResponsePolicies = initPolicyies("response");
	private Map<String, String> apimFaultHandlerPolicies = initPolicyies("faulthandler");

	public ImportOutboundProfiles(JsonNode config) throws AppException {
		OutboundProfile profile;
		this.outboundProfiles = new LinkedHashMap<String, Object>();
		Iterator it = config.fields();
		while(it.hasNext()) {
			Map.Entry<String, Object> xy = (Map.Entry<String, Object>)it.next();
			String key = xy.getKey().toString();
			JsonNode profileNode = (JsonNode)xy.getValue();
			profile = new OutboundProfile();
			profile.setRequestPolicy(profileNode.get("requestPolicy")!=null ? this.apimRequestPolicies.get(profileNode.get("requestPolicy").asText()) : null);
			profile.setResponsePolicy(profileNode.get("responsePolicy")!=null ? this.apimResponsePolicies.get(profileNode.get("responsePolicy").asText()) : null);
			profile.setRoutePolicy(profileNode.get("routePolicy")!=null ? this.apimRoutingPolicies.get(profileNode.get("routePolicy").asText()) : null);
			profile.setFaultHandlerPolicy(profileNode.get("faultHandlerPolicy")!=null ? this.apimFaultHandlerPolicies.get(profileNode.get("faultHandlerPolicy").asText()) : null);
			this.outboundProfiles.put(key, profile);
		}
	}

	@Override
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) throws AppException {
		if(this.outboundProfiles.size()!=0) {
			((ObjectNode)response).put("outboundProfiles", objectMapper.valueToTree(this.outboundProfiles));
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

}
