package com.axway.apim.test.basic;

import java.io.IOException;

import org.springframework.http.HttpStatus;
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
import com.consol.citrus.message.MessageType;

@Test
public class UnpublishedSwaggerChangeTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		echo("####### Validates an updated Swagger-File is replicated while in 'Unpublished' state #######");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/swagger-is-changed-${apiNumber}");
		variable("apiName", "Swagger-Is-Changed-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId") // Remember the API-ID --> This is the FE-API
			.extractFromPayload("$.[?(@.path=='${apiPath}')].apiId", "beApiId")); // This is the BE-API

		echo("####### Importing a new Swagger-File as a change #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the API has changed to the updated Swagger-Definition #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId")); // We have a new API-ID
		
		echo("####### Validate the updated Swagger-File has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/discovery/swagger/api/id/${newApiId}").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.description", "@assertThat(containsString('THIS IS MY NEW API-DESCRIPTION!'))@"));
		
		echo("####### Validate the previous FE-API has been deleted #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.FORBIDDEN));
		
		echo("####### Validate the previous BE-API has been deleted #######");
		http(builder -> builder.client("apiManager").send().get("/apirepo/${beApiId}").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.FORBIDDEN));
	}
}
