package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.api.API;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.error.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonAPIExporter extends APIResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(JsonAPIExporter.class);

    /**
     * Where to store the exported API-Definition
     */
    private final boolean exportMethods;

    public JsonAPIExporter(APIExportParams params) {
        super(params);
        this.exportMethods = params.isExportMethods();
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        LOG.info("Exporting API and configuration as JSON format");
        for (API api : apis) {
            ExportAPI exportAPI = new ExportAPI(api);
            exportHelper.saveAPILocally(exportAPI, this);
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
        if (exportMethods)
            builder.includeMethods(true);
        return builder.build();
    }
}
