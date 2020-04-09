package com.axway.apim.actions.tasks.props;

import com.axway.apim.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;

public class DoNothingPropHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) {
		return response;
	}

}
