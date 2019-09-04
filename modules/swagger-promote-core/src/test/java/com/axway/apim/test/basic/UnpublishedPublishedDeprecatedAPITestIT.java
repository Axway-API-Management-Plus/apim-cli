package com.axway.apim.test.basic;

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
public class UnpublishedPublishedDeprecatedAPITestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Import an Unpublished-API, then publish it and finally deprecate it.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-test-api-${apiNumber}");
		variable("apiName", "My-Test-API-${apiNumber}");

		
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
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Change API-State to published #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API-State has been changed to published, which is not a breaking change #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.validate("$.[?(@.path=='${apiPath}')].id", "${apiId}"));
		
		echo("####### Change API-State to deprecated #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "deprecated");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API-State has been changed to deprecated, which is not a breaking change  #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.validate("$.[?(@.path=='${apiPath}')].id", "${apiId}")
			.validate("$.[?(@.path=='${apiPath}')].deprecated", "true"));
	}

}
