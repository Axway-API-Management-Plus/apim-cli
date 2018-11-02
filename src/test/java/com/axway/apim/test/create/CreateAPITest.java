package com.axway.apim.test.create;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class CreateAPITest extends TestNGCitrusTestDesigner {
	
	//@CitrusTest(name = "Import API")
	public void setupDevOrgTest() {
		description("Import complete new API!");
		
		variable("orgNumber", RandomNumberFunction.getRandomNumber(2, true));
		
		String[] mainArgs = new String[] {"-a", "/api_definition_1/petstore.json", "-c", "/api_definition_1/apim-config.json", "-h", "api-env", "-p", "changeme", "-u", "apiadmin", "-f", "true"};
		java("com.axway.apim.App").method("main").methodArgs(mainArgs);
		
		http().client("apiManager")
			.send()
			.post("/organizations")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"Test Organisation ${orgNumber}\", \"description\": \"Test Org\", \"enabled\": true, \"development\": true }");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "Test Organisation ${orgNumber}");
		
		/* Can be used to show the response in case of errors!
		receive("apiManager")
			.name("response")
		*/
		
		//echo("citrus:message(response.payload(), )");
	}

}
