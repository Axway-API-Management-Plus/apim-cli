package com.axway.apim.test.applications;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

@Test
public class AppSubscriptionUnpublishedTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	public void run() {
		description("Validates, that App-Subscriptions are working on a Unpublished API!");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/app-subscr-unpublished-${apiNumber}");
		variable("apiName", "App Subsc Unpublished API-${apiNumber}");
		// ############## Creating Test-Application #################

		createVariable("appName", "Unpublished Test App ${apiNumber}");
		http().client("apiManager").send()
			.post("/applications")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "testAppId")
			.extractFromPayload("$.name", "testAppName");
		
		echo("####### Created Test-Application: '${testAppName}' with id: '${testAppId}' #######");
		
		createVariable("appName2", "Unpublished Test App 2 ${apiNumber}");
		http().client("apiManager").send()
			.post("/applications")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName2}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "testAppId2")
			.extractFromPayload("$.name", "testAppName2");
		
		echo("####### Created Test-Application: '${testAppName2}' with id: '${testAppId2}' #######");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
		createVariable("consumingTestAppName", "${testAppName}");
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
			.get("/applications/${testAppId}/apis")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}");
		
		echo("####### Trigger a Re-Create of the API: '${apiName}' - Subscription must stay  #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
		createVariable("consumingTestAppName", "${testAppName}");
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
			.get("/applications/${testAppId}/apis")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}");
		
		echo("####### Simulate the App-Subscription from before has been created manually (by removing it from the Config-File) #######");
		echo("####### Trigger a Re-Create of the API: '${apiName}' - Subscription must stay  #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
		createVariable("consumingTestAppName", "${testAppName2}");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been Re-Created #######");
		http().client("apiManager").send().get("/proxies").header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId2");
		
		echo("####### Validate that BOTH applications still has an active subscription to the API #######");
		echo("####### First app: ${testAppName} (${testAppId}) #######");
		http().client("apiManager").send().get("/applications/${testAppId}/apis").header("Content-Type", "application/json");
		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON).validate("$.*.apiId", "${newApiId2}");
		echo("####### Second app: ${testAppName2} (${testAppId2}) #######");
		http().client("apiManager").send().get("/applications/${testAppId2}/apis").header("Content-Type", "application/json");
		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON).validate("$.*.apiId", "${newApiId2}");
		
		echo("####### Simulate the Config-File has no applications configured - Existing subscriptions must stay! #######");
		echo("####### Trigger a Re-Create of the API: '${apiName}' - Subscription must stay  #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been Re-Created #######");
		http().client("apiManager").send().get("/proxies").header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId3");
		
		echo("####### Validate again that BOTH applications still has an active subscription to the API #######");
		echo("####### First app: ${testAppName} (${testAppId}) #######");
		http().client("apiManager").send().get("/applications/${testAppId}/apis").header("Content-Type", "application/json");
		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON).validate("$.*.apiId", "${newApiId3}");
		echo("####### Second app: ${testAppName2} (${testAppId2}) #######");
		http().client("apiManager").send().get("/applications/${testAppId2}/apis").header("Content-Type", "application/json");
		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON).validate("$.*.apiId", "${newApiId3}");
	}
}
