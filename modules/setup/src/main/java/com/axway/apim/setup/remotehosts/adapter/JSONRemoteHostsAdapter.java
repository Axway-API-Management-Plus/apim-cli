package com.axway.apim.setup.remotehosts.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.jackson.UserDeserializer;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class JSONRemoteHostsAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(JSONRemoteHostsAdapter.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	List<RemoteHost> remoteHosts;
	
	StandardImportParams importParams;

	public JSONRemoteHostsAdapter(StandardImportParams params) {
		this.importParams = params;
	}

	private void readConfig() throws AppException {
		String config = importParams.getConfig();
		String stage = importParams.getStage();

		File configFile = Utils.locateConfigFile(config);
		if(!configFile.exists()) return;
		File stageConfig = Utils.getStageConfig(stage, configFile);
		List<RemoteHost> baseRemoteHosts;
		// Try to read a list of applications
		try {
			baseRemoteHosts = mapper.reader()
					.withAttribute(UserDeserializer.DeserializeParams.useLoginName, true)
					.forType(new TypeReference<List<RemoteHost>>(){})
					.readValue(Utils.substitueVariables(configFile));
			//baseRemoteHosts = mapper.readValue(Utils.substitueVariables(configFile), new TypeReference<List<RemoteHost>>(){});
			if(stageConfig!=null) {
				ErrorState.getInstance().setError("Stage overrides are not supported for remote host lists.", ErrorCode.CANT_READ_CONFIG_FILE, false);
				throw new AppException("Stage overrides are not supported for remote host lists.", ErrorCode.CANT_READ_CONFIG_FILE);
			} else {
				this.remoteHosts = baseRemoteHosts;
			}
		// Try to read single remote host
		} catch (MismatchedInputException me) {
			try {
				RemoteHost remoteHost = mapper.reader()
						.withAttribute(UserDeserializer.DeserializeParams.useLoginName, true)
						.forType(RemoteHost.class)
						.readValue(Utils.substitueVariables(configFile));
				if(stageConfig!=null) {
					try {
						ObjectReader updater = mapper.readerForUpdating(remoteHost);
						remoteHost = updater.readValue(Utils.substitueVariables(stageConfig));
					} catch (FileNotFoundException e) {
						LOG.warn("No config file found for stage: '"+stage+"'");
					}
				}
				this.remoteHosts = new ArrayList<RemoteHost>();
				this.remoteHosts.add(remoteHost);
			} catch (Exception pe) {
				throw new AppException("Cannot read remote host(s) from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, pe);
			}
		} catch (Exception e) {
			throw new AppException("Cannot read remote host(s) from config file: " + config, ErrorCode.ACCESS_ORGANIZATION_ERR, e);
		}
		return;
	}
	
	public List<RemoteHost> getRemoteHosts() throws AppException {
		if(this.remoteHosts==null) readConfig();
		return this.remoteHosts;
	}
}
