package com.axway.apim.test.orgadmin;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test
public class OrgAdminTriesToPublishTestIT extends TestNGCitrusTestRunner {

	@CitrusTest
	@Test @Parameters("context")
	public void allowOrgAdminsToPublishApi(@Optional @CitrusResource TestContext context) {
		ImportTestAction swaggerImport = new ImportTestAction();
		description("But OrgAdmins should not being allowed to register published APIs.");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/org-admin-published-${apiNumber}");
		variable("apiName", "OrgAdmin-Published-${apiNumber}");
		echo("####### Calling the tool with a Non-Admin-User. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/2_initially_published.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
	}
}
