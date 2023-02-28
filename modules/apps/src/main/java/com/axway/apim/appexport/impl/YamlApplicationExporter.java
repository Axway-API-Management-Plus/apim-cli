package com.axway.apim.appexport.impl;

import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.ExportResult;

public class YamlApplicationExporter extends JsonApplicationExporter{
    public YamlApplicationExporter(AppExportParams params, ExportResult result) {
        super(params, result);
    }
}
