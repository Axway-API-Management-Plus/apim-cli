package com.axway.apim.test.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="APIGrantOrganizationsTestIT")
public class APIGrantOrganizationsTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "APIGrantOrganizationsTestIT")
	public void setupDevOrgTest() {
		description("Import an API can grant access to a number of defined orgs");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/grant_org-api-${apiNumber}");
		variable("apiName", "Grant to some orgs API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
		createVariable("state", "unpublished");
		createVariable("orgName", "${orgName}");
		createVariable("orgName2", "${orgName2}");
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
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
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
			.validate("$.[?(@.id=='${apiId}')].state", "published");
		
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
		
		
		echo("####### Execute the same definition - Tool must return with No-Change return code #######");
		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("orgName2", "${orgName2}");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
		
		echo("####### Going back to unpublished forcing a breaking change #######");
		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable("configFile", "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
		createVariable("state", "unpublished");
		createVariable("orgName", "${orgName}");
		createVariable("orgName2", "${orgName2}");
		createVariable("enforce", "true");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been imported without an error in state unpublished #######");
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
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId");
	}
}
