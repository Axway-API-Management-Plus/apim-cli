package com.axway.apim.appimport.impl.jackson;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.impl.ApplicationExporter;
import com.axway.apim.appexport.impl.YamlApplicationExporter;
import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class YamlApplicationExporterTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void testYamlExport() throws AppException {
        String[] args = {"-h", "localhost", "-deleteTarget"};
        AppExportParams params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
        APIManagerAdapter.deleteInstance();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        ApplicationExporter exporter = ApplicationExporter.create(ApplicationExporter.ResultHandler.YAML_EXPORTER, params, result);
        List<ClientApplication> apps = apimanagerAdapter.appAdapter.getApplications(exporter.getFilter(), true);
        YamlApplicationExporter yamlApplicationExporter = new YamlApplicationExporter(params, result);
        yamlApplicationExporter.export(apps);
    }
}
