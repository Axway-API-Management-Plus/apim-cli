package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.api.API;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.error.AppException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class YamlAPIExporter extends JsonAPIExporter {

    private static final Logger LOG = LoggerFactory.getLogger(YamlAPIExporter.class);

    public YamlAPIExporter(APIExportParams params) {
        super(params);
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        LOG.info("Export API and configuration as Yaml format");
        int size = apis.size();
        List<ExportAPI> exportAPIs = new ArrayList<>(size);
        ObjectMapper mapper = new ObjectMapper(CustomYamlFactory.createYamlFactory());
        for (API api : apis) {
            ExportAPI exportAPI = new ExportAPI(api);
            exportHelper.saveAPILocally(mapper, exportAPI, "/api-config.yaml", size);
            exportAPIs.add(exportAPI);
        }
        if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            exportHelper.writeToConsole(mapper, exportAPIs);
        }
    }
}
