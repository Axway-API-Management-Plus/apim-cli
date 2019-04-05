package com.axway.apim.test.orgadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test(testName="OrgAdminTriesToPublishTestIT")
public class OrgAdminCustomPoliciesTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "OrgAdminTriesToPublishTestIT")
	public void run() {
		description("OrgAdmin wants to use a custom policy.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/org-admin-published-${apiNumber}");
		variable("apiName", "OrgAdmin-Published-${apiNumber}");

		echo("####### Calling the tool with a Non-Admin-User. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/policies/1_request-policy.json");
		createVariable("requestPolicy", "Request policy 1");
		createVariable("expectedReturnCode", "0");
		createVariable("apiManagerUser", "${oadminUsername1}"); // This is an org-admin user
		createVariable("apiManagerPass", "${oadminPassword1}");
		createVariable("ignoreAdminAccount", "true"); // This tests simulate to use only an Org-Admin-Account
		action(swaggerImport);
	}

}
