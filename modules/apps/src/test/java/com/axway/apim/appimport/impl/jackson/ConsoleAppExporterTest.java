package com.axway.apim.appimport.impl.jackson;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.impl.ApplicationExporter;
import com.axway.apim.appexport.impl.CSVAppExporter;
import com.axway.apim.appexport.impl.ConsoleAppExporter;
import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class ConsoleAppExporterTest extends WiremockWrapper {

    @BeforeClass
    public void init() throws URISyntaxException {
        URI version = this.getClass().getClassLoader().getResource("wiremock_apim").toURI();
        System.out.println(version);
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void tesConsoleExport() throws AppException {
        String[] args = {"-h", "localhost"};
        AppExportParams params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
        APIManagerAdapter.deleteInstance();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        ApplicationExporter exporter = ApplicationExporter.create(ApplicationExporter.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<ClientApplication> apps = apimanagerAdapter.appAdapter.getApplications(exporter.getFilter(), true);
        ConsoleAppExporter consoleAppExporter = new ConsoleAppExporter(params, result);
        consoleAppExporter.export(apps);
    }
}