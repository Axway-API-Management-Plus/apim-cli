package com.axway.apim.swagger.api.properties.securityprofiles;

import java.util.List;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

public class APIMgrSecurityProfiles extends SecurityProfiles {

	public APIMgrSecurityProfiles(JsonNode config) throws AppException {
		try {
			this.securityProfiles = objectMapper.readValue( config.get("securityProfiles").toString(), new TypeReference<List<SecurityProfile>>(){} );
		} catch (Exception e) {
			throw new AppException("Cant process existing security profiles", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
}
