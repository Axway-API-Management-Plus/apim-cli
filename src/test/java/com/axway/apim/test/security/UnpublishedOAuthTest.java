package com.axway.apim.test.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="UnpublishedOAuthTest")
public class UnpublishedOAuthTest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "UnpublishedOAuthTest")
	public void setupDevOrgTest() {
		description("Tests for API-OAuth Security connfiguration");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/oauth-test-${apiNumber}");
		variable("apiName", "API OAuth Test ${apiNumber}");
		variable("status", "unpublished");
		

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("tokenStore", "OAuth Access Token Store");
		createVariable("accessTokenLocation", "HEADER");
		createVariable("scopes", "resource.WRITE, resource.READ, resource.ADMIN");
		createVariable("removeCredentialsOnSuccess", "false");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/security/3_api-oauth.json");
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
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].type", "oauth")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.tokenStore", "@assertThat(containsString(${tokenStore}))@")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.scopes", "${scopes}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.accessTokenLocation", "${accessTokenLocation}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Simulate re-import with no-change #######");
		createVariable("tokenStore", "OAuth Access Token Store");
		createVariable("accessTokenLocation", "HEADER");
		createVariable("scopes", "resource.WRITE, resource.READ, resource.ADMIN");
		createVariable("removeCredentialsOnSuccess", "false");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/security/3_api-oauth.json");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
	}
}
