package com.axway.apim.export.test.impl;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.api.API;
import com.axway.apim.api.export.impl.ConsoleAPIExporter;
import com.axway.apim.api.export.lib.cli.CLIAPIExportOptions;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConsoleAPIExportTest extends APIManagerMockBase {
	
	private static final String TEST_PACKAGE = "test/export/files/apiLists/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	
	@BeforeClass
	public void setTest() throws AppException, IOException {
		setupMockData();
		mapper.disable(MapperFeature.USE_ANNOTATIONS);
	}
	
	@Test
	public void runStandardConsoleAPIExport() throws JsonParseException, JsonMappingException, IOException, AppException, ParseException  {
		List<API> apis = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + "three-apis-no-clientOrgs-and-clientApps.json"), new TypeReference<List<API>>(){});
		
		APIExportParams cmdParams = (APIExportParams) CLIAPIExportOptions.create(new String[] {}).getParams();
		ConsoleAPIExporter consoleExp = new ConsoleAPIExporter(cmdParams);
		consoleExp.execute(apis);
	}
	
	@Test
	public void runWideConsoleAPIExport() throws JsonParseException, JsonMappingException, IOException, AppException, ParseException  {
		List<API> apis = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + "three-apis-no-clientOrgs-and-clientApps.json"), new TypeReference<List<API>>(){});
		
		APIExportParams cmdParams = (APIExportParams) CLIAPIExportOptions.create(new String[] {"-wide"}).getParams();
		ConsoleAPIExporter consoleExp = new ConsoleAPIExporter(cmdParams);
		consoleExp.execute(apis);
	}
	
	@Test
	public void runUltraConsoleAPIExport() throws JsonParseException, JsonMappingException, IOException, AppException, ParseException  {
		List<API> apis = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + "three-apis-no-clientOrgs-and-clientApps.json"), new TypeReference<List<API>>(){});
		
		APIExportParams cmdParams = (APIExportParams) CLIAPIExportOptions.create(new String[] {"-ultra"}).getParams();
		ConsoleAPIExporter consoleExp = new ConsoleAPIExporter(cmdParams);
		consoleExp.execute(apis);
	}
}