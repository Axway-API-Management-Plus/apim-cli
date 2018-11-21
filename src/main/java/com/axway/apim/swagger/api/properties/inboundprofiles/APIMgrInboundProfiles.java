package com.axway.apim.swagger.api.properties.inboundprofiles;

import java.util.Map;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

public class APIMgrInboundProfiles extends InboundProfiles {

	public APIMgrInboundProfiles(JsonNode config) throws AppException {
		try {
			this.inboundProfiles = objectMapper.readValue( config.get("inboundProfiles").toString(), new TypeReference<Map<String,InboundProfile>>(){} );
		} catch (Exception e) {
			throw new AppException("Cant process existing inbound profiles", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
}
