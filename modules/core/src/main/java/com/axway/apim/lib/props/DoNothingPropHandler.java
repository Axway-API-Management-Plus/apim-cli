package com.axway.apim.lib.props;

import com.axway.apim.api.API;
import com.fasterxml.jackson.databind.JsonNode;

public class DoNothingPropHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(API desired, API actual, JsonNode response) {
		return response;
	}

}
