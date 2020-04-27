package com.axway.apim.apiimport.actions.tasks.props;

import com.axway.apim.api.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APINamePropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) {
		((ObjectNode) response).put("name", desired.getName());
		return response;
	}
}
