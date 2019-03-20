package com.axway.apim.test.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="IgnoreClientOrgsTestIT")
public class IgnoreClientOrgsTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "IgnoreClientOrgsTestIT")
	public void setupDevOrgTest() {
		description("This test makes sure, no organizations have been granted permission.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/grant_invalid_org-api-${apiNumber}");
		variable("apiName", "Grant to invalid orgs API-${apiNumber}");
		
		variable("testOrgName", "Org without permission ${apiNumber}");

		http().client("apiManager")
			.send()
			.post("/organizations")
			.name("anotherOrgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"${testOrgName}\", \"description\": \"Org without permission\", \"enabled\": true, \"development\": true }");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "${testOrgName}")
			.extractFromPayload("$.id", "noPermOrgId");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("orgName2", "${testOrgName}");
		createVariable("clientOrgsMode", "ignore");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been imported without an error (defined orgs are ignored) #######");
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
		
		echo("####### Validate second org has no permission #######");
		http().client("apiManager")
			.send()
			.get("/organizations/${noPermOrgId}/apis")
			.name("org2")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "@assertThat(not(containsString(${apiId})))@");
	}
}
