package com.axway.apim.test.organizations;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class ChangeDevelopmentOrgTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Changing the Development Org (See issue #138 and using a toggle #263)");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/change-dev-org-api-${apiNumber}");
		variable("apiName", "Change the Dev-Org API-${apiNumber}");
		// This test must be executed with an Admin-Account as we need to flip the organization
		variable("oadminUsername1", "apiadmin"); 
		variable("oadminPassword1", "changeme");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/dynamic-organization.json");
		createVariable("state", "unpublished");
		createVariable("testOrgName", "${orgName}");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the imported APIs initialy belongs to '${orgName}' (${orgId}) #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.validate("$.[?(@.path=='${apiPath}')].organizationId", "${orgId}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Try to Re-Import the same API with a changed development organization: '${orgName3}' (${orgId3}). This must FAIL as the parameter: changeOrganization is not set #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/dynamic-organization.json");
		createVariable("state", "unpublished");
		createVariable("testOrgName", "${orgName3}");
		createVariable("expectedReturnCode", "7"); // Without providing the Force-Organization-Flip-Toggle we expect an error
		swaggerImport.doExecute(context);
		
		echo("####### Re-Import the same API with a changed development organization: '${orgName3}' (${orgId3}) #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/dynamic-organization.json");
		createVariable("state", "unpublished");
		createVariable("testOrgName", "${orgName3}");
		variable("changeOrganization", "true"); 
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the re-imported APIs now belongs to '${orgName3}' (${orgId3}) #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
				.validate("$.[?(@.path=='${apiPath}')].organizationId", "${orgId3}")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Perform a No-Change test! #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/dynamic-organization.json");
		createVariable("state", "unpublished");
		createVariable("testOrgName", "${orgName3}");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
	}
}
