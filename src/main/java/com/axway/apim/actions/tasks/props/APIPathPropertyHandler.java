package com.axway.apim.actions.tasks.props;

import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APIPathPropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) {
		((ObjectNode) response).put("path", desired.getApiPath());
		return response;
	}
}
