package com.axway.apim.test.serviceprofile;

import java.io.IOException;

import org.springframework.http.HttpStatus;
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
import com.consol.citrus.message.MessageType;

@Test
public class BackendBasepathChangedTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Import the API with a different backend-Base-Path then declared in the Swagger");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/basepath-test-${apiNumber}");
		variable("apiName", "Basepath Test ${apiNumber}");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/serviceprofile/2_backend_basepath_test.json");
		createVariable("backendBasepath", "https://swapi.co:443");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/serviceprofile/2_backend_basepath_test.json");
		createVariable("backendBasepath", "https://swapi.co:443");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);

		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has the given Base-Path configured. #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
				.validate("$.[?(@.path=='${apiPath}')].serviceProfiles._default.basePath", "https://swapi.co")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Change API to status published: #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/serviceprofile/2_backend_basepath_test.json");
		createVariable("backendBasepath", "https://swapi.co");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has status published. #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
				.validate("$.[?(@.id=='${apiId}')].state", "published")
				.validate("$.[?(@.path=='${apiPath}')].serviceProfiles._default.basePath", "${backendBasepath}"));
	}
}
