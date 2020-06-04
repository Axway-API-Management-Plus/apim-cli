package com.axway.apim.lib.props;

import com.axway.apim.api.API;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APINamePropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(API desired, API actual, JsonNode response) {
		((ObjectNode) response).put("name", desired.getName());
		return response;
	}
}
