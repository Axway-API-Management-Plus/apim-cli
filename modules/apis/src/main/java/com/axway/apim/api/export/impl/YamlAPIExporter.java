package com.axway.apim.api.export.impl;

import com.axway.apim.api.API;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;
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
        for (API api : apis) {
            ExportAPI exportAPI = new ExportAPI(api);
            try {
                saveAPILocally(exportAPI, this);
            } catch (AppException e) {
                LOG.error("Error in export", e);
                throw e;
            }
        }
    }
}
