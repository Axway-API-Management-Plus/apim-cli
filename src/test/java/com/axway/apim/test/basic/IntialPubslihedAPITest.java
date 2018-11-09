package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(dependsOnGroups = {"init-tests"})
public class IntialPubslihedAPITest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "Initial Pusblished API.")
	public void setupDevOrgTest() {
		description("Import an API which initially has the status published.");
		
		variable("apiNnumber", RandomNumberFunction.getRandomNumber(2, true));
		variable("apiPath", "/initially-published-${apiNnumber}");
		variable("apiName", "Initially-Published-API-${apiNnumber}");

		
		echo("##### Importing API: '${apiName}' on path: '${apiPath}' for the first time");
		createVariable("swaggerFile", "/com/axway/apim/test/files/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/initially_published.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
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
			.extractFromPayload("$.[?(@.path=='/${apiPath}')].id", "apiId");
		
		//echo("citrus:message(response.payload(), )");
	}

}
