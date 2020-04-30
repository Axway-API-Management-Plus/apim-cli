package com.axway.apim.lib.props;

import com.axway.apim.api.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APIVersionPropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) {
		((ObjectNode) response).put("version", desired.getVersion());
		return response;
	}
}
