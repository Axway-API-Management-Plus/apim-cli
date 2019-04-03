package com.axway.apim.actions.tasks.props;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class APIAuthenticationPropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(IAPI desired, JsonNode response) throws AppException {
		ArrayNode devices = (ArrayNode) ((ArrayNode) response.findPath("securityProfiles")).get(0).get("devices");
		// We put all security devices from the desired state into the request
		devices.removeAll();
		//devices.addAll((ArrayNode)desired.getAuthentication().getJsonConfig());
		return response;
	}
}
