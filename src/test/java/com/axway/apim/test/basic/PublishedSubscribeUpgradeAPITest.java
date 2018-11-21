package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="PublishedSubscribeUpgradeAPITest")
public class PublishedSubscribeUpgradeAPITest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "PublishedSubscribeUpgradeAPITest")
	public void setupDevOrgTest() {

		echo("####### Import a Published-API, subscribe to it and then Re-Import a new version. #######");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/my-test-api-${apiNumber}");
		variable("apiName", "My-Test-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("status", "published");
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
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId") // Remember the API-ID --> This is the FE-API
			.extractFromPayload("$.[?(@.path=='${apiPath}')].apiId", "beApiId"); // This is the BE-API
		
		// Subscribe to that API!
		echo("####### Subscribing API: ${apiName} with test-application: ${testAppName} #######");
		http().client("apiManager")
			.send()
			.post("/applications/${testAppId}/apis/")
			.contentType("application/json")
			.payload("{\"apiId\":\"${apiId}\",\"enabled\":true}")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON);

		echo("####### Importing a new Swagger-File as a change #######");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable("configFile", "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("status", "published");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate the API is still there with right status #######");
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
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"); // We have a new API-ID

		echo("####### Validate subscription is still present! #######");
		http().client("apiManager")
			.send()
			.get("/applications/${testAppId}/apis")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${newApiId}')].enabled", "true");
		
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
