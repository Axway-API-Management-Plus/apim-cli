package com.axway.apim.api.export.impl;

import com.axway.apim.api.API;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.error.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class YamlAPIExporter extends JsonAPIExporter{

    private static final Logger LOG = LoggerFactory.getLogger(YamlAPIExporter.class);

    public YamlAPIExporter(APIExportParams params) {
        super(params);
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        LOG.info("Export API and configuration as Yaml format");
        for (API api : apis) {
            ExportAPI exportAPI = new ExportAPI(api);
            exportHelper.saveAPILocally(exportAPI, this);
        }
    }
}
