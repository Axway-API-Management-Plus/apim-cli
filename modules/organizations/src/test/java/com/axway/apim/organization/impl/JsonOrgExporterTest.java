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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class JsonOrgExporterTest extends WiremockWrapper {

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
    public void tesCVSExport() throws AppException {
        String[] args = {"-h", "localhost"};
        OrgExportParams params = (OrgExportParams) OrgExportCLIOptions.create(args).getParams();
        APIManagerAdapter.deleteInstance();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();


        OrgResultHandler exporter = OrgResultHandler.create(OrgResultHandler.ResultHandler.JSON_EXPORTER, params, result);
        List<Organization> organizations = apimanagerAdapter.orgAdapter.getOrgs(exporter.getFilter());
        JsonOrgExporter jsonOrgExporter = new JsonOrgExporter(params, result);
        jsonOrgExporter.export(organizations);
    }
}
