package com.axway.apim.setup.impl;

import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.apim.setup.model.APIManagerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YamlAPIManagerSetupExporter extends JsonAPIManagerSetupExporter{
    private static final Logger LOG = LoggerFactory.getLogger(YamlAPIManagerSetupExporter.class);

    public YamlAPIManagerSetupExporter(APIManagerSetupExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public void export(APIManagerConfig apimanagerConfig) throws AppException {
        LOG.info("Exporting API Manager Configuration in yaml");
        exportToFile(apimanagerConfig, this);
    }
}
