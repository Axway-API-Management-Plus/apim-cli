package com.axway.apim.swagger.api.properties.corsprofiles;

import java.util.List;
import java.util.Vector;

import com.axway.apim.actions.tasks.props.PropertyHandler;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ImportCorsProfiles extends CorsProfiles implements PropertyHandler {

	public ImportCorsProfiles(JsonNode config) throws AppException {
		if(config instanceof MissingNode) return;
		try {
			this.corsProfiles = objectMapper.readValue( config.toString(), new TypeReference<List<CorsProfile>>(){} );
		} catch (Exception e) {
			throw new AppException("Can't process cors profiles", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	@Override
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) throws AppException {
		if(this.corsProfiles.size()!=0) {
			((ObjectNode)response).replace("corsProfiles", objectMapper.valueToTree(this.corsProfiles));
		}
		return response;
	}
}
