package com.axway.apim.test.description;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class LocalMarkdownDescriptionTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	public void importAPIWithLocalMarkdown() {
		description("Import an API with a local markdown file");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/localmarkdown-api-${apiNumber}");
		variable("apiName", "LocalMarkDown-API-${apiNumber}");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/description/1_api_with_local_mark_down.json");
		createVariable("state", "unpublished");
		createVariable("descriptionType", "markdownLocal");
		createVariable("markdownLocal", "MyLocalMarkdown.md");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has a description based on given local markdown file #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.validate("$.[?(@.path=='${apiPath}')].descriptionType", "manual")
			.validate("$.[?(@.path=='${apiPath}')].descriptionManual", "THIS IS THE API-DESCRIPTION FROM A LOCAL MARKDOWN!")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
	}

}
