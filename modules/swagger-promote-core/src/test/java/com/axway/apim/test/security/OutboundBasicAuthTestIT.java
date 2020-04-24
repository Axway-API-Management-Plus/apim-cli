package com.axway.apim.test.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class OutboundBasicAuthTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Test to validate API-Outbound-AuthN set to HTTP-Basic.");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/outbound-authn-test-${apiNumber}");
		variable("apiName", "Outbound AuthN Test ${apiNumber}");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with standard HTTP-Basic outbound config set #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_3_api_outbound-authn-basic.json");
		createVariable("state", "unpublished");
		createVariable("username", "6xKFp3hL7znGM+sfb90NDjmt5t9mhvqR");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with outbound security set to HTTP-Basic. #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		if(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP5") || APIManagerAdapter.hasAPIManagerVersion("7.7 SP1")) {
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
					.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
					.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
					.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
					.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "http_basic")
					.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].isDefault", "true")
					.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.username", "${username}")
					.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		} else {
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
					.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
					.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
					.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
					.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "http_basic")
					.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].isDefault", "true")
					.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.password", "password")
					.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.username", "${username}")
					.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		}

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
		createVariable("state", "unpublished");
		createVariable("username", "1234567890");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with custom Outbound-Security-Profile set to HTTP-Basic. #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
				.validate("$.[?(@.id=='${apiId}')].state", "unpublished")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[*].name", "@assertThat(hasSize(1))@") // Only one authn profile is expected!
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "http_basic")
				.validate("$.[?(@.id=='${apiId}')].outboundProfiles._default.authenticationProfile", "_default")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
		createVariable("state", "unpublished");
		createVariable("username", "1234567890");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
		
		echo("####### Simulate a change to the outbound configuration in UNPUBLISHED mode, by changing the username #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
		createVariable("state", "unpublished");
		createVariable("username", "0987654321");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the changed apiKey (username) is in place #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].id", "${apiId}")
			.validate("$.[?(@.id=='${apiId}')].state", "unpublished")
			.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
			.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "http_basic")
			.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].parameters.username", "${username}"));
		
		echo("####### Change API to status published: #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
		createVariable("state", "published");
		createVariable("username", "1234567890");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has status published. #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
				.validate("$.[?(@.id=='${apiId}')].state", "published")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "http_basic")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].parameters.username", "${username}")
				.validate("$.[?(@.id=='${apiId}')].outboundProfiles._default.authenticationProfile", "_default")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Re-Import same API: '${apiName}' on path: '${apiPath}' with status published but NOW AN API-Key (default): #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_2_api_outbound-apikey.json");
		createVariable("state", "published");
		createVariable("apiKey", "1234567890");
		createVariable("enforce", "true");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' now configured with API-Key #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "published")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "apiKey")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
				.validate("$.[?(@.path=='${apiPath}')].outboundProfiles._default.authenticationProfile", "_default")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_2_api_outbound-apikey.json");
		createVariable("state", "published");
		createVariable("apiKey", "1234567890");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
	}
}
