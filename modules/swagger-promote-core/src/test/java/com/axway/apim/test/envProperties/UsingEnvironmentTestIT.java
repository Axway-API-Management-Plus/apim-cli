package com.axway.apim.test.envProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="UsingEnvironmentTestIT")
public class UsingEnvironmentTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "UsingEnvironmentTestIT")
	public void run() {
		description("Import an API using the API-Environment with a certain stage only.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/envrionment-stage-api-${apiNumber}");
		variable("apiName", "envrionment-stage-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable(ImportTestAction.API_DEFINITION,  "");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
		createVariable("testAPIDefinition",  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("status", "unpublished");
		createVariable("stage", "localhost");
		createVariable("useEnvironmentOnly", "true");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable(ImportTestAction.API_DEFINITION,  "");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
		createVariable("testAPIDefinition",  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable("status", "unpublished");
		createVariable("stage", "localhost");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
	}

}
