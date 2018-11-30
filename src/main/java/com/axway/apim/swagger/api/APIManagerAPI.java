package com.axway.apim.swagger.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.fasterxml.jackson.databind.JsonNode;

public class APIManagerAPI extends AbstractAPIDefinition implements IAPIDefinition {
	
	static Logger LOG = LoggerFactory.getLogger(APIManagerAPI.class);

	JsonNode apiConfiguration;

	public APIManagerAPI() throws AppException {
		super();
	}

	public APIManagerAPI(JsonNode apiConfiguration) {
		this.apiConfiguration = apiConfiguration;
	}
	
	@Override
	public String getState() throws AppException {
		if(this.deprecated!=null 
				&& this.deprecated.equals("true")) return IAPIDefinition.STATE_DEPRECATED;
		return super.getState();
	}
}
