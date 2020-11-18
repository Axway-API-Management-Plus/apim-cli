package com.axway.apim.test.applications;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.CoreParameters.Mode;
import com.axway.apim.lib.errorHandling.AppException;
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
		//TestIndicator.getInstance().setTestRunning(false);
		swaggerImport = new ImportTestAction();
		swaggerImport = new ImportTestAction();
		description("Import an API, grant access to an org and create an application subscription.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/app-subscription-${apiNumber}");
		variable("apiName", "App Subscription API-${apiNumber}");
		// ############## Creating Test-Application 1 #################
		createVariable("app1Name", "Test-SubApp 1 ${apiNumber}");
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
		
		echo("####### Generated API-Key: '${consumingTestApp1ApiKey}' for Test-Application 1: '${consumingTestApp1Name}' with id: '${consumingTestApp1Id}' #######");
		
		// ############## Creating Test-Application 2 #################
		createVariable("extClientId", RandomNumberFunction.getRandomNumber(15, true));
		createVariable("app2Name", "Test-SubApp 2 ${apiNumber}");
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
		
		echo("####### Added an Ext-ClientID: '${extClientId}' to Test-Application 2: '${consumingTestApp2Name}' with id: '${consumingTestApp2Id}' #######");
		
		// ############## Creating Test-Application 3 #################
		createVariable("app3Name", "Test-SubApp 3 ${apiNumber}");
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
			.payload("{\"name\":\"${app3Name}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestApp3Id")
			.extractFromPayload("$.name", "consumingTestApp3Name"));
		
		echo("####### Created Test-Application 3: '${consumingTestApp3Name}' with id: '${consumingTestApp3Id}' withouth App-Credentials #######");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("version", "1.0.0");
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
		
		echo("####### Changin FE-API Settings only for: '${apiName}' - Mode: Unpublish/Publish and make sure subscriptions stay #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("version", "2.0.0");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has been reconfigured (Unpublich/Publish) and appscriptions are recreated #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published"));
		
		echo("####### Validate Application 3 still has an active subscription to the API (Based on the name) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp3Id}/apis").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		
		echo("####### Validate Application 1 still has an active subscription to the API (based on the API-Key) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp1Id}/apis").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		
		echo("####### Validate Application 2 still has an active subscription to the API (based on the Ext-Client-Id) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp2Id}/apis")
			.header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		
		echo("####### Re-Importing same API: '${apiName}' - Without applications subscriptions and mode replace #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("enforce", "true");
		createVariable("clientAppsMode", String.valueOf(Mode.replace));
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has been re-created and subscriptions has been removed #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "published")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"));
		
		echo("####### Validate the application no Access to this API #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp1Id}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "@assertThat(not(contains(${newApiId})))@"));
		
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp2Id}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "@assertThat(not(contains(${newApiId})))@"));
		
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp3Id}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "@assertThat(not(contains(${newApiId})))@"));
		
		echo("####### Changing the state to unpublished #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
		createVariable("state", "unpublished");
		createVariable("enforce", "true");
		createVariable("clientAppsMode", String.valueOf(Mode.add));
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Re-Import the API forcing a re-creation with an ORG-ADMIN ONLY account, making sure App-Subscriptions a re-created #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-2-app.json");
		createVariable("state", "unpublished");
		createVariable("enforce", "false");
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "0");
		createVariable("ignoreAdminAccount", "true"); // We need to ignore any given admin account!
		// We only provide two apps instead of three, but the existing third subscription must stay!
		createVariable("testAppName1", "${consumingTestApp1Name}");
		createVariable("testAppName2", "${consumingTestApp2Name}");		
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has been RE-CREATED #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"));
		
		echo("####### API has been RE-CREATED with ID: '${newApiId}' #######");
		
		echo("####### Validate created application 3 STILL has an active subscription to the API (Based on the name) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp3Id}/apis").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}"));
		
		echo("####### Validate Application 1 STILL has an active subscription to the API (based on the API-Key) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp1Id}/apis").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}"));
		
		echo("####### Validate Application 2 STILL has an active subscription to the API (based on the Ext-Client-Id) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp2Id}/apis")
			.header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}"));
	}
}
