package com.axway.apim.test.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="InvokeAuthPolicyTestIT")
public class InvokeAuthPolicyTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "InvokeAuthPolicyTestIT")
	public void setupDevOrgTest() {
		description("Tests for Invoke-Policy Security configuration");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/invoke-policy-test-${apiNumber}");
		variable("apiName", "API Invoke-Policy Test ${apiNumber}");
		variable("status", "unpublished");
		

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("authPolicy", "Authentication policy 1");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/security/6_api-invoke-policy.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with correct settings #######");
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
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].type", "authPolicy")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.authenticationPolicy", "@assertThat(containsString(id field='name' value='${authPolicy}'))@")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Simulate re-import with no-change #######");
		createVariable("tokenInfoPolicy", "Authentication policy 1");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/security/6_api-invoke-policy.json");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
	}
}
