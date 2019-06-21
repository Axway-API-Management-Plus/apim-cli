package com.axway.apim.test.applications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="ApplicationSubscriptionNoOrgsTestIT")
public class ApplicationSubscriptionNoOrgsTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "ApplicationSubscriptionNoOrgsTestIT")
	public void run() {
		description("Import an API and create an application subscription while not having defined any organization in the configuration.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/app-subscription-${apiNumber}");
		variable("apiName", "App Subscription API-${apiNumber}");
		// ############## Creating Test-Application #################

		createVariable("appName", "Consuming Test App ${orgNumber}");
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestAppId")
			.extractFromPayload("$.name", "consumingTestAppName");
		
		echo("####### Created Test-Application: '${consumingTestAppName}' with id: '${consumingTestAppId}' #######");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been created #######");
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
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### API has been created with ID: '${apiId}' #######");
		
		echo("####### Validate created application has an active subscription to the API (Based on the name) #######");
		http().client("apiManager")
			.send()
			.get("/applications/${consumingTestAppId}/apis")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}");
		
		echo("####### Re-Importing same API: '${apiName}' - must result in No-Change #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
		
		echo("####### Make sure, the API-ID hasn't changed #######");
		http().client("apiManager")
			.send()
			.get("/proxies/${apiId}")
			.name("api")
			.header("Content-Type", "application/json");

		// Check the API is still exposed on the same path
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].id", "${apiId}"); // Must be the same API-ID as before!
	}
}
