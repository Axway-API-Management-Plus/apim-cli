package com.axway.apim.test.setup;

import org.springframework.http.HttpStatus;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.design.TestDesignerBeforeSuiteSupport;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

public class InitializationTest extends TestDesignerBeforeSuiteSupport {
	
	@Override
	public void beforeSuite(TestDesigner designer) {
		designer.echo("Do a quick healthcheck");
		
		designer.http().client("gatewayPlain")
			.send()
			.get("/healthcheck");
		
		designer.http().client("gatewayPlain")
			.receive()
			.response(HttpStatus.OK)
			.payload("<status>ok</status>");

		designer.createVariable("orgNumber", RandomNumberFunction.getRandomNumber(3, true));
		
		designer.echo("Setup Test Org: 'API Development ${orgNumber}'");
		
		designer.http().client("apiManager")
			.send()
			.post("/organizations")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"API Development ${orgNumber}\", \"description\": \"Test Org ${orgNumber}\", \"enabled\": true, \"development\": true }");
		
		designer.echo("Validating Test-Organisation: API Development ${orgNumber} has been created");
		
		designer.http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "API Development ${orgNumber}")
			.extractFromPayload("$.id", "orgId");
		
		designer.echo("Extracted operation from header is: ${orgId}");

		designer.echo("Create a test application");
		
		designer.http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"Test App ${orgNumber}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");
	
		designer.http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "testAppId")
			.extractFromPayload("$.name", "testAppName");
		
		designer.action(new AbstractTestAction() {
            @Override public void doExecute(TestContext testContext) {
                testContext.getGlobalVariables().put("testAppId", testContext.getVariable("testAppId"));
                testContext.getGlobalVariables().put("testAppName", testContext.getVariable("testAppName"));
                testContext.getGlobalVariables().put("orgNumber", testContext.getVariable("orgNumber"));
            }
        });
		
	}
}
