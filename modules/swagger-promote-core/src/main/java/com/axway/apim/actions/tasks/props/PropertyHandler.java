package com.axway.apim.actions.tasks.props;

import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;

public interface PropertyHandler {
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException;
}
