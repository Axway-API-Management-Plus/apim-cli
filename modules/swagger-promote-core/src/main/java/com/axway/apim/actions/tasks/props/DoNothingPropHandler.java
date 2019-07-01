package com.axway.apim.actions.tasks.props;

import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;

public class DoNothingPropHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, JsonNode response) {
		return response;
	}

}
