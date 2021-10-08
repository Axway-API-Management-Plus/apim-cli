package com.axway.apim.setup.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.jackson.RemotehostDeserializer;
import com.axway.apim.adapter.jackson.UserDeserializer;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class JSONAPIManagerConfigAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(JSONAPIManagerConfigAdapter.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	APIManagerConfig managerConfig;
	
	StandardImportParams importParams;

	public JSONAPIManagerConfigAdapter(StandardImportParams params) {
		this.importParams = params;
	}

	private void readConfig() throws AppException {
		String config = importParams.getConfig();
		String stage = importParams.getStage();
		File configFile = Utils.locateConfigFile(config);
		if(!configFile.exists()) return;
		File stageConfig = Utils.getStageConfig(stage, importParams.getStageConfig(), configFile);
		APIManagerConfig baseConfig;
		try {
			mapper.configOverride(Map.class).setMergeable(true);
			baseConfig = mapper.reader()
					.withAttribute(UserDeserializer.Params.useLoginName, true)
					.withAttribute(RemotehostDeserializer.Params.validateRemoteHost, true)
					.forType(APIManagerConfig.class)
					.readValue(Utils.substitueVariables(configFile));
			if(stageConfig!=null) {
				try {
					ObjectReader updater = mapper.readerForUpdating(baseConfig);
					this.managerConfig = updater.readValue(Utils.substitueVariables(stageConfig));
					LOG.info("Successfully read stage configuration file: " + stageConfig);
				} catch (FileNotFoundException e) {
					LOG.warn("No config file found for stage: '"+stage+"'");
				}
			} else {
				this.managerConfig = baseConfig;
			}
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
