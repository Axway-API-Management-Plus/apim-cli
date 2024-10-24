package com.axway.apim.appexport.impl;

import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.appexport.model.ExportApplication;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class YamlApplicationExporter extends JsonApplicationExporter {


    public YamlApplicationExporter(AppExportParams params, ExportResult result) {
        super(params, result);
    }

    @Override
    public void export(List<ClientApplication> apps) throws AppException {
        for (ClientApplication app : apps) {
            saveApplicationLocally(new ObjectMapper(CustomYamlFactory.createYamlFactory()), new ExportApplication(app), "/application-config.yaml");
        }
    }

}
