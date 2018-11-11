package com.axway.apim.swagger.api.properties;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class APIAuthentication {
	protected String name = "";
	protected String type = "";
	
	protected String properties = "";
	
	protected ArrayNode jsonConfig;
	
	public APIAuthentication(JsonNode config) throws AppException {
		this.jsonConfig = (ArrayNode)config;
		if(config.size()>1) throw new AppException("Supporting only one security device!", ErrorCode.UNSUPPORTED_FEATURE);
		if(config.size()==0) return; // No security devices configured (new API-Proxy)
		// For now, we only support one security device!
		this.name = config.get(0).get("name").asText();
		this.type = config.get(0).get("type").asText();
		this.properties = config.get(0).get("properties").toString();
	}

	public JsonNode getJsonConfig() {
		return this.jsonConfig;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APIAuthentication) {
			APIAuthentication otherAuthN = (APIAuthentication)other;
			if(!otherAuthN.type.equals(this.type)) return false;
			if(!otherAuthN.name.equals(this.name)) return false;
			if(!otherAuthN.properties.equals(this.properties)) return false;
		} else {
			return false;
		}
		return true;
	}
}
