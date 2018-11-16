package com.axway.apim.swagger.api.properties.securityprofiles;

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

public class ImportSecurityProfiles extends SecurityProfiles implements PropertyHandler {

	public ImportSecurityProfiles(JsonNode config) throws AppException {
		if(config instanceof MissingNode) return;
		try {
			this.securityProfiles = objectMapper.readValue( config.toString(), new TypeReference<List<SecurityProfile>>(){} );
		} catch (Exception e) {
			throw new AppException("Cant process security profiles", ErrorCode.UNXPECTED_ERROR, e);
		} 
		
	}

	@Override
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) throws AppException {
		if(this.securityProfiles.size()!=0) {
			((ObjectNode)response).replace("securityProfiles", objectMapper.valueToTree(this.securityProfiles));
		}
		return response;
	}
}
