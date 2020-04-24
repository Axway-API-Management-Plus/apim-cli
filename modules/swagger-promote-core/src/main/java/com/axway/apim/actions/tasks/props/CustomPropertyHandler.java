package com.axway.apim.actions.tasks.props;

import java.util.Iterator;

import com.axway.apim.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CustomPropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) {
		Iterator<String> propKeys = desired.getCustomProperties().keySet().iterator();
		while(propKeys.hasNext()) {
			String propKey = propKeys.next();
			String propValue = desired.getCustomProperties().get(propKey);
			((ObjectNode) response).put(propKey, propValue);
		}
		return response;
	}
}
