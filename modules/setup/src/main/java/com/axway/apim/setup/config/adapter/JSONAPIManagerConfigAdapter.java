package com.axway.apim.setup.config.adapter;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONAPIManagerConfigAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(JSONAPIManagerConfigAdapter.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	APIManagerConfig managerConfig;
	
	StandardImportParams importParams;

	public JSONAPIManagerConfigAdapter(StandardImportParams params) {
		this.importParams = params;
	}

	public void readConfig() throws AppException {
		String config = importParams.getConfig();
		File configFile = new File(config);
		if(!configFile.exists()) return;
		try {
			//mapper.registerModule(new SimpleModule().addDeserializer(ClientAppCredential.class, new AppCredentialsDeserializer()));
			this.managerConfig = mapper.readValue(configFile, APIManagerConfig.class);
		} catch (Exception e) {
			throw new AppException("Cannot read API-Manager configuration from config file: " + config, ErrorCode.CANT_READ_CONFIG_FILE, e);
		}
		return;
	}
	
	public APIManagerConfig getManagerConfig() throws AppException {
		if(this.managerConfig==null) readConfig();
		return this.managerConfig;
	}
}
