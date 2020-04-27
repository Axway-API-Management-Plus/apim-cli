package com.axway.apim.test.quota;

import java.io.IOException;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test
public class InvalidQuotaConfigTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Try to import an API with invalid quota configuration.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/invalid-quota-api-${apiNumber}");
		variable("apiName", "Invalid Quota-API-${apiNumber}");

		echo("####### Trying to import API: '${apiName}' on path: '${apiPath}' with invalid quota config #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/issue-109-invalid-quota-config-1.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "71");
		swaggerImport.doExecute(context);
		
		echo("####### Trying to import API: '${apiName}' on path: '${apiPath}' with invalid quota config #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/issue-109-invalid-quota-config-2.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "71");
		swaggerImport.doExecute(context);
	}
}
