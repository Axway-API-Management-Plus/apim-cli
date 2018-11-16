package com.axway.apim.swagger.api.properties.corsprofiles;

import java.util.List;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

public class APIMgrCorsProfiles extends CorsProfiles {

	public APIMgrCorsProfiles(JsonNode config) throws AppException {
		try {
			this.corsProfiles = objectMapper.readValue( config.get("corsProfiles").toString(), new TypeReference<List<CorsProfile>>(){} );
		} catch (Exception e) {
			throw new AppException("Cant process existing cors profiles", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
	/*
	private boolean isProfileEmpty(OutboundProfile profile) {
		if(profile.getRequestPolicy()!=null && !profile.getRequestPolicy().equals("null")) return false;
		if(profile.getResponsePolicy()!=null && !profile.getResponsePolicy().equals("null")) return false;
		if(profile.getRoutePolicy()!=null && !profile.getRoutePolicy().equals("null")) return false;
		if(profile.getFaultHandlerPolicy()!=null && !profile.getFaultHandlerPolicy().equals("null")) return false;
		return true;
	}
	*/
}
