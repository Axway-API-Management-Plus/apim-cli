package com.axway.apim.test.applications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="ApplicationSubscriptionTestIT")
public class ApplicationSubscriptionTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "ApplicationSubscriptionTestIT")
	public void setupDevOrgTest() {
		description("Import an API, grant access to an org and create an application subscription.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/app-subscription-${apiNumber}");
		variable("apiName", "App Subscription API-${apiNumber}");
		// ############## Creating Test-Application 1 #################
		createVariable("app1Name", "Consuming Test App 1 ${orgNumber}");
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${app1Name}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp1Id")
			.extractFromPayload("$.name", "consumingTestApp1Name");
		
		echo("####### Created Test-Application 1: '${consumingTestApp1Name}' with id: '${consumingTestApp1Id}' #######");
		
		http().client("apiManager")
			.send()
			.post("/applications/${consumingTestApp1Id}/apikeys")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"applicationId\":\"${consumingTestApp1Id}\",\"enabled\":\"true\"}");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp1ApiKey");
		
		echo("####### Added an API-Key: '${consumingTestApp1ApiKey}' to Test-Application 1 #######");
		
		// ############## Creating Test-Application 2 #################
		createVariable("extClientId", RandomNumberFunction.getRandomNumber(15, true));
		createVariable("app2Name", "Consuming Test App 2 ${orgNumber}");
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${app2Name}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp2Id")
			.extractFromPayload("$.name", "consumingTestApp2Name");
		
		echo("####### Created Test-Application 2: '${consumingTestApp2Name}' with id: '${consumingTestApp2Id}' #######");
	
		http().client("apiManager")
			.send()
			.post("/applications/${consumingTestApp2Id}/extclients")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"clientId\":\"${extClientId}\",\"enabled\":\"true\"}");
	
		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp2ClientId");
		
		echo("####### Added an Ext-ClientID: '${extClientId}' to Test-Application 2 #######");
		
		// ############## Creating Test-Application 3 #################
		createVariable("app3Name", "Consuming Test App 3 ${orgNumber}");
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${app3Name}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp3Id")
			.extractFromPayload("$.name", "consumingTestApp3Name");
		
		echo("####### Created Test-Application 3: '${consumingTestApp3Name}' with id: '${consumingTestApp3Id}' #######");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
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
		
		echo("####### Validate created application 3 has an active subscription to the API (Based on the name) #######");
		http().client("apiManager")
			.send()
			.get("/applications/${consumingTestApp3Id}/apis")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}");
		
		echo("####### Validate Application 1 has an active subscription to the API (based on the API-Key) #######");
		http().client("apiManager")
			.send()
			.get("/applications/${consumingTestApp1Id}/apis")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}");
		
		echo("####### Validate Application 2 has an active subscription to the API (based on the Ext-Client-Id) #######");
		http().client("apiManager")
			.send()
			.get("/applications/${consumingTestApp2Id}/apis")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}");
		
		echo("####### Re-Importing same API: '${apiName}' - must result in No-Change #######");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
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
