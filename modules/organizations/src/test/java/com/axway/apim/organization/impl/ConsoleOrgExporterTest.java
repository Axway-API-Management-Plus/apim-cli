package com.axway.apim.organization.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.lib.OrgExportCLIOptions;
import com.axway.apim.organization.lib.OrgExportParams;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class ConsoleOrgExporterTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void testConsoleExport() throws AppException {
        String[] args = {"-h", "localhost"};
        OrgExportParams params = (OrgExportParams) OrgExportCLIOptions.create(args).getParams();
        APIManagerAdapter.deleteInstance();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        OrgResultHandler exporter = OrgResultHandler.create(OrgResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<Organization> organizations = apimanagerAdapter.orgAdapter.getOrgs(exporter.getFilter());
        ConsoleOrgExporter consoleOrgExporter = new ConsoleOrgExporter(params, result);
        consoleOrgExporter.export(organizations);
    }

    @Test
    public void tesCVSExportWide() throws AppException {
        String[] args = {"-h", "localhost", "-wide"};
        OrgExportParams params = (OrgExportParams) OrgExportCLIOptions.create(args).getParams();
        APIManagerAdapter.deleteInstance();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        OrgResultHandler exporter = OrgResultHandler.create(OrgResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<Organization> organizations = apimanagerAdapter.orgAdapter.getOrgs(exporter.getFilter());
        ConsoleOrgExporter consoleOrgExporter = new ConsoleOrgExporter(params, result);
        consoleOrgExporter.export(organizations);
    }

    @Test
    public void tesCVSExportUltra() throws AppException {
        String[] args = {"-h", "localhost", "-ultra"};
        OrgExportParams params = (OrgExportParams) OrgExportCLIOptions.create(args).getParams();
        APIManagerAdapter.deleteInstance();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        OrgResultHandler exporter = OrgResultHandler.create(OrgResultHandler.ResultHandler.CONSOLE_EXPORTER, params, result);
        List<Organization> organizations = apimanagerAdapter.orgAdapter.getOrgs(exporter.getFilter());
        ConsoleOrgExporter consoleOrgExporter = new ConsoleOrgExporter(params, result);
        consoleOrgExporter.export(organizations);
    }
}
