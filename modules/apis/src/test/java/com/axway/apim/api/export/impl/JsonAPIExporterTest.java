package com.axway.apim.api.export.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.cli.CLIAPIExportOptions;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CLIOptions;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class JsonAPIExporterTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }
    private static final Logger LOG = LoggerFactory.getLogger(JsonAPIExporterTest.class);

    @Test
    public void testRequestAndResponsePoliciesWithSpecialCharacters() throws IOException {
        // https://github.com/Axway-API-Management-Plus/apim-cli/issues/336
        String tmpDir = System.getProperty("java.io.tmpdir") + "/openapi";
        LOG.info("Test testRequestAndResponsePoliciesWithSpecialCharacters");
        String[] args = {"-host", "localhost", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150", "-t", tmpDir, "-o", "json", "-deleteTarget"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        APIManagerAdapter.deleteInstance();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        JsonAPIExporter jsonAPIExporter = new JsonAPIExporter(params);
        APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
        API api = apiManagerAPIAdapter.getAPI(new APIFilter.Builder().hasId(params.getId()).includeOriginalAPIDefinition(true).build(), true);
        api.setApplications(new ArrayList<>());
        api.setClientOrganizations(new ArrayList<>());
        List<API> apis = new ArrayList<>();
        apis.add(api);
        jsonAPIExporter.execute(apis);
        DocumentContext documentContext = JsonPath.parse(new File(tmpDir + "/api-v3/api-config.json"));
        assertEquals(documentContext.read("$.name", String.class), "petstore3");
        assertEquals(documentContext.read("$.outboundProfiles._default.requestPolicy", String.class), "Validate Size & Token");
        assertEquals(documentContext.read("$.outboundProfiles._default.responsePolicy", String.class), "Remove Header & Audit data");
    }
}
