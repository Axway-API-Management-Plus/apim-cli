package com.axway.apim.swagger.api.properties.outboundprofiles;

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
	}
}
