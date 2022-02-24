package com.axway.apim.test.description;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="APIDescriptionBasicTest")
public class APIDescriptionBasicTestIT extends TestNGCitrusTestRunner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "APIDescriptionBasicTest")
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) {
		description("Import an API with manual description first!");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/description-api-${apiNumber}");
		variable("apiName", "Description-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/description/1_api_with_manual_description.json");
		createVariable("state", "published");
		createVariable("descriptionType", "manual");
		createVariable("descriptionManual", "This is my markdown description test!");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a manual description configured #######");
		http(builder -> builder.client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.validate("$.[?(@.path=='${apiPath}')].descriptionType", "manual")
			.validate("$.[?(@.path=='${apiPath}')].descriptionManual", "This is my markdown description test!")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/description/1_api_with_manual_description.json");
		createVariable("state", "published");
		createVariable("descriptionType", "manual");
		createVariable("descriptionManual", "This is my markdown description test slightly updated!");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a manual description configured - Same API-ID #######");
		http(builder -> builder.client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].descriptionType", "manual")
			.validate("$.[?(@.id=='${apiId}')].descriptionManual", "This is my markdown description test slightly updated!"));
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/description/1_api_with_manual_description.json");
		createVariable("state", "published");
		createVariable("descriptionType", "original");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate description is back to original - Same API-ID #######");
		http(builder -> builder.client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].descriptionType", "original"));
	}

}
