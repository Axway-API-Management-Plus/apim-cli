package com.axway.apim.test.basic;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;

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
		
		http().client("apiManager")
			.send()
			.post("/organizations")
			.header("Content-Type", "application/json")
			.payload("{'name': 'Test Organisation', 'description': 'Test Org', 'enabled': true, 'development': true }");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED);
	}

}
