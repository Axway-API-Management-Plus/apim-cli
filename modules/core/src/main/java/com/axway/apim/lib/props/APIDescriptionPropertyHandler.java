package com.axway.apim.lib.props;

import com.axway.apim.api.API;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APIDescriptionPropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(API desired, API actual, JsonNode response) {
		if(desired.getDescriptionType()!=null) {
			String descriptionType = desired.getDescriptionType();
			((ObjectNode) response).put("descriptionType", descriptionType);
			if(descriptionType.equals("manual")) {
				((ObjectNode) response).put("descriptionManual", desired.getDescriptionManual());
			}
			if(descriptionType.equals("markdown")) {
				((ObjectNode) response).put("descriptionMarkdown", desired.getDescriptionMarkdown());
			}
			if(descriptionType.equals("url")) {
				((ObjectNode) response).put("descriptionUrl", desired.getDescriptionUrl());
			}
		}
		return response;
	}
}
