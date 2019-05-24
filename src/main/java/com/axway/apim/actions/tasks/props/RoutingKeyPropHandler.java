package com.axway.apim.actions.tasks.props;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RoutingKeyPropHandler implements PropertyHandler {
	
	static Logger LOG = LoggerFactory.getLogger(RoutingKeyPropHandler.class);

	public JsonNode handleProperty(IAPI desired, JsonNode response) {
		((ObjectNode) response).put("apiRoutingKey", desired.getApiRoutingKey());
		return response;
	}
}
