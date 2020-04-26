package com.axway.apim.actions.tasks.props;

import com.axway.apim.api.model.CorsProfile;
import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CorsProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		if(desired.getCorsProfiles().size()!=0) {
			((ObjectNode)response).replace("corsProfiles", objectMapper.valueToTree(desired.getCorsProfiles()));
			// Assign the Cors-Profile to be used (today we support only one, that means, not the default)
			for(CorsProfile profile: desired.getCorsProfiles()) {
				if(!profile.getName().equals("_default")) {
					JsonNode corsProfile = response.get("inboundProfiles").get("_default");
					((ObjectNode) corsProfile).put("corsProfile", profile.getName());
				}
			}
		}
		return response;
	}

}
