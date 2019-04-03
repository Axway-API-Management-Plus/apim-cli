package com.axway.apim.actions.tasks.props;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;

public interface PropertyHandler {
	public JsonNode handleProperty(IAPI desired, JsonNode response) throws AppException;
}
