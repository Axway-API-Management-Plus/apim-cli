package com.axway.apim.test.setup;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.message.MessageType;

@Test(groups = {"init-tests"})
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
		
		http().client("apiManager")
			.send()
			.post("/organizations")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"API Development ${orgNumber}\", \"description\": \"Test Org ${orgNumber} ${orgNumber} ${orgNumber}\", \"enabled\": true, \"development\": true }");
		
		echo("Validating Test-Organisation: API Development ${orgNumber} has been created");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "API Development ${orgNumber}");
	}

}
