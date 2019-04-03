package com.axway.apim.actions.tasks.props;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APITagsPropertyHandler implements PropertyHandler {
	
	ObjectMapper mapper = new ObjectMapper();
	
	public JsonNode handleProperty(IAPI desired, JsonNode response) throws AppException {
		//((ObjectNode) response).put("tags", desired.getTags());
		((ObjectNode) response).put("tags", mapper.valueToTree(desired.getTags()));
		return response;
	}
}
