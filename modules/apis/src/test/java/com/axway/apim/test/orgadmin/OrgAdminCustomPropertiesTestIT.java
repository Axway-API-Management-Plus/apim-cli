package com.axway.apim.test.orgadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test(testName="OrgAdminTriesToPublishTestIT")
public class OrgAdminCustomPropertiesTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "OrgAdminTriesToPublishTestIT")
	public void run() {
		description("OrgAdmin wants to use a custom policy.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/org-admin-published-${apiNumber}");
		variable("apiName", "OrgAdmin-Published-${apiNumber}");
		variable("ignoreAdminAccount", "true"); // This tests simulate to use only an Org-Admin-Account

		echo("####### Calling the tool with a Non-Admin-User. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/customproperties/1_custom-properties-config.json");
		createVariable("status", "unpublished");
		createVariable("customProperty1", "Test-Input 1");
		createVariable("customProperty2", "1");
		createVariable("customProperty3", "true");
		createVariable("expectedReturnCode", "0");
		createVariable("apiManagerUser", "${oadminUsername1}"); // This is an org-admin user
		createVariable("apiManagerPass", "${oadminPassword1}");
		action(swaggerImport);
	}

}
