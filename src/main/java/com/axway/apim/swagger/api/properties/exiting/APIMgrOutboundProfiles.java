package com.axway.apim.swagger.api.properties.exiting;

import java.util.LinkedHashMap;
import java.util.Vector;

import com.axway.apim.swagger.api.properties.OutboundProfile;
import com.axway.apim.swagger.api.properties.OutboundProfiles;
import com.fasterxml.jackson.databind.JsonNode;

public class APIMgrOutboundProfiles extends OutboundProfiles {

	public APIMgrOutboundProfiles(JsonNode config) {
		OutboundProfile profile;
		this.outboundProfiles = new LinkedHashMap<String, Object>();
		JsonNode existingOutboundProfiles = config.get("outboundProfiles");
		for(JsonNode profileNode : existingOutboundProfiles) {
			profile = new OutboundProfile();
			profile.setRequestPolicy(profileNode.get("requestPolicy")!=null ? profileNode.get("requestPolicy").asText() : null);
			profile.setResponsePolicy(profileNode.get("responsePolicy")!=null ? profileNode.get("responsePolicy").asText() : null);
			profile.setRoutePolicy(profileNode.get("routePolicy")!=null ? profileNode.get("routePolicy").asText() : null);
			profile.setFaultHandlerPolicy(profileNode.get("faultHandlerPolicy")!=null ? profileNode.get("faultHandlerPolicy").asText() : null);
			if(!isProfileEmpty(profile)) {
				this.outboundProfiles.put("1", profile);
			}
		}
	}
	
	private boolean isProfileEmpty(OutboundProfile profile) {
		if(profile.getRequestPolicy()!=null && !profile.getRequestPolicy().equals("null")) return false;
		if(profile.getResponsePolicy()!=null && !profile.getResponsePolicy().equals("null")) return false;
		if(profile.getRoutePolicy()!=null && !profile.getRoutePolicy().equals("null")) return false;
		if(profile.getFaultHandlerPolicy()!=null && !profile.getFaultHandlerPolicy().equals("null")) return false;
		return true;
	}

}
