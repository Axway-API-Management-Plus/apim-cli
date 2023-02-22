package com.axway.apim.test.methodLevel;

import java.io.IOException;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test
public class InboundMethodLevelInvalidOperationTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Make sure, the error that an invalid operationId is given is properly handled.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/basic-method-level-api-${apiNumber}");
		variable("apiName", "Basic Method-Level-API-${apiNumber}");
		
		echo("####### Try to replicate an API having Method-Level settings declared #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-inbound-invalidOperation.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "72");
		createVariable("securityProfileName", "APIKeyBased${apiNumber}");
		swaggerImport.doExecute(context);
	}
}
