package com.axway.apim.actions.tasks.props;

import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ServiceProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		if(desired.getServiceProfiles().size()!=0) {
			((ObjectNode)response).replace("serviceProfiles", objectMapper.valueToTree(desired.getServiceProfiles()));
		}
		return response;
	}

}
