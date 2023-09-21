package com.axway.apim.api.export.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.cli.CLIAPIExportOptions;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class ConsoleAPIExporterTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void exportConsole() throws AppException {
        String[] args = {"-host", "localhost", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150", "-t", "openapi"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        ConsoleAPIExporter consoleAPIExporter = new ConsoleAPIExporter(params);
        APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.getApiAdapter();
        API api = apiManagerAPIAdapter.getAPI(new APIFilter.Builder().hasId(params.getId()).includeOriginalAPIDefinition(true).build(), true);
        api.setApplications(new ArrayList<>());
        api.setClientOrganizations(new ArrayList<>());
        List<API> apis = new ArrayList<>();
        apis.add(api);
        consoleAPIExporter.execute(apis);
        apiManagerAdapter.deleteInstance();

    }

    @Test
    public void exportConsoleWide() throws AppException {
        String[] args = {"-host", "localhost", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150", "-t", "openapi", "-wide"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        ConsoleAPIExporter consoleAPIExporter = new ConsoleAPIExporter(params);
        APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.getApiAdapter();
        API api = apiManagerAPIAdapter.getAPI(new APIFilter.Builder().hasId(params.getId()).includeOriginalAPIDefinition(true).build(), true);
        api.setApplications(new ArrayList<>());
        api.setClientOrganizations(new ArrayList<>());
        List<API> apis = new ArrayList<>();
        apis.add(api);
        consoleAPIExporter.execute(apis);
        apiManagerAdapter.deleteInstance();

    }

    @Test
    public void exportConsoleUltra() throws AppException {
        String[] args = {"-host", "localhost", "-id", "e4ded8c8-0a40-4b50-bc13-552fb7209150", "-t", "openapi", "-ultra"};
        CLIOptions options = CLIAPIExportOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        ConsoleAPIExporter consoleAPIExporter = new ConsoleAPIExporter(params);
        APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.getApiAdapter();
        API api = apiManagerAPIAdapter.getAPI(new APIFilter.Builder().hasId(params.getId()).includeOriginalAPIDefinition(true).build(), true);
        api.setApplications(new ArrayList<>());
        api.setClientOrganizations(new ArrayList<>());
        List<API> apis = new ArrayList<>();
        apis.add(api);
        consoleAPIExporter.execute(apis);
        apiManagerAdapter.deleteInstance();

    }
}
