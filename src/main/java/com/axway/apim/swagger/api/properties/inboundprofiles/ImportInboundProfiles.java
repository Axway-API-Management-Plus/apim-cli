package com.axway.apim.swagger.api.properties.inboundprofiles;

import java.util.LinkedHashMap;
import java.util.Map;

import com.axway.apim.actions.tasks.props.PropertyHandler;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ImportInboundProfiles extends InboundProfiles implements PropertyHandler {

	public ImportInboundProfiles(JsonNode config) throws AppException {
		if(config instanceof MissingNode) {
			this.inboundProfiles = new LinkedHashMap<String, InboundProfile>();
			this.inboundProfiles.put("_default", getDefaultPassthroughProfile());
			return;
		}
		try {
			this.inboundProfiles = objectMapper.readValue( config.toString(), new TypeReference<Map<String, InboundProfile>>(){} );
		} catch (Exception e) {
			throw new AppException("Cant process inbound profiles", ErrorCode.UNXPECTED_ERROR, e);
		}
	}

	@Override
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) throws AppException {
		if(this.inboundProfiles.size()!=0) {
			((ObjectNode)response).replace("inboundProfiles", objectMapper.valueToTree(this.inboundProfiles));
		}
		return response;
	}
	
	private InboundProfile getDefaultPassthroughProfile() {
		InboundProfile passthroughProfile = new InboundProfile();
		passthroughProfile.setCorsProfile("_default");
		passthroughProfile.setSecurityProfile("_default");
		return passthroughProfile;
	}
}
