package com.axway.apim.lib;

import com.fasterxml.jackson.databind.JsonNode;

public class StagingJsonNode {
	private JsonNode jsonNode;

	public StagingJsonNode(JsonNode jsonNode) {
		super();
		this.jsonNode = jsonNode;
	}
	
	StagingJsonNode get(String fieldName) {
		JsonNode node = this.jsonNode.get(fieldName);
		return new StagingJsonNode(node);
	}

}
