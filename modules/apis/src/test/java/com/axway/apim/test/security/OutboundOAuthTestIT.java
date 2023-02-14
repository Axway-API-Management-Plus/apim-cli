package com.axway.apim.test.security;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;

@Test
public class OutboundOAuthTestIT extends TestNGCitrusTestRunner {

	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
		ImportTestAction swaggerImport = new ImportTestAction();
		description("Test to validate API-Outbound-AuthN set to OAuth works as expected.");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/outbound-authn-oauth-test-${apiNumber}");
		variable("apiName", "Outbound AuthN OAuth Test ${apiNumber}");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with OAuth outbound config set #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_4_api_outbound-authn-oauth.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with outbound security set to OAuth has been imported. #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[*].name", "@assertThat(hasSize(1))@") // Only one authn profile is expected!
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "oauth")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].isDefault", "true")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.providerProfile", "@assertThat(containsString(<key type='AuthProfilesGroup))@")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.ownerId", "TEST-SOMETHING")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_4_api_outbound-authn-oauth.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
	}
}
