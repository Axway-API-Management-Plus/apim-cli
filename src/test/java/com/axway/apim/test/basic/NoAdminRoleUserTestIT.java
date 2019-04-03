package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test(testName="NoAdminRoleUserTestIT")
public class NoAdminRoleUserTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "NoAdminRoleUserTestIT")
	public void run() {
		description("Is a non-admin role user is used, the tool must fail with a dedicated return code.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-no-change-${apiNumber}");
		variable("apiName", "No-Change-${apiNumber}");

		echo("####### Calling the tool with a Non-Admin-User. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/1_no-change-config.json");
		createVariable("expectedReturnCode", "17");
		createVariable("apiManagerUser", "${oadminUsername1}"); // This is an org-admin user
		createVariable("apiManagerPass", "${oadminPassword1}");
		action(swaggerImport);
	}

}
