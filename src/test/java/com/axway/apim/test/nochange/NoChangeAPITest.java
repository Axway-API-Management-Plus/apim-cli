package com.axway.apim.test.nochange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class NoChangeAPITest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "Re-Import API with No-Change.")
	public void setupDevOrgTest() {
		description("Import an API and re-import it without any change. It must be detected, that no change happened.");
		
		variable("apiNnumber", RandomNumberFunction.getRandomNumber(2, true));
		variable("apiPath", "/no-change-${apiNnumber}");
		variable("apiName", "No-Change-${apiNnumber}");

		
		echo("##### Importing API: '${apiName}' on path: '${apiPath}' for the first time");
		createVariable("swaggerFile", "/com/axway/apim/test/basic/nochange/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/basic/nochange/no-change-config.json");
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
			.extractFromPayload("$.[?(@.path=='/no-change')].id", "apiId");

		echo("##### RE-Importing same API: '${apiName}' on path: '${apiPath}' without changes. Expecting failure with RC 99.");
		createVariable("swaggerFile", "/com/axway/apim/test/basic/nochange/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/basic/nochange/no-change-config.json");
		createVariable("expectedReturnCode", "99");
		action(swaggerImport);
		
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		// Check the API is still exposed on the same path
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='/no-change')].id", "${apiId}"); // Must be the same API-ID as before!
		
		//echo("citrus:message(response.payload(), )");
	}

}
