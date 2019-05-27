package com.axway.apim.actions.tasks.props;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RoutingKeyPropHandler implements PropertyHandler {
	
	static Logger LOG = LoggerFactory.getLogger(RoutingKeyPropHandler.class);

	public JsonNode handleProperty(IAPI desired, JsonNode response) {
		try {
			if(APIManagerAdapter.getInstance().hasAdminAccount()) {
				if(APIManagerAdapter.getApiManagerConfig("apiRoutingKeyEnabled").equals("false")) {
					ErrorState.getInstance().setError("API-Manager Query-String Routing option is disabled. Please turn it on to use apiRoutingKey.", ErrorCode.QUERY_STRING_ROUTING_DISABLED, false);
					throw new RuntimeException();
				}
			} else {
				LOG.info("Please note, that apiRoutingKey is set for this API, but can't validate with orgAdmin only if Query-String-Routing is turned on.");
			}
			((ObjectNode) response).put("apiRoutingKey", desired.getApiRoutingKey());
		} catch (AppException e) {
			ErrorState.getInstance().setError("Can read apiRoutingKeyEnabled from API-Manager config", ErrorCode.API_MANAGER_COMMUNICATION);
			throw new RuntimeException(e);
		}
		return response;
	}
}
