package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(dependsOnGroups = {"init-tests"})
public class ImportUnpublishedSetToPublishedAPITest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "Import an Unpublished-API and publish")
	public void setupDevOrgTest() {
		description("Import an Unpublished-API and in the second step publish it");
		
		variable("apiNnumber", RandomNumberFunction.getRandomNumber(2, true));
		variable("apiPath", "/my-test-api-${apiNnumber}");
		variable("apiName", "My-Test-API-${apiNnumber}");

		
		echo("##### Importing API: '${apiName}' on path: '${apiPath}' for the first time");
		createVariable("swaggerFile", "/com/axway/apim/test/files/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/3_1_unpublished-api.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
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
			.extractFromPayload("$.[?(@.path=='/${apiPath}')].id", "apiId");
		
		echo("##### API-State changed to published");
		createVariable("swaggerFile", "/com/axway/apim/test/files/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/3_2_published-api.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
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
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='/${apiPath}')].id", "apiId");
	}

}
