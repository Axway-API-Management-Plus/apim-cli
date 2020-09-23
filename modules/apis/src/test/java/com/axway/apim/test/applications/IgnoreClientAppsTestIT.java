package com.axway.apim.test.applications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.lib.CoreParameters.Mode;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="IgnoreClientAppsTestIT")
public class IgnoreClientAppsTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "IgnoreClientAppsTestIT")
	public void run() {
		description("This test makes sure, no client-applications have got a subscription.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/ignore-client-apps-test-${apiNumber}");
		variable("apiName", "Ignore Client-Apps-API-${apiNumber}");
		
		// ############## Creating Test-Application #################
		createVariable("testAppName", "Ignored Test App-Name ${apiNumber}");
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${testAppName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "testAppId")
			.extractFromPayload("$.name", "testAppName");
		
		echo("####### Created Test-Application to be ignored: '${testAppName}' with id: '${testAppId}' #######");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-1-app.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("clientAppsMode", String.valueOf(Mode.ignore));
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been imported without an error (defined apps are ignored) #######");
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
		
		echo("####### Validate the application no Access to this API #######");
		http().client("apiManager")
			.send()
			.get("/applications/${testAppId}/apis")
			.name("api")
			.header("Content-Type", "application/json");
	
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "@assertThat(not(containsString(${apiId})))@");
	}
}
