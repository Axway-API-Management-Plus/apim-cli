package com.axway.apim.setup.adapter;

import com.axway.apim.adapter.jackson.RemotehostDeserializer;
import com.axway.apim.adapter.jackson.UserDeserializer;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.setup.model.APIManagerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class APIManagerConfigAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerConfigAdapter.class);
    private APIManagerConfig managerConfig;
    private final StandardImportParams importParams;

    public APIManagerConfigAdapter(StandardImportParams params) {
        this.importParams = params;
    }

    private void readConfig() throws AppException {
        ObjectMapper mapper;
        String config = importParams.getConfig();
        String stage = importParams.getStage();
        File configFile = Utils.locateConfigFile(config);
        if (!configFile.exists()) return;
        File stageConfig = Utils.getStageConfig(stage, importParams.getStageConfig(), configFile);
        APIManagerConfig baseConfig;
        try {
            mapper = Utils.createObjectMapper(configFile);
            mapper.configOverride(Map.class).setMergeable(true);
            baseConfig = mapper.reader()
                    .withAttribute(UserDeserializer.Params.USE_LOGIN_NAME, true)
                    .withAttribute(RemotehostDeserializer.Params.validateRemoteHost, true)
                    .forType(APIManagerConfig.class)
                    .readValue(Utils.substituteVariables(configFile));
        } catch (Exception e) {
            throw new AppException("Cannot read API-Manager configuration from config file: " + config, ErrorCode.CANT_READ_CONFIG_FILE, e);
        }
        if (stageConfig != null) {
            try {
                ObjectReader updater = mapper.readerForUpdating(baseConfig);
                this.managerConfig = updater.readValue(Utils.substituteVariables(stageConfig));
                LOG.info("Successfully read stage configuration file: {}", stageConfig);
            } catch (IOException e) {
                LOG.warn("No config file found for stage: {}", stage);
            }
        } else {
            this.managerConfig = baseConfig;
        }
    }

    public APIManagerConfig getManagerConfig() throws AppException {
        if (this.managerConfig == null) readConfig();
        return this.managerConfig;
    }
}
