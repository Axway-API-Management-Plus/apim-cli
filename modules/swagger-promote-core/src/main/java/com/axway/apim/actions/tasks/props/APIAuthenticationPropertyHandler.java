package com.axway.apim.actions.tasks.props;

import com.axway.apim.api.model.AuthenticationProfile;
import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class APIAuthenticationPropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		preProcessAuthenticationProfiles(desired);
		ArrayNode devices = (ArrayNode) ((ArrayNode) response.findPath("securityProfiles")).get(0).get("devices");
		// We put all security devices from the desired state into the request
		devices.removeAll();
		//devices.addAll((ArrayNode)desired.getAuthentication().getJsonConfig());
		return response;
	}
	
	private void preProcessAuthenticationProfiles(IAPI desired) throws AppException {
		for(AuthenticationProfile profile : desired.getAuthenticationProfiles()) {
			// Validate we have only one default declared
			if(!profile.getName().equals("_default") && profile.getIsDefault()) {
				ErrorState.getInstance().setError("Not allowed to configure non _default profile: '"+profile.getName()+"' as default.", ErrorCode.CANT_READ_CONFIG_FILE, false);
				throw new AppException("Not allowed to configure non _default profile as default to true.", ErrorCode.CANT_READ_CONFIG_FILE);
			}
		}
	}
}
