package com.axway.apim.actions.tasks.props;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;

public interface PropertyHandler {
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) throws AppException;
}
