package com.axway.apim.test.staging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="NestedPropertyStagingTest")
public class NestedPropertyStagingTest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "NestedPropertyStagingTest")
	public void setupDevOrgTest() {
		description("Make sure nested properties can be staged as well!");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-stage-test-${apiNumber}");
		variable("apiName", "Stage-Test-${apiNumber}");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/staging/1_no-change-config.json");
		createVariable("stage", "prod"); // << Program will search for file: 1_no-change-config.prod.json
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);

		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
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
			.validate("$.[?(@.path=='${apiPath}')].state", "published") // State must be published in "prod"
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
	}

}
