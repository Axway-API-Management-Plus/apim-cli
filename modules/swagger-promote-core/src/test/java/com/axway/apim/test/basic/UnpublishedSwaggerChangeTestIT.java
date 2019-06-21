package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="UnpublishedSwaggerChangeTestIT")
public class UnpublishedSwaggerChangeTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "UnpublishedSwaggerChangeTestIT")
	public void run() {

		echo("####### Validates an updated Swagger-File is replicated while in 'Unpublished' state #######");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/swagger-is-changed-${apiNumber}");
		variable("apiName", "Swagger-Is-Changed-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("status", "unpublished");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);

		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId") // Remember the API-ID --> This is the FE-API
			.extractFromPayload("$.[?(@.path=='${apiPath}')].apiId", "beApiId"); // This is the BE-API

		echo("####### Importing a new Swagger-File as a change #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("status", "unpublished");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate the API has changed to the updated Swagger-Definition #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"); // We have a new API-ID
		
		echo("####### Validate the updated Swagger-File has been imported #######");
		http().client("apiManager")
			.send()
			.get("/discovery/swagger/api/id/${newApiId}")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.description", "@assertThat(containsString('THIS IS MY NEW API-DESCRIPTION!'))@");
		
		echo("####### Validate the previous FE-API has been deleted #######");
		http().client("apiManager")
			.send()
			.get("/proxies/${apiId}")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.FORBIDDEN);
		
		echo("####### Validate the previous BE-API has been deleted #######");
		http().client("apiManager")
			.send()
			.get("/apirepo/${beApiId}")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.FORBIDDEN);
	}
}
