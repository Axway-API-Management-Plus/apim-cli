package com.axway.apim.test.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="ClientOrgModeAddTestIT")
public class ClientOrgModeAddTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "ClientOrgModeAddTestIT")
	public void run() {
		description("Validates the Client-Org-Mode: add is working as expected.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/org-mode-add-api-${apiNumber}");
		variable("apiName", "Org-Mode-Add Test API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-client-1-org.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName2}"); // Initially this org get's access (simulate doing this in the UI)
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been imported and get generated API-ID #######");
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
		
		echo("####### Validate organization: '${orgName2}' has access to the imported API #######");
		http().client("apiManager")
			.send()
			.get("/organizations/${orgId2}/apis")
			.name("org2")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${apiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${apiId}')].enabled", "true");

		
		echo("####### Grant access to another API with mode: ADD, the existing Org-Access must stay #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-client-1-org.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName3}"); // This time another org must be added, the existing permission must stay
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate organization: '${orgName2}' STILL has access to the imported API #######");
		http().client("apiManager")
			.send()
			.get("/organizations/${orgId2}/apis")
			.name("org2")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${apiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${apiId}')].enabled", "true");
		
		echo("####### Validate organization: '${orgName3}' has NOW access to the imported API #######");
		http().client("apiManager")
			.send()
			.get("/organizations/${orgId3}/apis")
			.name("org3")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${apiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${apiId}')].enabled", "true");
	}
}
