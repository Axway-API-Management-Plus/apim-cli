package com.axway.apim.actions.tasks.props;

import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APIDescriptionPropertyHandler implements PropertyHandler {
	
	public JsonNode handleProperty(IAPI desired, JsonNode response) {
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
