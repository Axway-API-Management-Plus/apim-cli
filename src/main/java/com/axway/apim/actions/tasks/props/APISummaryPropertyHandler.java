package com.axway.apim.actions.tasks.props;

import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APISummaryPropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(IAPI desired, JsonNode response) {
		((ObjectNode) response).put("summary", desired.getSummary());
		return response;
	}
}
