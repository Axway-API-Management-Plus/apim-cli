package com.axway.apim.test.policies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="AllOutboundPoliciesTest")
public class AllOutboundPoliciesTest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "AllOutboundPoliciesTest")
	public void setupDevOrgTest() {
		description("Test a Request-Policy");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/api-key-test-${apiNumber}");
		variable("apiName", "API Key Test ${apiNumber}");
		variable("status", "unpublished");
		

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("requestPolicy", "Request policy 1");
		createVariable("responsePolicy", "Response policy 1");
		createVariable("routePolicy", "Routing policy 1");
		createVariable("faultHandlerPolicy", "Faulthandler policy 1");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/policies/1_all-policies.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######");
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
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.validate("$.[?(@.path=='${apiPath}')].outboundProfiles._default.requestPolicy", "@assertThat(containsString(Request policy 1))@")
			.validate("$.[?(@.path=='${apiPath}')].outboundProfiles._default.responsePolicy", "@assertThat(containsString(Response policy 1))@")
			.validate("$.[?(@.path=='${apiPath}')].outboundProfiles._default.routePolicy", "@assertThat(containsString(Routing policy 1))@")
			.validate("$.[?(@.path=='${apiPath}')].outboundProfiles._default.faultHandlerPolicy", "@assertThat(containsString(Faulthandler policy 1))@")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
	}
}
