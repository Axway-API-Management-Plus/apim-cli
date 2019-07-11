package com.axway.apim.actions.tasks.props;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APIPathPropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		((ObjectNode) response).put("path", desired.getPath());
		return response;
	}
}
