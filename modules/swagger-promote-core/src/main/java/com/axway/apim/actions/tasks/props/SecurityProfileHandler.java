package com.axway.apim.actions.tasks.props;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SecurityProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		if(desired.getSecurityProfiles().size()!=0) {
			preProcessSecurityProfiles(desired);
			((ObjectNode)response).replace("securityProfiles", objectMapper.valueToTree(desired.getSecurityProfiles()));
		}
		return response;
	}
	
	private void preProcessSecurityProfiles(IAPI desired) throws AppException {
		for(SecurityProfile profile : desired.getSecurityProfiles()) {
			// Validate we have only one default declared
			if(!profile.getName().equals("_default") && profile.getIsDefault().equals("true")) {
				ErrorState.getInstance().setError("Not allowed to configure non _default profile: '"+profile.getName()+"' as default.", ErrorCode.CANT_READ_CONFIG_FILE, false);
				throw new AppException("Not allowed to configure non _default profile as default to true.", ErrorCode.CANT_READ_CONFIG_FILE);
			}
		}
	}
}
