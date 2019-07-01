package com.axway.apim.test.organizations;

import java.io.IOException;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test
public class NoChangedOrgsUnpublishedAPI extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Making sure, organization are not conisdered as changes if desired state is Unpublished");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/no-change-org-unpublished-${apiNumber}");
		variable("apiName", "No-Change-Org-Unpublished-${apiNumber}");

		// Replication must fail, is Query-String option is enabled, but API-Manager hasn't configured it 
		echo("####### API-Config without queryString option - Must fail #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
		createVariable("state", "unpublished");
		createVariable("orgName", "${orgName}");
		createVariable("orgName2", "${orgName2}");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Re-Import the same - Must lead to a No-Change! #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
		createVariable("state", "unpublished");
		createVariable("orgName", "${orgName}");
		createVariable("orgName2", "${orgName2}");
		createVariable("expectedReturnCode", "10"); // No-Change is expected!
		swaggerImport.doExecute(context);
	}
}
