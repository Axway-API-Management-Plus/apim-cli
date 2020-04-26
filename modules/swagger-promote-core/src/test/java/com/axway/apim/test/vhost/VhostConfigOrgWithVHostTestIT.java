package com.axway.apim.test.vhost;

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
public class VhostConfigOrgWithVHostTestIT extends TestNGCitrusTestRunner {
	
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Test VHost with an organization having a Default virtual host");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/vhost-org-test-${apiNumber}");
		variable("apiName", "VHost Org-Test ${apiNumber}");
		variable("vhost", "abc.company.com");
		variable("vhostOrgName", "VHost Org ${orgNumber}");
		// Directly use an admin-account, otherwise the OrgAdmin organization is used by default
		variable("oadminUsername1", "apiadmin"); 
		variable("oadminPassword1", "changeme");
		
		http(builder -> builder.client("apiManager").send().post("/organizations").header("Content-Type", "application/json")
				.payload("{\"name\": \"${vhostOrgName}\", \"description\": \"Org 1 with dev permission and VHost\", \"enabled\": true, \"development\": true, \"virtualHost\": \"${vhost}\" }"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.name", "${vhostOrgName}")
				.extractFromPayload("$.id", "vhostOrgId"));

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "published");
		createVariable("vhost", "${vhost}"); // Using the same V-Host as configured for the organization.
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/vhost/2_vhost-config.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		// Validate the API has been imported correctly
		echo("####### Validate the API has been imported correctly #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "${status}")
				.validate("$.[?(@.path=='${apiPath}')].vhost", "${vhost}")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Manually unpublish this API! #######");
		http(builder -> builder.client("apiManager").send().post("/proxies/${apiId}/unpublish").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].state", "unpublished"));
		
		echo("####### Re-Import the API and the VHost must be configured again #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/vhost/2_vhost-config.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
				.validate("$.[?(@.id=='${apiId}')].state", "${status}")
				.validate("$.[?(@.id=='${apiId}')].vhost", "${vhost}"));
	}
}
