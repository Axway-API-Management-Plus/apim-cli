package com.axway.apim.actions.tasks.props;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OutboundProfileHandler implements PropertyHandler {

	@Override
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
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

}
