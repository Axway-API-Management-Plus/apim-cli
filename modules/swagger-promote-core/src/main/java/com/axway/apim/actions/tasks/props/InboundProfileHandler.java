package com.axway.apim.actions.tasks.props;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.properties.inboundprofiles.InboundProfile;
import com.axway.apim.swagger.api.state.APIMethod;
import com.axway.apim.swagger.api.state.AbstractAPI;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class InboundProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		if(desired.getInboundProfiles().size()!=0) {
			translateOperationId(desired, actual);
			((ObjectNode)response).replace("inboundProfiles", objectMapper.valueToTree(desired.getInboundProfiles()));
		}
		return response;
	}
	
	private void translateOperationId(IAPI desired, IAPI actual) throws AppException {
		boolean defaultFound = false;
		Map<String, InboundProfile> profiles = desired.getInboundProfiles();
		Iterator<String> keys = profiles.keySet().iterator();
		Map<String, InboundProfile> updatedProfiles = new LinkedHashMap<String, InboundProfile>();
		while(keys.hasNext()) {
			String key = keys.next();
			if(key.equals("_default")) {
				if(defaultFound) { // Already having a default, can't have more than one
					ErrorState.getInstance().setError("You can't configured more than one Default Inbound-Profile.", ErrorCode.CANT_READ_CONFIG_FILE, false);
					throw new AppException("You can't configured more than one Default Inbound-Profile.", ErrorCode.CANT_READ_CONFIG_FILE);
				}
				defaultFound = true;
			} else {
				String internalMethodId = lookupAPIMethodId(key, actual);
				InboundProfile value = profiles.get(key);
				keys.remove();
				updatedProfiles.put(internalMethodId, value);
			}
		}
		profiles.putAll(updatedProfiles);
	}
	
	private String lookupAPIMethodId(String operationId, IAPI actual) throws AppException {
		if(((AbstractAPI)actual).getApiMethods()==null) {
			((AbstractAPI)actual).setApiMethods(APIManagerAdapter.getInstance().getAllMethodsForAPI(actual.getId()));
		}
		for(APIMethod method : ((AbstractAPI)actual).getApiMethods()) {
			if(method.getName().equals(operationId)) {
				return method.getId();
			}
		}
		ErrorState.getInstance().setError("No operation found with operationId: '"+operationId+"'", ErrorCode.CANT_READ_CONFIG_FILE, false);
		throw new AppException("No operation found with operationId: '"+operationId+"'", ErrorCode.CANT_READ_CONFIG_FILE);
	}
}
