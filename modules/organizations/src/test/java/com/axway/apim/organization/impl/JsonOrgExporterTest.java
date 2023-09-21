package com.axway.apim.organization.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.organization.lib.OrgExportCLIOptions;
import com.axway.apim.organization.lib.OrgExportParams;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class JsonOrgExporterTest extends WiremockWrapper {

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
        String[] args = {"-h", "localhost", "-o", "json", "-deleteTarget"};
        OrgExportParams params = (OrgExportParams) OrgExportCLIOptions.create(args).getParams();
        ExportResult result = new ExportResult();
        APIManagerAdapter apimanagerAdapter = APIManagerAdapter.getInstance();
        OrgResultHandler exporter = OrgResultHandler.create(OrgResultHandler.ResultHandler.JSON_EXPORTER, params, result);
        List<Organization> organizations = apimanagerAdapter.getOrgAdapter().getOrgs(exporter.getFilter());
        JsonOrgExporter jsonOrgExporter = new JsonOrgExporter(params, result);
        jsonOrgExporter.export(organizations);
        apimanagerAdapter.deleteInstance();
    }
}
