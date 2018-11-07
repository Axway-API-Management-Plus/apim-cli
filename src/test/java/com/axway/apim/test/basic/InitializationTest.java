package com.axway.apim.test.basic;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class InitializationTest extends TestNGCitrusTestDesigner {
	
	@CitrusTest(name = "HealthCheck")
	public void initTest() {
		description("Do a quick healthcheck");
		
		http().client("gatewayPlain")
			.send()
			.get("/healthcheck");

		http().client("gatewayPlain")
			.receive()
			.response(HttpStatus.OK)
			.payload("<status>ok</status>");
	}
	
	@CitrusTest(name = "Setup Test-Org")
	public void setupDevOrgTest() {
		description("Setup Test Org");
		
		variable("orgNumber", RandomNumberFunction.getRandomNumber(3, true));
		
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
