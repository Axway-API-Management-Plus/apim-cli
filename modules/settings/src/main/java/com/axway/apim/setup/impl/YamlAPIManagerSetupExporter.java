package com.axway.apim.setup.impl;

import com.axway.apim.adapter.apis.RemoteHostFilter;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YamlAPIManagerSetupExporter extends JsonAPIManagerSetupExporter{
    protected static Logger LOG = LoggerFactory.getLogger(YamlAPIManagerSetupExporter.class);

    public YamlAPIManagerSetupExporter(APIManagerSetupExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public RemoteHostFilter getRemoteHostFilter() {
        return getRemoteHostBaseFilterBuilder().build();
    }

    public void export(APIManagerConfig apimanagerConfig) throws AppException {
        LOG.info("Exporting API Manager Configuration in yaml");
        exportToFile(apimanagerConfig, this);
    }
}
