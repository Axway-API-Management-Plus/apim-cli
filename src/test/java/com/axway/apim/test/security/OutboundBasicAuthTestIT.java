package com.axway.apim.test.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName = "OutboundBasicAuthTest")
public class OutboundBasicAuthTestIT extends TestNGCitrusTestDesigner {

	@Autowired
	private ImportTestAction swaggerImport;

	@CitrusTest(name = "OutboundBasicAuthTest")
	public void run() {
		description("Test to validate API-Outbound-AuthN set to HTTP-Basic.");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/outbound-authn-test-${apiNumber}");
		variable("apiName", "Outbound AuthN Test ${apiNumber}");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
		createVariable("state", "unpublished");
		createVariable("username", "1234567890");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
		createVariable("state", "unpublished");
		createVariable("username", "1234567890");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);

		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with outbound security set to HTTP-Basic. #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "http_basic")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Simulate a change to the outbound configuration in UNPUBLISHED mode, by changing the username #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
		createVariable("state", "unpublished");
		createVariable("username", "0987654321");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate the changed apiKey (username) is in place #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].id", "${apiId}")
			.validate("$.[?(@.id=='${apiId}')].state", "unpublished")
			.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
			.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "http_basic")
			.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].parameters.username", "${username}");
		
		echo("####### Change API to status published: #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
		createVariable("state", "published");
		createVariable("username", "1234567890");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has status published. #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
				.validate("$.[?(@.id=='${apiId}')].state", "published")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "http_basic")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].parameters.username", "${username}")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Re-Import same API: '${apiName}' on path: '${apiPath}' with status published but NOW AN API-Key (default): #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_2_api_outbound-apikey.json");
		createVariable("state", "published");
		createVariable("apiKey", "1234567890");
		createVariable("enforce", "true");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' now configured with API-Key #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "published")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "apiKey")
				.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/5_2_api_outbound-apikey.json");
		createVariable("state", "published");
		createVariable("apiKey", "1234567890");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);		
	}
}
