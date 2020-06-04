package com.axway.apim.adapter.apis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.API;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class JSONAPIAdapter extends APIAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(JSONAPIAdapter.class);
	
	CommandParameters params = CommandParameters.getInstance();
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private List<API> apis = new ArrayList<API>();

	public JSONAPIAdapter() {

	}
	
	@Override
	public boolean readConfig(Object config) throws AppException {
		if (config==null) return false;
		if (config instanceof String == false) return false;
		String myConfig = (String)config;
		try {
			this.apis = mapper.readValue(myConfig, new TypeReference<List<API>>(){});
		} catch (MismatchedInputException me) {
			try {
				API api = mapper.readValue(myConfig, API.class);
				apis.add(api);
			} catch (Exception pe) {
				throw new AppException("Cannot read apps from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, pe);
			}
		} catch (Exception e) {
			throw new AppException("Cannot read apps from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, e);
		}
		return true;
	}

	@Override
	public API getAPI(APIFilter filter, boolean logMessage) throws AppException {
		List<API> foundAPIs = getAPIs(filter, logMessage);
		return uniqueAPI(foundAPIs);
	}

	@Override
	public List<API> getAPIs(APIFilter filter, boolean logMessage) throws AppException {
		if(this.apis==null) return null;
		return apis;
	}
	
	private API uniqueAPI(List<API> foundAPIs) throws AppException {
		if(foundAPIs.size()>1) {
			throw new AppException("No unique API found", ErrorCode.UNKNOWN_API);
		}
		if(foundAPIs.size()==0) return null;
		return foundAPIs.get(0);
	}
}
