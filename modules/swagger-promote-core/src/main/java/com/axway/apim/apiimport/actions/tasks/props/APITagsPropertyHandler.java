package com.axway.apim.apiimport.actions.tasks.props;

import com.axway.apim.api.IAPI;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APITagsPropertyHandler implements PropertyHandler {
	
	ObjectMapper mapper = new ObjectMapper();
	
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		//((ObjectNode) response).put("tags", desired.getTags());
		((ObjectNode) response).put("tags", mapper.valueToTree(desired.getTags()));
		return response;
	}
}
