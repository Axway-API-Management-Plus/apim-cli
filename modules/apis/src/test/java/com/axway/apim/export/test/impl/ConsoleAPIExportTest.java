package com.axway.apim.export.test.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.export.impl.ConsoleAPIExporter;
import com.axway.apim.api.export.lib.cli.CLIAPIExportOptions;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class ConsoleAPIExportTest  {
	
	private static final String TEST_PACKAGE = "test/export/files/apiLists/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	
	@BeforeClass
	public void setTest() {
		mapper.disable(MapperFeature.USE_ANNOTATIONS);
	}
	
	@Test
	public void runStandardConsoleAPIExport() throws  IOException  {
		List<API> apis = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + "three-apis-no-clientOrgs-and-clientApps.json"), new TypeReference<>() {
		});
		APIExportParams cmdParams = (APIExportParams) CLIAPIExportOptions.create(new String[] {}).getParams();
		ConsoleAPIExporter consoleExp = new ConsoleAPIExporter(cmdParams);
		consoleExp.execute(apis);
	}
	
	@Test
	public void runWideConsoleAPIExport() throws  IOException  {
		List<API> apis = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + "three-apis-no-clientOrgs-and-clientApps.json"), new TypeReference<>() {
		});
		
		APIExportParams cmdParams = (APIExportParams) CLIAPIExportOptions.create(new String[] {"-wide"}).getParams();
		ConsoleAPIExporter consoleExp = new ConsoleAPIExporter(cmdParams);
		consoleExp.execute(apis);
	}
	
	@Test
	public void runUltraConsoleAPIExport() throws  IOException  {
		List<API> apis = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + "three-apis-no-clientOrgs-and-clientApps.json"), new TypeReference<>() {
		});
		
		APIExportParams cmdParams = (APIExportParams) CLIAPIExportOptions.create(new String[] {"-ultra"}).getParams();
		ConsoleAPIExporter consoleExp = new ConsoleAPIExporter(cmdParams);
		consoleExp.execute(apis);
	}
}