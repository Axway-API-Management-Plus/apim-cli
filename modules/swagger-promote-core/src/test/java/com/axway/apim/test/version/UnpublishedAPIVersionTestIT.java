package com.axway.apim.test.version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="UnpublishedAPIVersionTest")
public class UnpublishedAPIVersionTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "UnpublishedAPIVersionTest")
	public void run() {
		description("Validate that API-Version is updated");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/version-test-${apiNumber}");
		variable("apiName", "Version-test-${apiNumber}");
		

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/version/1_flexible_version_and_state.json");
		createVariable("version", "1.0.0");
		createVariable("state", "unpublished");
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
			.validate("$.[?(@.path=='${apiPath}')].version", "${version}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Perform a no-change #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/version/1_flexible_version_and_state.json");
		createVariable("version", "1.0.0");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
		
		echo("####### Change the API-Version for the Unpublished API #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/version/1_flexible_version_and_state.json");
		createVariable("version", "1.0.1");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate that the same API (as it's still unpublished) has been updated #######");
		http().client("apiManager")
			.send()
			.get("/proxies/${apiId}")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
		.send()
		.get("/proxies")
		.name("api")
		.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "unpublished")
			.validate("$.[?(@.id=='${apiId}')].version", "${version}");
	}
}
