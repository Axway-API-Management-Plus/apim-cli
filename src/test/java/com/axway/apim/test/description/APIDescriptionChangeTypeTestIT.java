package com.axway.apim.test.description;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="APIDescriptionChangeTypeTest")
public class APIDescriptionChangeTypeTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "APIDescriptionChangeTypeTest")
	public void run() {
		description("Tests, that the description type can be changed");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/description-api-${apiNumber}");
		variable("apiName", "Description-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/description/1_api_with_manual_description.json");
		createVariable("state", "published");
		createVariable("descriptionType", "manual");
		createVariable("descriptionManual", "This is my manual markdown description!");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has a manual description configured #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.validate("$.[?(@.path=='${apiPath}')].descriptionType", "manual")
			.validate("$.[?(@.path=='${apiPath}')].descriptionManual", "This is my manual markdown description!")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/description/1_api_with_url_description.json");
		createVariable("state", "published");
		createVariable("descriptionType", "url");
		createVariable("descriptionUrl", "https://any.url.com/serves/my/docu.md");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has a manual description configured - Same API-ID #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].descriptionType", "url")
			.validate("$.[?(@.id=='${apiId}')].descriptionUrl", "https://any.url.com/serves/my/docu.md");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/description/1_api_with_markdown_description.json");
		createVariable("state", "published");
		createVariable("descriptionType", "markdown");
		createVariable("descriptionMarkdown", "${//env.DOCUMENTS//}/api/docu.md");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate description is now set to markdown - Same API-ID #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].descriptionType", "markdown") 
			.validate("$.[?(@.id=='${apiId}')].descriptionMarkdown", "${//env.DOCUMENTS//}/api/docu.md");
	}

}
