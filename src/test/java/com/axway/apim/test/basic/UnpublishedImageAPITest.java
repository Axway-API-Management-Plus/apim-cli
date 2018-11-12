package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName = "UnpublishedImageAPITest")
public class UnpublishedImageAPITest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "UnpublishedImageAPITest")
	public void setupDevOrgTest() {
		description("Import an API including an image!");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-image-api-${apiNumber}");
		variable("apiName", "My-Image-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/basic/3_5_unpublished-image.json");
		createVariable("image", "/com/axway/apim/test/files/basic/API-Logo.jpg");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
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
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.validate("$.[?(@.path=='${apiPath}')].image", "@assertThat(containsString(/image))@") // Just checking there is at least an image
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
	}
}
