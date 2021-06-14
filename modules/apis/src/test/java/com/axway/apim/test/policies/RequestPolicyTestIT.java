package com.axway.apim.test.policies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="RequestPolicyTest")
public class RequestPolicyTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "RequestPolicyTest")
	public void run() {
		description("Test a Request-Policy");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/request-policy-test-${apiNumber}");
		variable("apiName", "Request Policy Test ${apiNumber}");
		variable("status", "unpublished");
		
		echo("####### Initially add API: '${apiName}' on path: '${apiPath}' without any custom policy #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
		createVariable(ImportTestAction.STATE,  "unpublished");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has imported with correct settings #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");

		echo("####### Re-Importing API: '${apiName}' on path: '${apiPath}' including a Request-Policy #######");
		createVariable("requestPolicy", "Request policy 1");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/policies/1_request-policy.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######");
		http().client("apiManager").send().get("/proxies/${apiId}").name("api").header("Content-Type", "application/json");

		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.validate("$.[?(@.path=='${apiPath}')].outboundProfiles._default.requestPolicy", "@assertThat(containsString(Request policy 1))@");
	}
}
