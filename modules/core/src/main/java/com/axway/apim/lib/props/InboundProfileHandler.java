package com.axway.apim.lib.props;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.IAPI;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.CorsProfile;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class InboundProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		if(desired.getInboundProfiles().size()!=0) {
			validateSecurityProfiles(desired);
			validateCORSProfiles(desired);
			translateOperationId(desired, actual);
			((ObjectNode)response).replace("inboundProfiles", objectMapper.valueToTree(desired.getInboundProfiles()));
		}
		return response;
	}
	
	private void validateSecurityProfiles(IAPI desired) throws AppException {	
		Map<String, InboundProfile> inboundProfiles = desired.getInboundProfiles();
		List<SecurityProfile> securityProfiles = desired.getSecurityProfiles();
		Iterator<InboundProfile> it = inboundProfiles.values().iterator();
		while(it.hasNext()) {
			InboundProfile profile = it.next();
			if(profile.getSecurityProfile()!=null) {
				boolean profileFound = false;
				String profileName = profile.getSecurityProfile();
				if(securityProfiles!=null) {
					for(SecurityProfile secProfile : securityProfiles) {
						if(secProfile.getName().equals(profileName)) {
							profileFound = true;
							break;
						}
					}
				}
				if(!profileFound) {
					ErrorState.getInstance().setError("InboundProfile is referencing a unknown SecurityProfile: '"+profileName+"'", ErrorCode.REFERENCED_PROFILE_INVALID, false);
					throw new AppException("Inbound profile is referencing a unknown SecurityProfile: '"+profileName+"'", ErrorCode.REFERENCED_PROFILE_INVALID);
				}
			}
		}
	}
	
	private void validateCORSProfiles(IAPI desired) throws AppException {	
		Map<String, InboundProfile> inboundProfiles = desired.getInboundProfiles();
		List<CorsProfile> corsProfiles = desired.getCorsProfiles();
		Iterator<InboundProfile> it = inboundProfiles.values().iterator();
		while(it.hasNext()) {
			InboundProfile profile = it.next();
			if(profile.getCorsProfile()!=null) {
				boolean profileFound = false;
				String profileName = profile.getCorsProfile();
				if(corsProfiles!=null) {
					for(CorsProfile corsProfile : corsProfiles) {
						if(corsProfile.getName().equals(profileName)) {
							profileFound = true;
							break;
						}
					}
				}
				if(!profileFound) {
					ErrorState.getInstance().setError("InboundProfile is referencing a unknown CorsProfile: '"+profileName+"'", ErrorCode.REFERENCED_PROFILE_INVALID, false);
					throw new AppException("Inbound profile is referencing a unknown CorsProfile: '"+profileName+"'", ErrorCode.REFERENCED_PROFILE_INVALID);
				}
			}
		}
	}
	
	private void translateOperationId(IAPI desired, IAPI actual) throws AppException {
		boolean defaultFound = false;
		Map<String, InboundProfile> profiles = desired.getInboundProfiles();
		Iterator<String> keys = profiles.keySet().iterator();
		Map<String, InboundProfile> updatedProfiles = new LinkedHashMap<String, InboundProfile>();
		while(keys.hasNext()) {
			String operationId = keys.next();
			if(operationId.equals("_default")) {
				if(defaultFound) { // Already having a default, can't have more than one
					ErrorState.getInstance().setError("You can't configured more than one Default Inbound-Profile.", ErrorCode.CANT_READ_CONFIG_FILE, false);
					throw new AppException("You can't configured more than one Default Inbound-Profile.", ErrorCode.CANT_READ_CONFIG_FILE);
				}
				defaultFound = true;
			} else {
				String internalMethodId = lookupAPIMethodId(operationId, actual);
				if(internalMethodId.equals(operationId)) continue;
				InboundProfile value = profiles.get(operationId);
				keys.remove();
				updatedProfiles.put(internalMethodId, value);
			}
		}
		profiles.putAll(updatedProfiles);
	}
	
	private String lookupAPIMethodId(String operationId, IAPI actual) throws AppException {
		if(((API)actual).getApiMethods()==null) {
			((API)actual).setApiMethods(APIManagerAdapter.getInstance().getAllMethodsForAPI(actual.getId()));
		}
		for(APIMethod method : ((API)actual).getApiMethods()) {
			if(method.getName().equals(operationId)) {
				return method.getId();
			}
			if(method.getId().equals(operationId)) { // MethodIds are already translated, if an existing APIs is updated
				return operationId;
			}
		}
		ErrorState.getInstance().setError("No operation found with operationId: '"+operationId+"'", ErrorCode.API_OPERATION_NOT_FOUND, false);
		throw new AppException("No operation found with operationId: '"+operationId+"'", ErrorCode.API_OPERATION_NOT_FOUND);
	}
}
