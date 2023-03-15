package com.axway.apim.setup.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.RemoteHostFilter;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.setup.lib.APIManagerSetupExportCLIOptions;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.apim.setup.model.APIManagerConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ConsolePrinterTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void testConsoleExportConfigStandard() throws AppException {
        String[] args = {"-h", "localhost", "-c", "manager-config.json", "-type", "config"};
        APIManagerSetupExportParams params = (APIManagerSetupExportParams) APIManagerSetupExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        APIManagerSetupResultHandler exporter = APIManagerSetupResultHandler.create(APIManagerSetupResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        APIManagerConfig apiManagerConfig = new APIManagerConfig();
        apiManagerConfig.setConfig(apimanagerAdapter.configAdapter.getConfig(APIManagerAdapter.hasAdminAccount()));
        exporter.export(apiManagerConfig);
    }

    @Test
    public void testConsoleExportAlertsStandard() throws AppException {
        String[] args = {"-h", "localhost", "-c", "manager-config.json", "-type", "alerts"};
        APIManagerSetupExportParams params = (APIManagerSetupExportParams) APIManagerSetupExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        APIManagerSetupResultHandler exporter = APIManagerSetupResultHandler.create(APIManagerSetupResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        APIManagerConfig apiManagerConfig = new APIManagerConfig();
        apiManagerConfig.setAlerts(apimanagerAdapter.alertsAdapter.getAlerts());
        exporter.export(apiManagerConfig);
    }

    @Test
    public void testConsoleExportRemoteHostsStandard() throws AppException {
        String[] args = {"-h", "localhost", "-c", "manager-config.json", "-type", "remotehosts"};
        APIManagerSetupExportParams params = (APIManagerSetupExportParams) APIManagerSetupExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        APIManagerSetupResultHandler exporter = APIManagerSetupResultHandler.create(APIManagerSetupResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        APIManagerConfig apiManagerConfig = new APIManagerConfig();
        apiManagerConfig.setRemoteHosts(apimanagerAdapter.remoteHostsAdapter.getRemoteHosts(new RemoteHostFilter.Builder().build()));
        exporter.export(apiManagerConfig);
    }

    @Test
    public void testConsoleExportRemoteHostsWide() throws AppException {
        String[] args = {"-h", "localhost", "-c", "manager-config.json", "-type", "remotehosts","-wide"};
        APIManagerSetupExportParams params = (APIManagerSetupExportParams) APIManagerSetupExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        APIManagerSetupResultHandler exporter = APIManagerSetupResultHandler.create(APIManagerSetupResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        APIManagerConfig apiManagerConfig = new APIManagerConfig();
        apiManagerConfig.setRemoteHosts(apimanagerAdapter.remoteHostsAdapter.getRemoteHosts(new RemoteHostFilter.Builder().build()));
        exporter.export(apiManagerConfig);
    }

    @Test
    public void testConsoleExportRemoteHostsUltra() throws AppException {
        String[] args = {"-h", "localhost", "-c", "manager-config.json", "-type", "remotehosts","-ultra"};
        APIManagerSetupExportParams params = (APIManagerSetupExportParams) APIManagerSetupExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        APIManagerSetupResultHandler exporter = APIManagerSetupResultHandler.create(APIManagerSetupResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        APIManagerConfig apiManagerConfig = new APIManagerConfig();
        apiManagerConfig.setRemoteHosts(apimanagerAdapter.remoteHostsAdapter.getRemoteHosts(new RemoteHostFilter.Builder().build()));
        exporter.export(apiManagerConfig);
    }


    @Test
    public void testConsoleExportPolicesStandard() throws AppException {
        String[] args = {"-h", "localhost", "-c", "manager-config.json", "-type", "policies"};
        APIManagerSetupExportParams params = (APIManagerSetupExportParams) APIManagerSetupExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        APIManagerSetupResultHandler exporter = APIManagerSetupResultHandler.create(APIManagerSetupResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        APIManagerConfig apiManagerConfig = new APIManagerConfig();
        exporter.export(apiManagerConfig);
    }
}
