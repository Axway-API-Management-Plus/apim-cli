package com.axway.apim.test.applications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class AppSubscriptionUnpublishedTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	public void run() {
		description("Validates, that App-Subscriptions are working on a Unpublished API!");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/app-subscr-unpublished-${apiNumber}");
		variable("apiName", "App Subsc Unpublished API-${apiNumber}");
		// ############## Creating Test-Application #################

		createVariable("appName", "Consuming Test App ${orgNumber}");
		http().client("apiManager").send()
			.post("/applications")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestAppId")
			.extractFromPayload("$.name", "consumingTestAppName");
		
		echo("####### Created Test-Application: '${consumingTestAppName}' with id: '${consumingTestAppId}' #######");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been created #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### API has been created with ID: '${apiId}' #######");
		
		echo("####### Validate the application has already an active subscription to the API (Based on the name) #######");
		http().client("apiManager")
			.send()
			.get("/applications/${consumingTestAppId}/apis")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}");
		
		echo("####### Trigger a Re-Create of the API: '${apiName}' - Subscription must stay  #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been Re-Created #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId");
		
		echo("####### Validate the application still has an active subscription to the API #######");
		http().client("apiManager")
			.send()
			.get("/applications/${consumingTestAppId}/apis")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}");
	}
}
