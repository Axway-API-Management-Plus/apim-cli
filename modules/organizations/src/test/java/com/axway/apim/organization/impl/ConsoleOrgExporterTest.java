package com.axway.apim.organization.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.organization.lib.OrgExportCLIOptions;
import com.axway.apim.organization.lib.OrgExportParams;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class ConsoleOrgExporterTest extends WiremockWrapper {

    private APIManagerAdapter apimanagerAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();
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
        Utils.deleteInstance(apimanagerAdapter);
        close();
    }

    @Test
    public void testConsoleExport() throws AppException {
        String[] args = {};
        OrgExportParams params = (OrgExportParams) OrgExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        OrgResultHandler exporter = OrgResultHandler.create(OrgResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<Organization> organizations = apimanagerAdapter.getOrgAdapter().getOrgs(exporter.getFilter());
        ConsoleOrgExporter consoleOrgExporter = new ConsoleOrgExporter(params, result);
        consoleOrgExporter.export(organizations);
    }

    @Test
    public void tesCVSExportWide() throws AppException {
        String[] args = {"localhost", "-wide"};
        OrgExportParams params = (OrgExportParams) OrgExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        OrgResultHandler exporter = OrgResultHandler.create(OrgResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<Organization> organizations = apimanagerAdapter.getOrgAdapter().getOrgs(exporter.getFilter());
        ConsoleOrgExporter consoleOrgExporter = new ConsoleOrgExporter(params, result);
        consoleOrgExporter.export(organizations);
    }

    @Test
    public void tesCVSExportUltra() throws AppException {
        String[] args = {"-ultra"};
        OrgExportParams params = (OrgExportParams) OrgExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        OrgResultHandler exporter = OrgResultHandler.create(OrgResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<Organization> organizations = apimanagerAdapter.getOrgAdapter().getOrgs(exporter.getFilter());
        ConsoleOrgExporter consoleOrgExporter = new ConsoleOrgExporter(params, result);
        consoleOrgExporter.export(organizations);
    }
}
