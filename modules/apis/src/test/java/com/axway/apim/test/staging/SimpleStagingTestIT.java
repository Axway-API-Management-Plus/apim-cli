package com.axway.apim.test.staging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="SimpleStagingTest")
public class SimpleStagingTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "SimpleStagingTest")
	public void run() {
		description("Import the API with production stage settings");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-stage-test-${apiNumber}");
		variable("apiName", "Stage-Test-${apiNumber}");
		
		echo("####### Must fail, as the base organization wrong #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/staging/1_no-change-config.json");
		createVariable("expectedReturnCode", "57"); // 57 is expected - Base org is wrong
		action(swaggerImport);
		
		echo("####### Must fail, as the organization is invalid in base- and stage-config #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/staging/1_no-change-config.json");
		createVariable("stage", "wrongOrg"); // << Will map to a config having an invalid organization
		createVariable("expectedReturnCode", "57"); // 57 is expected - Invalid organization in staged config
		action(swaggerImport);

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' on stage prod #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/staging/1_no-change-config.json");
		createVariable("stage", "prod"); // << Program will search for file: 1_no-change-config.prod.json
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);

		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published") // State must be published in "prod"
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
	}

}
