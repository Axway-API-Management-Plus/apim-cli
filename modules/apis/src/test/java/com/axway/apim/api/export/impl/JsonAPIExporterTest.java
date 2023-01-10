package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.cli.CLIAPIExportOptions;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.utils.TestIndicator;
import com.github.tomakehurst.wiremock.WireMockServer;
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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.testng.Assert.assertEquals;

public class JsonAPIExporterTest {

    private static final Logger LOG = LoggerFactory.getLogger(JsonAPIExporterTest.class);


    WireMockServer wireMockServer;

    @BeforeClass
    public void initWiremock() {
        TestIndicator.getInstance().setTestRunning(true);
        wireMockServer = new WireMockServer(options().httpsPort(8075).usingFilesUnderDirectory(this.getClass().getResource("/").getPath()));
        wireMockServer.start();
        LOG.info("Wiremock server started");
    }

    @AfterClass
    public void close() {
        wireMockServer.stop();
    }

    @Test
    /** https://github.com/Axway-API-Management-Plus/apim-cli/issues/336 **/
    public void testRequestAndResponsePoliciesWithSpecialCharacters() throws IOException {

        String[] args = {"-host", "localhost", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150", "-t", "openapi", "-o", "json", "-deleteTarget"};
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
        DocumentContext documentContext = JsonPath.parse(new File("openapi/api-v3/api-config.json"));
        assertEquals(documentContext.read("$.name", String.class), "petstore3");
        assertEquals(documentContext.read("$.name", String.class), "petstore3");
        assertEquals(documentContext.read("$.outboundProfiles._default.requestPolicy", String.class), "Validate Size & Token");
        assertEquals(documentContext.read("$.outboundProfiles._default.responsePolicy", String.class), "Remove Header & Audit data");

    }
}
