package com.axway.apim.appimport.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class JSONConfigClientAppAdapter extends ClientAppAdapter {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	List<ClientApplication> apps;

	public JSONConfigClientAppAdapter() {
	}

	@Override
	public boolean readConfig(Object config) throws AppException {
		if (config==null) return false;
		if (config instanceof String == false) return false;
		String myConfig = (String)config;
		File configFile = new File(myConfig);
		if(!configFile.exists()) return false;
		try {
			this.apps = mapper.readValue(configFile, new TypeReference<List<ClientApplication>>(){});
		} catch (MismatchedInputException me) {
			try {
				ClientApplication app = mapper.readValue(configFile, ClientApplication.class);
				this.apps = new ArrayList<ClientApplication>();
				this.apps.add(app);
			} catch (Exception pe) {
				throw new AppException("Cannot read apps from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, pe);
			}
		} catch (Exception e) {
			throw new AppException("Cannot read apps from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, e);
		}
		return true;
	}
	
	@Override
	public List<ClientApplication> getApplications(ClientAppFilter filter) throws AppException {
		throw new UnsupportedOperationException("Filtering results is not supported for the JSON implementation");
	}

	@Override
	public ClientApplication getApplication(ClientApplication applicationName) throws AppException {
		if(this.apps==null) return null;
		for(ClientApplication app : this.apps) {
			if(applicationName.getName().equals(app.getName())) return app;
		}
		return null;
	}

	@Override
	public ClientApplication createApplication(ClientApplication app) throws AppException {
		throw new UnsupportedOperationException("createApplication not implemented for JSONConfigClientAppAdapter");
	}

	@Override
	public List<ClientApplication> getApplications() throws AppException {
		return this.apps;
	}
}
