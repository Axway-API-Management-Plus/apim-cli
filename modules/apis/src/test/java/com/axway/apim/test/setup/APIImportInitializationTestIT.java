package com.axway.apim.test.setup;

import org.springframework.http.HttpStatus;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.design.TestDesignerBeforeSuiteSupport;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

public class APIImportInitializationTestIT extends TestDesignerBeforeSuiteSupport {
	
	@Override
	public void beforeSuite(TestDesigner designer) {

		designer.createVariable("orgNumber2", RandomNumberFunction.getRandomNumber(4, true));
		designer.createVariable("orgNumber3", RandomNumberFunction.getRandomNumber(4, true));
		designer.createVariable("orgName2", "API Test-Org 2 ${orgNumber2}");
		designer.createVariable("orgName3", "API Test-Org 3 ${orgNumber2}");
		
		designer.echo("####### Create Test Org 2: 'API Development ${orgNumber2}' with Non-Development flag #######");
		
		designer.http().client("apiManager")
			.send()
			.post("/organizations")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"${orgName2}\", \"description\": \"Non-Dev Org ${orgNumber2}\", \"enabled\": true, \"development\": false }");
	
		designer.echo("####### Validating Non-Development Test-Organisation 2: 'API Test-Org ${orgNumber2}' has been created #######");
		
		designer.http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "${orgName2}")
			.extractFromPayload("$.id", "orgId2");
	
		designer.echo("####### Extracted Non-Dev organization id: ${orgId2} as attribute: orgId2 #######");
		
		designer.echo("####### Create Test Org 3: 'API Development ${orgNumber3}' with Development flag #######");
		
		designer.http().client("apiManager")
			.send()
			.post("/organizations")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"${orgName3}\", \"description\": \"Dev Org ${orgNumber3}\", \"enabled\": true, \"development\": true }");
	
		designer.echo("####### Validating Development Test-Organisation3 : 'API Test-Org ${orgNumber3}' has been created #######");
		
		designer.http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "${orgName3}")
			.extractFromPayload("$.id", "orgId3");
	
		designer.echo("####### Extracted Non-Dev organization id: ${orgId3} as attribute: orgId3 #######");
		
		designer.action(new AbstractTestAction() {
            @Override public void doExecute(TestContext testContext) {
                testContext.getGlobalVariables().put("orgId2", testContext.getVariable("orgId2"));
                testContext.getGlobalVariables().put("orgId3", testContext.getVariable("orgId3"));
                testContext.getGlobalVariables().put("orgNumber2", testContext.getVariable("orgNumber2"));
                testContext.getGlobalVariables().put("orgNumber3", testContext.getVariable("orgNumber3"));
                testContext.getGlobalVariables().put("orgName2", testContext.getVariable("orgName2"));
                testContext.getGlobalVariables().put("orgName3", testContext.getVariable("orgName3"));
            }
        });
		
	}
}
