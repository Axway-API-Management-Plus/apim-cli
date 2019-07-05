package com.axway.apim.actions.tasks.props;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AuthenticationProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		if(desired.getAuthenticationProfiles().size()!=0) {
			((ObjectNode)response).replace("authenticationProfiles", objectMapper.valueToTree(desired.getAuthenticationProfiles()));
		}
		return response;
	}
}
