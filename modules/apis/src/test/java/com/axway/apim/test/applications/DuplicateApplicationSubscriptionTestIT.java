package com.axway.apim.test.applications;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class DuplicateApplicationSubscriptionTestIT extends TestNGCitrusTestRunner {
	
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Verify subscription handling, if App isn't unique based on the name (See issue #217)");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/duplicate.app-subscription-${apiNumber}");
		variable("apiName", "Duplicate-App Subscription API-${apiNumber}");
		// ############## Creating Test-Application 1 #################
		createVariable("app1Name", "Consuming Test App 1 ${apiNumber}");
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
		
		// ############## Creating the same application, having the same name, different organization #################
		createVariable("extClientId", RandomNumberFunction.getRandomNumber(15, true));
		createVariable("app2Name", "Consuming Test App 1 ${apiNumber}");
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
			.payload("{\"name\":\"${app2Name}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp2Id")
			.extractFromPayload("$.name", "consumingTestApp2Name"));
		
		echo("####### Created Test-Application 2: '${consumingTestApp2Name}' with id: '${consumingTestApp2Id}' #######");
	
		http(builder -> builder.client("apiManager").send().post("/applications/${consumingTestApp2Id}/extclients").header("Content-Type", "application/json")
			.payload("{\"clientId\":\"${extClientId}\",\"enabled\":\"true\"}"));
	
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp2ClientId"));
		
		echo("####### Added an Ext-ClientID: '${extClientId}' to Test-Application 2 #######");
		
		echo("####### Try to importing API: '${apiName}' on path: '${apiPath}' having a Non-Unique subscription #######");	
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-1-app.json");
		// Try to create a subscription with a Non-Unique application name
		createVariable("testAppName", "${app1Name}");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "89");
		swaggerImport.doExecute(context);
		
		echo("####### Importing the same API: '${apiName}' on path: '${apiPath}' given the orgname as part of the application-name #######");	
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-1-app.json");
		// Try to create a subscription with a Non-Unique application name
		createVariable("testAppName", "${app1Name}|${orgName2}");
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
		
		echo("####### Validate created application 2 has an active subscription to the API (NOT THE FIRST APPLICATION) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp2Id}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		
		echo("####### Validate created application 1 has NO active subscription to the API #######");		
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp1Id}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "@assertThat(not(containsString(${apiId})))@"));
	}
}
