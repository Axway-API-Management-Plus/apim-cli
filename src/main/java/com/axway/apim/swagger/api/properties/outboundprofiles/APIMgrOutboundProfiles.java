package com.axway.apim.swagger.api.properties.outboundprofiles;

import java.util.LinkedHashMap;
import java.util.Map;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

public class APIMgrOutboundProfiles extends OutboundProfiles {

	public APIMgrOutboundProfiles(JsonNode config) throws AppException {
		if(config instanceof MissingNode) return;
		try {
			this.outboundProfiles = objectMapper.readValue( config.get("outboundProfiles").toString(), new TypeReference<Map<String,OutboundProfile>>(){} );
		} catch (Exception e) {
			throw new AppException("Cant process existing outbound profiles", ErrorCode.UNXPECTED_ERROR, e);
		}
		
		/*
		OutboundProfile profile;
		//this.outboundProfiles = new ArrayList<Map<<String, OutboundProfile>>();
		JsonNode existingOutboundProfiles = config.get("outboundProfiles");
		for(JsonNode profileNode : existingOutboundProfiles) {
			profile = new OutboundProfile();
			profile.setRequestPolicy(profileNode.get("requestPolicy")!=null ? profileNode.get("requestPolicy").asText() : null);
			profile.setResponsePolicy(profileNode.get("responsePolicy")!=null ? profileNode.get("responsePolicy").asText() : null);
			profile.setRoutePolicy(profileNode.get("routePolicy")!=null ? profileNode.get("routePolicy").asText() : null);
			profile.setFaultHandlerPolicy(profileNode.get("faultHandlerPolicy")!=null ? profileNode.get("faultHandlerPolicy").asText() : null);
			if(!isProfileEmpty(profile)) {
		//		this.outboundProfiles.put("1", profile);
			}
		}*/
	}
	/*
	private boolean isProfileEmpty(OutboundProfile profile) {
		if(profile.getRequestPolicy()!=null && !profile.getRequestPolicy().equals("null")) return false;
		if(profile.getResponsePolicy()!=null && !profile.getResponsePolicy().equals("null")) return false;
		if(profile.getRoutePolicy()!=null && !profile.getRoutePolicy().equals("null")) return false;
		if(profile.getFaultHandlerPolicy()!=null && !profile.getFaultHandlerPolicy().equals("null")) return false;
		return true;
	}*/

}
