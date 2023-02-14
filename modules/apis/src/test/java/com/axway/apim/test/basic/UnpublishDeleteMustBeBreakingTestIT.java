package com.axway.apim.test.basic;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;

@Test
public class UnpublishDeleteMustBeBreakingTestIT extends TestNGCitrusTestRunner {

	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
		ImportTestAction swaggerImport = new ImportTestAction();
		echo("####### This test makes sure, once an API is published, unpublishing or deleting it requires a force #######");
		variable("useApiAdmin", "true"); // Use apiadmin account
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/check-is-breaking-${apiNumber}");
		variable("apiName", "Check-is-Breaking-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' as Published #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "published");
		createVariable("version", "1.0.0");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate unpublishing it, will fail, with the need to enforce it #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "unpublished");
		createVariable("enforce", "false");
		createVariable("expectedReturnCode", "15");
		swaggerImport.doExecute(context);
		
		echo("####### Validate deleting it, will fail, with the need to enforce it #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "deleted");
		createVariable("expectedReturnCode", "15");
		swaggerImport.doExecute(context);
	}
}
