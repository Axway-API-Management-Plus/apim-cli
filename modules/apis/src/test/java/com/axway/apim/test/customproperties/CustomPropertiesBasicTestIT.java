package com.axway.apim.test.customproperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="CustomPropertiesBasicTest")
public class CustomPropertiesBasicTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "CustomPropertiesBasicTest")
	public void run() {
		description("Importing & validating custom-properties");
		variable("useApiAdmin", "true"); // Use apiadmin account
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/api-custom-prop-test-${apiNumber}");
		variable("apiName", "API Custom-Properties Test ${apiNumber}");
		variable("status", "unpublished");
		

		echo("####### 1. Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "unpublished");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/customproperties/1_custom-properties-config_IT.json");
		createVariable("customProperty1", "Test-Input 1");
		createVariable("customProperty2", "1");
		createVariable("customProperty3", "true");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######");
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
			.validate("$.[?(@.path=='${apiPath}')].customProperty1", "Test-Input 1")
			.validate("$.[?(@.path=='${apiPath}')].customProperty2", "1")
			.validate("$.[?(@.path=='${apiPath}')].customProperty3", "true")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### 2. Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "published");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/customproperties/1_custom-properties-config.json");
		createVariable("customProperty1", "Test-Input 0815");
		createVariable("customProperty2", "2");
		createVariable("customProperty3", "false");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		// API-ID must be the same, as we changed an unpublished API!
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######");
		http().client("apiManager")
			.send()
			.get("/proxies/${apiId}")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].customProperty1", "Test-Input 0815")
			.validate("$.[?(@.id=='${apiId}')].customProperty2", "2")
			.validate("$.[?(@.id=='${apiId}')].customProperty3", "false");
		
		echo("####### 3. Re-Import with No-Change #######");
		createVariable("status", "published");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/customproperties/1_custom-properties-config.json");
		createVariable("customProperty1", "Test-Input 0815");
		createVariable("customProperty2", "2");
		createVariable("customProperty3", "false");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
		
		// Finally, Change the Custom-Prop of a published API, which will lead to a new API-ID!
		echo("####### 4. Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "published");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/customproperties/1_custom-properties-config.json");
		createVariable("customProperty1", "Test-Input Final");
		createVariable("customProperty2", "3");
		createVariable("customProperty3", "false");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######");
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
			.validate("$.[?(@.path=='${apiPath}')].customProperty1", "Test-Input Final")
			.validate("$.[?(@.path=='${apiPath}')].customProperty2", "3")
			.validate("$.[?(@.path=='${apiPath}')].customProperty3", "false");
	}
}
