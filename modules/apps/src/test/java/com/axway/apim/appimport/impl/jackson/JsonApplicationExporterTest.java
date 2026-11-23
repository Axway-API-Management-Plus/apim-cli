package com.axway.apim.appimport.impl.jackson;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.impl.ApplicationExporter;
import com.axway.apim.appexport.impl.JsonApplicationExporter;
import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class JsonApplicationExporterTest extends WiremockWrapper {

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
        String[] args = {"-h", "localhost", "-deleteTarget"};
        AppExportParams params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        ApplicationExporter exporter = ApplicationExporter.create(ApplicationExporter.ResultHandler.JSON_EXPORTER, params, result);
        List<ClientApplication> apps = apimanagerAdapter.getAppAdapter().getApplications(exporter.getFilter(), true);
        JsonApplicationExporter jsonApplicationExporter = new JsonApplicationExporter(params, result);
        jsonApplicationExporter.export(apps);
        apimanagerAdapter.deleteInstance();

    }

    @Test
    public void testExportAppsWithSameNameGetUniqueFolders() throws Exception {
        java.nio.file.Path target = java.nio.file.Files.createTempDirectory("app-export");
        try {
            String[] args = {"-h", "localhost", "-t", target.toString(), "-deleteTarget"};
            AppExportParams params = (AppExportParams) AppExportCLIOptions.create(args).getParams();
            ExportResult result = new ExportResult();
            APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
            ApplicationExporter exporter = ApplicationExporter.create(ApplicationExporter.ResultHandler.JSON_EXPORTER, params, result);
            List<ClientApplication> apps = apimanagerAdapter.getAppAdapter().getApplications(exporter.getFilter(), true);
            Assert.assertFalse(apps.isEmpty());
            // Export every application twice within the same run: the second copy simulates a same-named application
            List<ClientApplication> duplicated = new java.util.ArrayList<>(apps);
            duplicated.addAll(apps);
            exporter.export(duplicated);
            apimanagerAdapter.deleteInstance();

            java.io.File[] folders = target.toFile().listFiles(java.io.File::isDirectory);
            Assert.assertNotNull(folders);
            Assert.assertEquals(folders.length, duplicated.size(), "Every application must be exported into its own folder");
            for (java.io.File folder : folders) {
                Assert.assertTrue(new java.io.File(folder, "application-config.json").exists(), "Missing config in " + folder);
            }
        } finally {
            com.axway.apim.lib.utils.Utils.deleteDirectory(target.toFile());
        }
    }
}
