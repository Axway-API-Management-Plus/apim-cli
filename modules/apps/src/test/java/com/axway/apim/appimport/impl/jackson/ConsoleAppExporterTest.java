package com.axway.apim.appimport.impl.jackson;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.impl.ApplicationExporter;
import com.axway.apim.appexport.impl.ConsoleAppExporter;
import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class ConsoleAppExporterTest extends WiremockWrapper {

    private APIManagerAdapter apimanagerAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();
            APIManagerAdapter.deleteInstance();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apimanagerAdapter = APIManagerAdapter.getInstance();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }
    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void tesConsoleExport() throws AppException {
        String[] args = {};
        AppExportParams params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        ApplicationExporter exporter = ApplicationExporter.create(ApplicationExporter.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<ClientApplication> apps = apimanagerAdapter.appAdapter.getApplications(exporter.getFilter(), true);
        ConsoleAppExporter consoleAppExporter = new ConsoleAppExporter(params, result);
        consoleAppExporter.export(apps);
    }

    @Test
    public void tesConsoleExportWide() throws AppException {
        String[] args = {"-wide"};
        AppExportParams params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        ApplicationExporter exporter = ApplicationExporter.create(ApplicationExporter.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<ClientApplication> apps = apimanagerAdapter.appAdapter.getApplications(exporter.getFilter(), true);
        ConsoleAppExporter consoleAppExporter = new ConsoleAppExporter(params, result);
        consoleAppExporter.export(apps);
    }

    @Test
    public void tesConsoleExportUltra() throws AppException {
        String[] args = {"-ultra"};
        AppExportParams params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        ApplicationExporter exporter = ApplicationExporter.create(ApplicationExporter.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<ClientApplication> apps = apimanagerAdapter.appAdapter.getApplications(exporter.getFilter(), true);
        ConsoleAppExporter consoleAppExporter = new ConsoleAppExporter(params, result);
        consoleAppExporter.export(apps);
    }
}