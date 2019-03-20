package com.axway.apim.test.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="UnpublishedPublishedApiKeyTest")
public class UnpublishedPublishedApiKeyTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "UnpublishedPublishedApiKeyTest")
	public void setupDevOrgTest() {
		description("Some checks for the API-Key security device");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/api-key-test-${apiNumber}");
		variable("apiName", "API Key Test ${apiNumber}");
		

		echo("####### Importing UNPUBLISHED API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("apiKeyFieldName", "KeyId");
		createVariable("takeFrom", "HEADER");
		createVariable("removeCredentialsOnSuccess", "false");
		createVariable("status", "unpublished");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/security/1_api-apikey.json");
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
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].type", "apiKey")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.takeFrom", "${takeFrom}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.apiKeyFieldName", "${apiKeyFieldName}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.removeCredentialsOnSuccess", "${removeCredentialsOnSuccess}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Change the API-Security settings and at the same time set it to PUBLISHED #######");
		createVariable("apiKeyFieldName", "KeyId-Test");
		createVariable("takeFrom", "QUERY");
		createVariable("removeCredentialsOnSuccess", "true");
		createVariable("status", "published");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/security/1_api-apikey.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate the Security-Settings have been changed (without changing the API-ID) #######");
		http().client("apiManager")
			.send()
			.get("/proxies/${apiId}")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].id", "${apiId}")
			.validate("$.[?(@.id=='${apiId}')].securityProfiles[0].devices[0].properties.takeFrom", "${takeFrom}")
			.validate("$.[?(@.id=='${apiId}')].securityProfiles[0].devices[0].properties.apiKeyFieldName", "${apiKeyFieldName}")
			.validate("$.[?(@.id=='${apiId}')].securityProfiles[0].devices[0].properties.removeCredentialsOnSuccess", "${removeCredentialsOnSuccess}")
			.validate("$.[?(@.id=='${apiId}')].state", "published");
		
		echo("####### Change some settings of the PUBLISHED API, which leads to a new API-ID #######");
		createVariable("apiKeyFieldName", "KeyId-Test-Published");
		createVariable("takeFrom", "HEADER");
		createVariable("removeCredentialsOnSuccess", "false");
		createVariable("status", "published");
		createVariable("enforce", "true");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/security/1_api-apikey.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate the Security-Settings have been changed (without changing the API-ID) #######");
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
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].type", "apiKey")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.takeFrom", "${takeFrom}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.apiKeyFieldName", "${apiKeyFieldName}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.removeCredentialsOnSuccess", "${removeCredentialsOnSuccess}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId")
			.validate("$.[?(@.path=='${apiPath}')].id", "@assertThat(not(equalTo(${apiId})))@");
		
		echo("First API-ID: ${apiId}");
		echo("New   API-ID: ${newApiId}");
		
	}

}
