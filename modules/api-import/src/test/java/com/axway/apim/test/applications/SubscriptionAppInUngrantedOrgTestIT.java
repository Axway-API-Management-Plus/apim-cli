package com.axway.apim.test.applications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="SubscriptionAppInUngrantedOrgTestIT")
public class SubscriptionAppInUngrantedOrgTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "SubscriptionAppInUngrantedOrgTestIT")
	public void run() {
		description("This test validates the behavior if a Client-App-Subscription is configured for an org without API-Permission.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/app-in-ungranted-org-${apiNumber}");
		variable("apiName", "App-Subscription wrong Org-${apiNumber}");
		// ############## Creating Test-Application 1 #################
		createVariable("appName1", "App in granted org ${orgNumber}");
		createVariable("appName2", "App in ungranted org ${orgNumber}");
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName1}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "testAppId1")
			.extractFromPayload("$.name", "testAppName1");
		
		echo("####### Created Test-Application: '${testAppName1}' with id: '${testAppId1}' #######");
		
		// ############## Creating Test-Application 2 #################
		createVariable("appName1", "App in granted org ${orgNumber}");
		createVariable("appName2", "App in ungranted org ${orgNumber}");
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName2}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "testAppId2")
			.extractFromPayload("$.name", "testAppName2");
		
		echo("####### Created Test-Application: '${testAppName2}' with id: '${testAppId2}' #######");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-2-app.json");
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
		
		echo("####### Validate App-1: '${testAppName1}' (ID: ${testAppId1}) has access #######");

		http().client("apiManager")
			.send()
			.get("/applications/${testAppId1}/apis")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "@assertThat(containsString(${apiId}))@");
		
		echo("####### Validate App-2: '${testAppName2}' (ID: ${testAppId2}) has NO access #######");
		http().client("apiManager")
			.send()
			.get("/applications/${testAppId2}/apis")
			.name("api")
			.header("Content-Type", "application/json");
	
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "@assertThat(not(containsString(${apiId})))@");
	}
}
