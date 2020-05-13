package com.axway.apim.adapter.apis;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class APIAdapter {

	public APIAdapter() { }
	
	public abstract List<JsonNode> getAPIs(APIFilter filter, boolean logMessage) throws AppException;
	
	public abstract JsonNode getAPI(APIFilter filter, boolean logMessage) throws AppException;
	
	public static APIAdapter create(Object config){
		// Simple static factory for now
		if(config instanceof APIManagerAdapter) {
			return new APIManagerAPIAdapter();
		} else {
			return null;
		}
	}
}
