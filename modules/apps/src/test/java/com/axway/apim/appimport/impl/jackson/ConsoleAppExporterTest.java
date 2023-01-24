package com.axway.apim.appimport.impl.jackson;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.impl.ApplicationExporter;
import com.axway.apim.appexport.impl.ConsoleAppExporter;
import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class ConsoleAppExporterTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void tesConsoleExport() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
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

    @Test
    public void tesConsoleExportWide() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        String[] args = {"-h", "localhost", "-wide"};
        AppExportParams params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
        APIManagerAdapter.deleteInstance();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        ApplicationExporter exporter = ApplicationExporter.create(ApplicationExporter.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<ClientApplication> apps = apimanagerAdapter.appAdapter.getApplications(exporter.getFilter(), true);
        ConsoleAppExporter consoleAppExporter = new ConsoleAppExporter(params, result);
        consoleAppExporter.export(apps);
    }

    @Test
    public void tesConsoleExportUltra() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        String[] args = {"-h", "localhost", "-ultra"};
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