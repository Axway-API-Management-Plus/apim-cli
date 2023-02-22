package com.axway.apim.test.security;

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
public class OutboundNoAuthWithPoliciesTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Test to validate behavior if OutboundProfiles are set, but no AuthN-Profile. It should result into no AuthN.");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/issue-133-noauth-${apiNumber}");
		variable("apiName", "Issue 133 No-Auth ${apiNumber}");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' Outbound-Profiles (for policies) not containing a AuthN-Profile. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/issue-133-outbound-profile-no-auth.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with outbound security set to No-AuthN as this should be the default. #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "none")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].isDefault", "true")
				.validate("$.[?(@.path=='${apiPath}')].outboundProfiles._default.authenticationProfile", "_default")
				.validate("$.[?(@.path=='${apiPath}')].outboundProfiles._default.routeType", "policy")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/issue-133-outbound-profile-no-auth.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
	}
}
