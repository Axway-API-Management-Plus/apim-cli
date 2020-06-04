package com.axway.apim.lib.props;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class VhostPropertyHandler implements PropertyHandler {
	
	static Logger LOG = LoggerFactory.getLogger(VhostPropertyHandler.class);
	
	public VhostPropertyHandler() {
		super();
	}

	public VhostPropertyHandler(List<String> changedProps) {
		if(changedProps.contains("vhost")) {
			// Make sure, Vhost isn't updated with all the other properties
			changedProps.remove("vhost");
		}
	}

	@Override
	public JsonNode handleProperty(API desired, API actual, JsonNode response) throws AppException {
		((ObjectNode) response).put("vhost", desired.getVhost());
		return response;
	}
}