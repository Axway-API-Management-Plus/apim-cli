package com.axway.apim.lib.props;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.AuthenticationProfile;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OutboundProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(API desired, API actual, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		validateAuthenticationProfiles(desired);
		if(desired.getOutboundProfiles().size()!=0) {
			((ObjectNode)response).replace("outboundProfiles", objectMapper.valueToTree(desired.getOutboundProfiles()));
		}
		if(!APIManagerAdapter.hasAPIManagerVersion("7.6.2")){ // Versions before 7.6.2 don't support a FaultHandlerPolicy
			JsonNode outboundProfiles = response.get("outboundProfiles");
			if (outboundProfiles instanceof ObjectNode) {
				Iterator<JsonNode> it = outboundProfiles.elements();
				while(it.hasNext()) {
					ObjectNode profile = (ObjectNode)it.next();
					profile.remove("faultHandlerPolicy");
				}
		    }
		}
		return response;
	}
	
	private void validateAuthenticationProfiles(API desired) throws AppException {
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
