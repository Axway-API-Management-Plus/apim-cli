package com.axway.apim.actions.tasks.props;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.properties.authenticationProfiles.AuthenticationProfile;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OutboundProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		validateAuthenticationProfiles(desired);
		APIManagerAdapter.getInstance().translateMethodIds(desired.getOutboundProfiles(), actual);
		if(desired.getOutboundProfiles().size()!=0) {
			((ObjectNode)response).replace("outboundProfiles", objectMapper.valueToTree(desired.getOutboundProfiles()));
		}
		if(APIManagerAdapter.getApiManagerVersion().startsWith("7.5")){
			JsonNode outboundProfiles = response.get("outboundProfiles").get("_default");
			
			if (outboundProfiles instanceof ObjectNode) {
				
		        ObjectNode object = (ObjectNode) outboundProfiles;
		        object.remove("faultHandlerPolicy");
		    }
		}
		return response;
	}
	
	private void validateAuthenticationProfiles(IAPI desired) throws AppException {
		Map<String, OutboundProfile> outboundProfiles = desired.getOutboundProfiles();
		List<AuthenticationProfile> authenticationProfiles = desired.getAuthenticationProfiles();
		Iterator<OutboundProfile> it = outboundProfiles.values().iterator();
		while(it.hasNext()) {
			OutboundProfile profile = it.next();
			if(profile.getAuthenticationProfile()!=null) {
				boolean profileFound = false;
				String profileName = profile.getAuthenticationProfile();
				if(profileName.equals("_default")) continue; // Not needed. If the default it not given it falls back to "No AuthN"
				if(authenticationProfiles!=null) {
					for(AuthenticationProfile authProfile : authenticationProfiles) {
						if(authProfile.getName().equals(profileName)) {
							profileFound = true;
							break;
						}
					}
				}
				if(!profileFound) {
					ErrorState.getInstance().setError("OutboundProfile is referencing a unknown AuthenticationProfile: '"+profileName+"'", ErrorCode.REFERENCED_PROFILE_INVALID, false);
					throw new AppException("OutboundProfile is referencing a unknown AuthenticationProfile: '"+profileName+"'", ErrorCode.REFERENCED_PROFILE_INVALID);
				}
			}
		}
	}

}
