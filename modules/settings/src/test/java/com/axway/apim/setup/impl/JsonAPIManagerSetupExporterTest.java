package com.axway.apim.setup.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.setup.lib.APIManagerSetupExportCLIOptions;
import com.axway.apim.setup.lib.APIManagerSetupExportParams;
import com.axway.apim.setup.model.APIManagerConfig;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JsonAPIManagerSetupExporterTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void testJsonExport() throws AppException {
        String[] args = {"-h", "localhost", "-c", "manager-config.json", "-o", "json"};
        APIManagerSetupExportParams params = (APIManagerSetupExportParams) APIManagerSetupExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        APIManagerSetupResultHandler exporter = APIManagerSetupResultHandler.create(APIManagerSetupResultHandler.ResultHandler.JSON_EXPORTER, params, result);
        APIManagerConfig apiManagerConfig = new APIManagerConfig();
        apiManagerConfig.setConfig(apimanagerAdapter.configAdapter.getConfig(APIManagerAdapter.hasAdminAccount()));
        exporter.export(apiManagerConfig);
    }
}
