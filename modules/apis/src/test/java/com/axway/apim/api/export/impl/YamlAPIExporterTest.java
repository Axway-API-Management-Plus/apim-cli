package com.axway.apim.api.export.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.cli.CLIAPIExportOptions;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CLIOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class YamlAPIExporterTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void testYamlApiConfigExport() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir") + "/openapi";
        String[] args = {"-host", "localhost", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150", "-t", tmpDir, "-o", "yaml", "-deleteTarget"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        YamlAPIExporter yamlAPIExporter = new YamlAPIExporter(params);
        APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.getApiAdapter();
        API api = apiManagerAPIAdapter.getAPI(new APIFilter.Builder().hasId(params.getId()).includeOriginalAPIDefinition(true).build(), true);
        api.setApplications(new ArrayList<>());
        api.setClientOrganizations(new ArrayList<>());
        List<API> apis = new ArrayList<>();
        apis.add(api);
        yamlAPIExporter.execute(apis);
        ObjectMapper objectMapper = new ObjectMapper(CustomYamlFactory.createYamlFactory());
        JsonNode jsonNode = objectMapper.readTree(new File(tmpDir + "/api-v3/api-config.yaml"));
        ObjectMapper objectMapperJson = new ObjectMapper();
        DocumentContext documentContext = JsonPath.parse(objectMapperJson.writeValueAsString(jsonNode));
        assertEquals(documentContext.read("$.name", String.class), "petstore3");
        assertEquals(documentContext.read("$.outboundProfiles._default.requestPolicy", String.class), "Validate Size & Token");
        assertEquals(documentContext.read("$.outboundProfiles._default.responsePolicy", String.class), "Remove Header & Audit data");
        apiManagerAdapter.deleteInstance();

    }
}
