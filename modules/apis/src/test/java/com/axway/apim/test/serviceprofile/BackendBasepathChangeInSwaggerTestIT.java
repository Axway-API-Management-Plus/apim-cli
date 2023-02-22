package com.axway.apim.test.serviceprofile;

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
public class BackendBasepathChangeInSwaggerTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Import Swagger Spec without any host");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/basepath-changed-in-swagger-test-${apiNumber}");
		variable("apiName", "Basepath changed in Swagger Test ${apiNumber}");
		echo("####### Try to import an API without having the host configured at all #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore-without-any-host.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/serviceprofile/2_backend_basepath_test.json");
		createVariable("backendBasepath", "https://petstore.swagger.io");
		createVariable("apiPath", "/basepath-changed-in-swagger-test-2-${apiNumber}");
		createVariable("apiName", "Basepath changed in Swagger Test 2 ${apiNumber}");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

	}
}