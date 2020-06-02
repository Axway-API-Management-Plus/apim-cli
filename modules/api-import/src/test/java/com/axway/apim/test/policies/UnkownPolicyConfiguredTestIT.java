package com.axway.apim.test.policies;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test(testName="UnkownPolicyConfiguredTest")
public class UnkownPolicyConfiguredTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "UnkownPolicyConfiguredTest")
	public void run() {
		description("A dedicated return-code is expected, when an unknown policy is configured.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/unknown-policy-test-${apiNumber}");
		variable("apiName", "Unknown Policy Test ${apiNumber}");
		variable("status", "unpublished");
		

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("requestPolicy", "Brand new policy!");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/policies/1_request-policy.json");
		createVariable("expectedReturnCode", "85");
		action(swaggerImport);
	}
}
