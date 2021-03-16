package com.axway.apim.export.test.basic;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.export.test.ExportTestAction;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Test
public class SimpleAPIExportTestIT extends TestNGCitrusTestRunner {

	private ExportTestAction swaggerExport;
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException {		
		ObjectMapper mapper = new ObjectMapper();

		swaggerExport = new ExportTestAction();
		swaggerImport = new ImportTestAction();
		description("Import an API to export it afterwards");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/api/test/"+this.getClass().getSimpleName()+"-${apiNumber}");
		variable("apiName", this.getClass().getSimpleName()+"-${apiNumber}");
		variable("state", "unpublished");
		variable("exportLocation", "citrus:systemProperty('java.io.tmpdir')");
		variable(ExportTestAction.EXPORT_API,  "${apiPath}");
		
		// These are the folder and filenames generated by the export tool 
		variable("exportFolder", "api-test-${apiName}");
		variable("exportAPIName", "${apiName}.json");

		echo("####### Importing the API, which should exported in the second step #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/test/export/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/test/export/files/basic/minimal-config.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the API has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Manually configure the backend to https://yet.another.petstore/another/path --> https://yet.another.petstore/another/path/v2 #######");
		// Get the API-Proxy object
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON).extractFromPayload("$", "apiProxy"));
		// Manually set another backend host and path
		JsonNode config = mapper.readTree(context.getVariable("apiProxy"));
		((ObjectNode) config.get("serviceProfiles").get("_default")).put("basePath", "https://yet.another.petstore/another/path");
		variable("updatedAPIProxy", mapper.writeValueAsString(config));
		// Update the API-Proxy
		http(builder -> builder.client("apiManager").send().put("/proxies/${apiId}").header("Content-Type", "application/json")
				.payload("${updatedAPIProxy}"));

		echo("####### Export the API from the API-Manager using useFEAPIDefinition #######");
		createVariable("expectedReturnCode", "0");
		createVariable("useFEAPIDefinition", "true"); // In this case we simulate to export the FE-API-Definition instead of the backend
		swaggerExport.doExecute(context);
		
		String exportedAPIConfigFile = context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/api-config.json";
		
		echo("####### Reading exported API-Config file: '"+exportedAPIConfigFile+"' #######");
		JsonNode exportedAPIConfig = mapper.readTree(new FileInputStream(new File(exportedAPIConfigFile)));
		
		assertEquals(exportedAPIConfig.get("version").asText(), 			"2.0.0");
		assertEquals(exportedAPIConfig.get("organization").asText(),		"API Development "+context.getVariable("orgNumber"));
		assertEquals(exportedAPIConfig.get("state").asText(), 				"unpublished");
		assertEquals(exportedAPIConfig.get("path").asText(), 				context.getVariable("apiPath"));
		assertEquals(exportedAPIConfig.get("name").asText(), 				context.getVariable("apiName"));
		assertEquals(exportedAPIConfig.get("backendBasepath").asText(), 	"https://yet.another.petstore/another/path/v2");
		assertEquals(exportedAPIConfig.get("caCerts").size(), 				4);
		
		assertEquals(exportedAPIConfig.get("caCerts").get(0).get("certFile").asText(), 				"swagger.io.crt");
		assertEquals(exportedAPIConfig.get("caCerts").get(0).get("inbound").asBoolean(), 			false);
		assertEquals(exportedAPIConfig.get("caCerts").get(0).get("outbound").asBoolean(), 			true);
		
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/swagger.io.crt").exists(), "Certificate swagger.io.crt is missing");
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/StarfieldServicesRootCertificateAuthority-G2.crt").exists(), "Certificate StarfieldServicesRootCertificateAuthority-G2.crt is missing");
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/AmazonRootCA1.crt").exists(), "Certificate AmazonRootCA1.crt is missing");
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/Amazon.crt").exists(), "Certificate Amazon.crt is missing");
		
		File exportedAPISpecFile = new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/"+context.getVariable("exportAPIName"));
		assertTrue(exportedAPISpecFile.exists(), "Exported API-Specification is missing");
		
		// Read the export Swagger-File
		JsonNode exportedAPISpec = mapper.readTree(new FileInputStream(exportedAPISpecFile));
		// Check the original basePath is set (See issue https://github.com/Axway-API-Management-Plus/apim-cli/issues/158)
		assertEquals(exportedAPISpec.get("basePath").asText(), 			"/v2");
		assertEquals(exportedAPISpec.get("host").asText(), 				"petstore.swagger.io");
		assertEquals(exportedAPISpec.get("schemes").get(0).asText(), 	"https");
		
	}
}
