package com.axway.apim.test.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="APIGrantToAllOrgsTestIT")
public class APIGrantToAllOrgsTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "APIGrantToAllOrgsTestIT")
	public void run() {
		description("Tool must fail is not all organizations are asigned to an API.");
		createVariable("useApiAdmin", "true"); // Use apiadmin account
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/grant_all_orgs-api-${apiNumber}");
		variable("apiName", "Grant access to all orgs API-${apiNumber}");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-all-orgs.json");
		createVariable("state", "published");
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
		
		echo("####### Re-Import without a change. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-all-orgs.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' reduce the number of orgs to TWO #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("orgName2", "${orgName2}");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		http().client("apiManager")
		.send()
		.get("/organizations/${orgId}/apis")
		.name("org1")
		.header("Content-Type", "application/json");
	
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${apiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${apiId}')].enabled", "true");
		
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
	}
}
