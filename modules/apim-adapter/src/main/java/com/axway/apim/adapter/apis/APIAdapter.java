package com.axway.apim.adapter.apis;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;

public abstract class APIAdapter {

	public APIAdapter() { }
	
	public abstract boolean readConfig(Object config) throws AppException;
	
	public abstract List<API> getAPIs(APIFilter filter, boolean logMessage) throws AppException;
	
	public abstract API getAPI(APIFilter filter, boolean logMessage) throws AppException;
	
	public static APIAdapter create(Object config) throws AppException{
		APIAdapter adapter;
		// Simple static factory for now
		if(config instanceof APIManagerAdapter) {
			adapter = new APIManagerAPIAdapter();
			if(adapter.readConfig(config)) {
				return adapter;
			}
		} else if(config instanceof String) {
			adapter = new JSONAPIAdapter();
			if(adapter.readConfig(config)) {
				return adapter;
			} else {
				return null;	
			}
		}
		return null;
	}
}
