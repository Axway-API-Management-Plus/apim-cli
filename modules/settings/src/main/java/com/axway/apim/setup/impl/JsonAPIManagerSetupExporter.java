package com.axway.apim.setup.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.RemoteHostFilter;
import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.adapter.jackson.PolicySerializerModifier;
import com.axway.apim.adapter.jackson.UserSerializerModifier;
import com.axway.apim.api.model.Config;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.apim.setup.model.APIManagerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class JsonAPIManagerSetupExporter extends APIManagerSetupResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(JsonAPIManagerSetupExporter.class);

    public JsonAPIManagerSetupExporter(APIManagerSetupExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public RemoteHostFilter getRemoteHostFilter() {
        return getRemoteHostBaseFilterBuilder().build();
    }

    @Override
    public void export(APIManagerConfig apimanagerConfig) throws AppException {
        exportToFile(apimanagerConfig, this);
    }


    public void exportToFile(APIManagerConfig apimanagerConfig, APIManagerSetupResultHandler apiManagerSetupResultHandler) throws AppException {
        File localFolder = null;
        ObjectMapper mapper;
        String configFile;
        if (!EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            String folderName = getExportFolder(apimanagerConfig.getConfig());
            String targetFolder = params.getTarget();
            localFolder = new File(targetFolder + File.separator + folderName);
            LOG.info("Going to export API-Manager configuration into folder: {}", localFolder);
            if (localFolder.exists()) {
                if (params.isDeleteTarget()) {
                    LOG.debug("Existing local export folder: {} already exists and will be deleted.", localFolder);
                    try {
                        FileUtils.deleteDirectory(localFolder);
                    } catch (IOException e) {
                        throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
                    }
                } else {
                    LOG.warn("Local export folder: {} already exists. Configuration will not be exported. (You may set -deleteTarget)", localFolder);
                    this.hasError = true;
                    return;
                }
            }
            if (!localFolder.mkdirs()) {
                throw new AppException("Cannot create export folder: " + localFolder, ErrorCode.UNXPECTED_ERROR);
            }
        }

        if (apiManagerSetupResultHandler instanceof YamlAPIManagerSetupExporter) {
            mapper = new ObjectMapper(CustomYamlFactory.createYamlFactory());
            configFile = "/apimanager-config.yaml";
        } else {
            mapper = new ObjectMapper();
            configFile = "/apimanager-config.json";
        }
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.registerModule(new SimpleModule().setSerializerModifier(new PolicySerializerModifier(true)));
            mapper.registerModule(new SimpleModule().setSerializerModifier(new UserSerializerModifier(true)));
            FilterProvider filters = new SimpleFilterProvider()
                .addFilter("RemoteHostFilter", SimpleBeanPropertyFilter.serializeAllExcept("id", "organizationId"))
                .addFilter("APIManagerConfigFilter", SimpleBeanPropertyFilter.serializeAllExcept("os", "architecture", "productVersion", "baseOAuth"))
                .addFilter("QuotaRestrictionFilter", SimpleBeanPropertyFilter.serializeAllExcept("apiId")) // Is handled in ExportApplication
                .setFailOnUnknownId(false);
            mapper.setFilterProvider(filters);
            if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
                mapper.writeValue(System.out, apimanagerConfig);
            } else {
                mapper.writeValue(new File(localFolder.getCanonicalPath() + configFile), apimanagerConfig);
                result.addExportedFile(localFolder.getCanonicalPath() + configFile);
            }
        } catch (Exception e) {
            throw new AppException("Can't create configuration export", ErrorCode.UNXPECTED_ERROR, e);
        }
        LOG.info("Successfully exported API-Manager configuration into: {}{}", localFolder, configFile);
        if (!APIManagerAdapter.getInstance().hasAdminAccount()) {
            LOG.warn("Export has been done with an Org-Admin account only. Export of configuration restricted.");
        }
    }

    private String getExportFolder(Config config) {
        try {
            if (config == null) {
                config = APIManagerAdapter.getInstance().getConfigAdapter().getConfig(APIManagerAdapter.getInstance().hasAdminAccount());
            }
            String name = config.getPortalName().toLowerCase();
            name = name.replace(" ", "-");
            return name;
        } catch (Exception e) {
            LOG.warn("Error defining export folder. Error message: {}", e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.error("Error defining export folder.", e);
            }
            return "";
        }
    }

}
