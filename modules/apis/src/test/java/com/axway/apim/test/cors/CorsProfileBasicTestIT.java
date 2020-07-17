package com.axway.apim.test.cors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="CorsProfileBasicTest")
public class CorsProfileBasicTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "CorsProfileBasicTest")
	public void run() {
		description("Importing & validating CORS-Profile");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/api-cors-profile-test-${apiNumber}");
		variable("apiName", "API CORS-Profile Test ${apiNumber}");
		variable("status", "unpublished");
		

		echo("####### 1. Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "unpublished");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/cors/1_api-with_default_cors.json");
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
			.validate("$.[?(@.path=='${apiPath}')].corsProfiles.[?(@.name=='New CORS Profile')].allowedHeaders[0]", "Authorization")
			.validate("$.[?(@.path=='${apiPath}')].corsProfiles.[?(@.name=='New CORS Profile')].exposedHeaders[0]", "via")
			.validate("$.[?(@.path=='${apiPath}')].inboundProfiles._default.corsProfile", "New CORS Profile")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
	}
}
