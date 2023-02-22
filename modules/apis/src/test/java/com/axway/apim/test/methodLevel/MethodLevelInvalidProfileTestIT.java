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
public class MethodLevelInvalidProfileTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void runInboundProfileValidation(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Make sure only valid profile names are referenced");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/invalid-sec-profile-api-${apiNumber}");
		variable("apiName", "Invalid-SecProfile-API-${apiNumber}");
		
		echo("####### Try to replicate an API having invalid profiles referenced #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-inbound-invalidProfileRefercence.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "73");
		createVariable("securityProfileName1", "APIKeyBased${apiNumber}");
		createVariable("securityProfileName2", "SomethingWrong${apiNumber}");
		swaggerImport.doExecute(context);
	}
	
	@CitrusTest
	@Test @Parameters("context")
	public void runOutboundProfileValidation(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Make sure only valid profile names are referenced");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/invalid-authn-profile-api-${apiNumber}");
		variable("apiName", "Invalid AuthN-Profile-API-${apiNumber}");
		
		echo("####### Try to replicate an API having invalid profiles referenced #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-outboundbound-invalidProfileReference.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "73");
		createVariable("authenticationProfileName1", "HTTP Basic");
		createVariable("authenticationProfileName2", "SomethingWrong");
		swaggerImport.doExecute(context);
	}
	
	@CitrusTest
	@Test @Parameters("context")
	public void runInboundCorsProfileValidation(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Is the CORS-Profile not know - Error must be handled");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/basic-method-level-api-${apiNumber}");
		variable("apiName", "Basic Method-Level-API-${apiNumber}");
		
		echo("####### Try to replicate an API having invalid profiles referenced #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-inbound-invalidCorsProfileRefercence.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "73");
		swaggerImport.doExecute(context);
	}
}
