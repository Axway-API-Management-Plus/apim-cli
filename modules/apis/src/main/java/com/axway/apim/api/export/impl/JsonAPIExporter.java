package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
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

public class JsonAPIExporter extends APIResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(JsonAPIExporter.class);

    /**
     * Where to store the exported API-Definition
     */

    public JsonAPIExporter(APIExportParams params) {
        super(params);
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        LOG.info("Exporting API and configuration as JSON format");
        int size = apis.size();
        List<ExportAPI> exportAPIs = new ArrayList<>(size);
        ObjectMapper mapper = new ObjectMapper();
        for (API api : apis) {
            ExportAPI exportAPI = new ExportAPI(api);
            exportHelper.saveAPILocally(mapper, exportAPI, "/api-config.json", size);
            exportAPIs.add(exportAPI);
        }
        if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            exportHelper.writeToConsole(mapper, exportAPIs);
        }
    }


    @Override
    public APIFilter getFilter() {
        Builder builder = getBaseAPIFilterBuilder()
            .includeQuotas(true)
            .includeImage(true)
            .includeClientApplications(true)
            .includeClientOrganizations(true)
            .includeOriginalAPIDefinition(true)
            .includeRemoteHost(true);
        if (params.isExportMethods())
            builder.includeMethods(true);
        if (params.getState() != null && params.getState().equalsIgnoreCase("deprecated")) {
            builder.isDeprecated(true);
            builder.hasState(null);
        }
        if (params.getState() != null && params.getState().equalsIgnoreCase("retired")) {
            builder.isRetired(true);
            builder.hasState(null);
        }
        return builder.build();
    }
}
