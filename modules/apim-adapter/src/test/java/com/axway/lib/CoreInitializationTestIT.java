package com.axway.lib;

import com.consol.citrus.dsl.design.TestDesignerBeforeTestSupport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.design.TestDesignerBeforeSuiteSupport;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

public class CoreInitializationTestIT extends TestDesignerBeforeTestSupport {
	
//	@Override
	public void beforeSuite(TestDesigner designer) {
		
		designer.echo("Do a quick healthcheck");
		
		designer.http().client("gatewayPlain")
			.send()
			.get("/healthcheck");
		
		designer.http().client("gatewayPlain")
			.receive()
			.response(HttpStatus.OK)
			.payload("<status>ok</status>");
		
		designer.createVariable("orgNumber", RandomNumberFunction.getRandomNumber(4, true));
		designer.createVariable("orgName", "API Development ${orgNumber}");
		
		designer.echo("####### Setup Test Org: 'API Development ${orgNumber}' #######");
		designer.http().client("apiManager")
			.send()
			.post("/organizations")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"${orgName}\", \"description\": \"Test Org ${orgNumber}\", \"enabled\": true, \"development\": true }");
		
		designer.echo("####### Validate Test-Organisation: ${orgName} has been created #######");
		
		designer.http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "${orgName}")
			.extractFromPayload("$.id", "orgId");
		
		designer.echo("####### Extracted organization id: ${orgId} as attribute: orgId #######");
		
		designer.echo("####### Create an Org-Admin-User #######");
		designer.createVariable("oadminUsername1", "orgadmin-${orgNumber}");
		designer.createVariable("oadminPassword1", "orgadmin-${orgNumber}");
		
		designer.http().client("apiManager")
			.send()
			.post("/users")
			.header("Content-Type", "application/json")
			.payload("{\"enabled\":true,\"loginName\":\"${oadminUsername1}\",\"name\":\"Anna Owen ${orgNumber}\",\"email\":\"anna-${orgNumber}@axway.com\",\"role\":\"oadmin\",\"organizationId\":\"${orgId}\"}");
		
		designer.http().client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
			.extractFromPayload("$.id", "oadminUserId1")
			.extractFromPayload("$.loginName", "oadminUsername1");
		
		designer.http().client("apiManager").send()
			.post("/users/${oadminUserId1}/changepassword/")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.payload("newPassword=${oadminPassword1}");
		
		designer.http().client("apiManager").receive().response(HttpStatus.NO_CONTENT);
		
		designer.echo("####### Created a Org-Admin user: '${oadminUsername1}' ID: '${oadminUserId1}' #######");
		
		designer.echo("####### Create a test application #######");
		
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
		
		designer.echo("####### Created a application: '${testAppName}' ID: '${testAppId}' (testAppName/testAppId) #######");
		
		// Adjusting the API-Manager config in preparation to run integration tests
		designer.echo("Turn off changePasswordOnFirstLogin and passwordExpiryEnabled validation to run integration tests");
		designer.http().client("apiManager").send().put("/config").header("Content-Type", "application/json")
			.payload(new ClassPathResource("/apimanager/config/apimanager-test-config.json"));

//		designer.setTestContext();
//
//		designer.action(new AbstractTestAction() {
//	        @Override public void doExecute(TestContext testContext) {
//	            testContext.getGlobalVariables().put("orgId", testContext.getVariable("orgId"));
//	            testContext.getGlobalVariables().put("orgNumber", testContext.getVariable("orgNumber"));
//	            testContext.getGlobalVariables().put("orgName", testContext.getVariable("orgName"));
//	            testContext.getGlobalVariables().put("oadminUserId1", testContext.getVariable("oadminUserId1"));
//	            testContext.getGlobalVariables().put("oadminUsername1", testContext.getVariable("oadminUsername1"));
//	            testContext.getGlobalVariables().put("oadminPassword1", testContext.getVariable("oadminPassword1"));
//                testContext.getGlobalVariables().put("testAppId", testContext.getVariable("testAppId"));
//                testContext.getGlobalVariables().put("testAppName", testContext.getVariable("testAppName"));
//	        }
//	    });
	
	}

	@Override
	public void beforeTest(TestDesigner designer) {

		designer.createVariable("orgNumber", RandomNumberFunction.getRandomNumber(4, true));
		designer.createVariable("orgName", "API Development ${orgNumber}");

		designer.echo("####### Setup Test Org: 'API Development ${orgNumber}' #######");
		designer.http().client("apiManager")
				.send()
				.post("/organizations")
				.name("orgCreatedRequest")
				.header("Content-Type", "application/json")
				.payload("{\"name\": \"${orgName}\", \"description\": \"Test Org ${orgNumber}\", \"enabled\": true, \"development\": true }");

		designer.echo("####### Validate Test-Organisation: ${orgName} has been created #######");

		designer.http().client("apiManager")
				.receive()
				.response(HttpStatus.CREATED)
				.messageType(MessageType.JSON)
				.validate("$.name", "${orgName}")
				.extractFromPayload("$.id", "orgId");

		designer.echo("####### Extracted organization id: ${orgId} as attribute: orgId #######");

		designer.echo("####### Create an Org-Admin-User #######");
		designer.createVariable("oadminUsername1", "orgadmin-${orgNumber}");
		designer.createVariable("oadminPassword1", "orgadmin-${orgNumber}");

		designer.http().client("apiManager")
				.send()
				.post("/users")
				.header("Content-Type", "application/json")
				.payload("{\"enabled\":true,\"loginName\":\"${oadminUsername1}\",\"name\":\"Anna Owen ${orgNumber}\",\"email\":\"anna-${orgNumber}@axway.com\",\"role\":\"oadmin\",\"organizationId\":\"${orgId}\"}");

		designer.http().client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.extractFromPayload("$.id", "oadminUserId1")
				.extractFromPayload("$.loginName", "oadminUsername1");

		designer.http().client("apiManager").send()
				.post("/users/${oadminUserId1}/changepassword/")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.payload("newPassword=${oadminPassword1}");

		designer.http().client("apiManager").receive().response(HttpStatus.NO_CONTENT);

		designer.echo("####### Created a Org-Admin user: '${oadminUsername1}' ID: '${oadminUserId1}' #######");

		designer.echo("####### Create a test application #######");

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

		designer.echo("####### Created a application: '${testAppName}' ID: '${testAppId}' (testAppName/testAppId) #######");

		// Adjusting the API-Manager config in preparation to run integration tests
		designer.echo("Turn off changePasswordOnFirstLogin and passwordExpiryEnabled validation to run integration tests");
		designer.http().client("apiManager").send().put("/config").header("Content-Type", "application/json")
				.payload(new ClassPathResource("/apimanager/config/apimanager-test-config.json"));
	}
}
