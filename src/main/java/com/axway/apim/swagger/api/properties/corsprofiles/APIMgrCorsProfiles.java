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
}
