package com.axway.apim.appexport.impl;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.lib.AppExportCLIOptions;
import com.axway.apim.appexport.lib.AppExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConsoleAppExportTest extends APIManagerMockBase {

	private static final String TEST_PACKAGE = "com/axway/apim/appexport/apps/resultSet/";

	ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public void setTest() throws AppException, IOException {
		setupMockData();
		mapper.disable(MapperFeature.USE_ANNOTATIONS);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	@Test
	public void runStandardConsoleAppExport()
			throws JsonParseException, JsonMappingException, IOException, AppException, ParseException {
		List<ClientApplication> apps = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(
				TEST_PACKAGE + "appsAsOrgAdmin.json"), new TypeReference<List<ClientApplication>>() {
				});

		AppExportParams cmdParams = new AppExportParams(new AppExportCLIOptions(new String[] {}));
		ConsoleAppExporter consoleExp = new ConsoleAppExporter(cmdParams);
		consoleExp.export(apps);
	}

	@Test
	public void runWideConsoleAppExport()
			throws JsonParseException, JsonMappingException, IOException, AppException, ParseException {
		List<ClientApplication> apps = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(
				TEST_PACKAGE + "appsAsOrgAdmin.json"), new TypeReference<List<ClientApplication>>() {
				});

		AppExportParams cmdParams = new AppExportParams(new AppExportCLIOptions(new String[] { "-wide" }));
		ConsoleAppExporter consoleExp = new ConsoleAppExporter(cmdParams);
		consoleExp.export(apps);
	}

	@Test
	public void runUltraConsoleAppExport()
			throws JsonParseException, JsonMappingException, IOException, AppException, ParseException {
		List<ClientApplication> apps = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + "appsAsOrgAdmin.json"), new TypeReference<List<ClientApplication>>() {});

		AppExportParams cmdParams = new AppExportParams(new AppExportCLIOptions(new String[] { "-ultra" }));
		ConsoleAppExporter consoleExp = new ConsoleAppExporter(cmdParams);
		consoleExp.export(apps);
	}
}