package com.axway.apim.test.applications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class ReplaceOrgPermissionsWithSubscribedAppsTestIT extends TestNGCitrusTestRunner {

	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) {
		description("When an org is removed, belong apps are automatically removed. This isn't handled properly by Swagger-Promote. Testing this here! issue: #124");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/replace-org-permissions-with-subscribed-apps-${apiNumber}");
		variable("apiName", "Replace Org-Permissions with subscribed Apps-${apiNumber}");
		variable("clientAppsMode", "replace");
		variable("clientOrgsMode", "replace");
		// ############## Create 4 Consuming organizations #################
		createVariable("orgName1", "1 Client-Org ${orgNumber}");
		createVariable("orgName2", "2 Client-Org ${orgNumber}");
		createVariable("orgName3", "3 Client-Org ${orgNumber}");
		createVariable("orgName4", "4 Client-Org ${orgNumber}");
		
		http(builder -> builder.client("apiManager").send().post("/organizations").header("Content-Type", "application/json")
				.payload("{\"name\": \"${orgName1}\", \"description\": \"Org 1 without dev permission\", \"enabled\": true, \"development\": false }"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.name", "${orgName1}")
				.extractFromPayload("$.id", "orgId1"));
		
		http(builder -> builder.client("apiManager").send().post("/organizations").header("Content-Type", "application/json")
				.payload("{\"name\": \"${orgName2}\", \"description\": \"Org 2 without dev permission\", \"enabled\": true, \"development\": false }"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.name", "${orgName2}")
				.extractFromPayload("$.id", "orgId2"));
		
		http(builder -> builder.client("apiManager").send().post("/organizations").header("Content-Type", "application/json")
				.payload("{\"name\": \"${orgName3}\", \"description\": \"Org 3 without dev permission\", \"enabled\": true, \"development\": false }"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.name", "${orgName3}")
				.extractFromPayload("$.id", "orgId3"));
		
		http(builder -> builder.client("apiManager").send().post("/organizations").header("Content-Type", "application/json")
				.payload("{\"name\": \"${orgName4}\", \"description\": \"Org 4 without dev permission\", \"enabled\": true, \"development\": false }"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.name", "${orgName4}")
				.extractFromPayload("$.id", "orgId4"));
		
		echo("####### Created 4 Test-Organizations #######");
		
		// ############## Creating 4 Test-Applications #################
		createVariable("appName1", "App 1 in org ${apiNumber}");
		createVariable("appName2", "App 2 in org ${apiNumber}");
		createVariable("appName3", "App 3 in org ${apiNumber}");
		createVariable("appName4", "App 4 in org ${apiNumber}");
		
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName1}\",\"apis\":[],\"organizationId\":\"${orgId1}\"}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.name", "${appName1}")
				.extractFromPayload("$.id", "appId1"));
		
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
				.payload("{\"name\":\"${appName2}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.name", "${appName2}")
				.extractFromPayload("$.id", "appId2"));
		
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
				.payload("{\"name\":\"${appName3}\",\"apis\":[],\"organizationId\":\"${orgId3}\"}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.name", "${appName3}")
				.extractFromPayload("$.id", "appId3"));
		
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
				.payload("{\"name\":\"${appName4}\",\"apis\":[],\"organizationId\":\"${orgId4}\"}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.name", "${appName4}")
				.extractFromPayload("$.id", "appId4"));
		
		echo("####### Created 4 Test-Applications #######");
		
		echo("####### Importing Published API: '${apiName}' on path: '${apiPath}' having Clients-Ors and Apps #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-4-orgs-4-apps.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has been created #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Validate each organization has access to this API #######");
		http(builder -> builder.client("apiManager").send().get("/organizations/${orgId1}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${apiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${apiId}')].enabled", "true"));
		http(builder -> builder.client("apiManager").send().get("/organizations/${orgId2}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${apiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${apiId}')].enabled", "true"));
		http(builder -> builder.client("apiManager").send().get("/organizations/${orgId3}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${apiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${apiId}')].enabled", "true"));
		http(builder -> builder.client("apiManager").send().get("/organizations/${orgId4}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${apiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${apiId}')].enabled", "true"));
		
		echo("####### Validate each application has access to this API #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId1}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		http(builder -> builder.client("apiManager").send().get("/applications/${appId2}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		http(builder -> builder.client("apiManager").send().get("/applications/${appId3}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		http(builder -> builder.client("apiManager").send().get("/applications/${appId4}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}"));
		
		// ############## Update the API, but remove one organization, but leave the number of apps unchanged #################
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-3-orgs-4-apps.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "0");
		createVariable("enforce", "true");
		createVariable("image", "/com/axway/apim/test/files/basic/API-Logo.jpg");
		swaggerImport.doExecute(context);
		
		echo("####### Validate Updated API - Remaining 3 orgs and their Apps have still access #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"));
		
		echo("####### Validate each organization has access to the Re-Created API #######");
		http(builder -> builder.client("apiManager").send().get("/organizations/${orgId1}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${newApiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${newApiId}')].enabled", "true"));
		http(builder -> builder.client("apiManager").send().get("/organizations/${orgId2}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${newApiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${newApiId}')].enabled", "true"));
		http(builder -> builder.client("apiManager").send().get("/organizations/${orgId3}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${newApiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${newApiId}')].enabled", "true"));
		
		echo("####### Validate each application has access to the Re-Created API #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId1}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}"));
		http(builder -> builder.client("apiManager").send().get("/applications/${appId2}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}"));
		http(builder -> builder.client("apiManager").send().get("/applications/${appId3}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}"));
		
		// ############## Re-Create this API and remove one organization! #################
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-2-orgs-4-apps.json");
		createVariable("state", "published");
		createVariable("orgName", "${orgName}");
		createVariable("expectedReturnCode", "0");
		createVariable("enforce", "true");
		swaggerImport.doExecute(context);
		
		echo("####### Validate Re-Created API exists and remaining 2 orgs and their Apps have still access #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"));
		
		echo("####### Validate each organization has access to the Re-Created API #######");
		http(builder -> builder.client("apiManager").send().get("/organizations/${orgId1}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${newApiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${newApiId}')].enabled", "true"));
		http(builder -> builder.client("apiManager").send().get("/organizations/${orgId2}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.apiId=='${newApiId}')].state", "approved")
			.validate("$.[?(@.apiId=='${newApiId}')].enabled", "true"));
		
		echo("####### Validate each application has access to the Re-Created API #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId1}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}"));
		http(builder -> builder.client("apiManager").send().get("/applications/${appId2}/apis").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}"));
	}
}
