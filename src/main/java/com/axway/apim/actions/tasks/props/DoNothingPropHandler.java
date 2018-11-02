package com.axway.apim.actions.tasks.props;

import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;

public class DoNothingPropHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) {
		return response;
	}

}
