package com.axway.apim.test.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.lib.CoreParameters.Mode;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class APIRemoveAccessForAnOrgTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	public void run() {
		description("Import an API can grant access to a number of defined orgs");
		variable("useApiAdmin", "true");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/grant_org-api-${apiNumber}");
		variable("apiName", "Grant to some orgs API-${apiNumber}");
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("orgName2", "${orgName2}");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' is granted to defined organizations: '${orgName}', '${orgName2}' #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
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
		
		
		echo("####### Running with mode \"Replace\" - The API should now be granted to only ONE organization - The other must be removed #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-client-1-org.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("clientOrgsMode", String.valueOf(Mode.replace));
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
			.validate("$.*.apiId", "@assertThat(not(containsString(${apiId})))@");
	}
}
