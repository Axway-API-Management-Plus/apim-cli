package com.axway.apim.test.tags;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="APINoTagsChangedTest")
public class APINoTagsChangedTest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "APINoTagsChangedTest")
	public void setupDevOrgTest() {
		description("Make sure, Desired and Actual tags are compared correctly. Must lead to a No-Change");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/api-tags-test-${apiNumber}");
		variable("apiName", "API Tags Test ${apiNumber}");
		variable("status", "unpublished");
		

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "unpublished");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/tags/1_tags-config.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######");
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
			.validate("$.[?(@.path=='${apiPath}')].tags.['tag-name 2'][0]", "value 3")
			.validate("$.[?(@.path=='${apiPath}')].tags.['tag-name 2'][1]", "value 4")
			.validate("$.[?(@.path=='${apiPath}')].tags.['tag-name 1'][0]", "value 1")
			.validate("$.[?(@.path=='${apiPath}')].tags.['tag-name 1'][1]", "value 2")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Importing exactly the same API with the same tags --> No-Change: #######");
		createVariable("status", "unpublished");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/tags/1_tags-config.json");
		createVariable("expectedReturnCode", "10"); // This is the real test here!
		action(swaggerImport);
	}
}
