package com.axway.apim.lib.props;

import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;

public interface PropertyHandler {
	public JsonNode handleProperty(API desired, API actual, JsonNode response) throws AppException;
}
