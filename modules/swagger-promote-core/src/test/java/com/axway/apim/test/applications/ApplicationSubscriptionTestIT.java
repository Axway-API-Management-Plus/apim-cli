package com.axway.apim.test.applications;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class ApplicationSubscriptionTestIT extends TestNGCitrusTestRunner {
	
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		swaggerImport = new ImportTestAction();
		description("Import an API, grant access to an org and create an application subscription.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/app-subscription-${apiNumber}");
		variable("apiName", "App Subscription API-${apiNumber}");
		// ############## Creating Test-Application 1 #################
		createVariable("app1Name", "Consuming Test App 1 ${orgNumber}");
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
			.payload("{\"name\":\"${app1Name}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp1Id")
			.extractFromPayload("$.name", "consumingTestApp1Name"));
		
		echo("####### Created Test-Application 1: '${consumingTestApp1Name}' with id: '${consumingTestApp1Id}' #######");
		
		http(builder -> builder.client("apiManager").send().post("/applications/${consumingTestApp1Id}/apikeys")
			.header("Content-Type", "application/json")
			.payload("{\"applicationId\":\"${consumingTestApp1Id}\",\"enabled\":\"true\"}"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp1ApiKey"));
		
		echo("####### Added an API-Key: '${consumingTestApp1ApiKey}' to Test-Application 1 #######");
		
		// ############## Creating Test-Application 2 #################
		createVariable("extClientId", RandomNumberFunction.getRandomNumber(15, true));
		createVariable("app2Name", "Consuming Test App 2 ${orgNumber}");
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
			.payload("{\"name\":\"${app2Name}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp2Id")
			.extractFromPayload("$.name", "consumingTestApp2Name"));
		
		echo("####### Created Test-Application 2: '${consumingTestApp2Name}' with id: '${consumingTestApp2Id}' #######");
	
		http(builder -> builder.client("apiManager").send().post("/applications/${consumingTestApp2Id}/extclients").header("Content-Type", "application/json")
			.payload("{\"clientId\":\"${extClientId}\",\"enabled\":\"true\"}"));
	
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp2ClientId"));
		
		echo("####### Added an Ext-ClientID: '${extClientId}' to Test-Application 2 #######");
		
		// ############## Creating Test-Application 3 #################
		createVariable("app3Name", "Consuming Test App 3 ${orgNumber}");
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
			.payload("{\"name\":\"${app3Name}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp3Id")
			.extractFromPayload("$.name", "consumingTestApp3Name"));
		
		echo("####### Created Test-Application 3: '${consumingTestApp3Name}' with id: '${consumingTestApp3Id}' #######");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has been created #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### API has been created with ID: '${apiId}' #######");
		
		echo("####### Validate created application 3 has an active subscription to the API (Based on the name) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp3Id}/apis").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		
		echo("####### Validate Application 1 has an active subscription to the API (based on the API-Key) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp1Id}/apis").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		
		echo("####### Validate Application 2 has an active subscription to the API (based on the Ext-Client-Id) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp2Id}/apis")
			.header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		
		echo("####### Re-Importing same API: '${apiName}' - must result in No-Change #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
		
		echo("####### Make sure, the API-ID hasn't changed #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		// Check the API is still exposed on the same path
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].id", "${apiId}")); // Must be the same API-ID as before!
	}
}
