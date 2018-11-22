package com.axway.apim.test.tags;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="APITagsTest")
public class APITagsTest extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "APITagsTest")
	public void setupDevOrgTest() {
		description("Verify that tags can be set for an API");
		
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
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "published");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/tags/2_tags-config.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		// API-ID must be the same, as we changed an unpublished API!
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######");
		http().client("apiManager")
			.send()
			.get("/proxies/${apiId}")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].tags.['tag-name 3'][0]", "value 123")
			.validate("$.[?(@.id=='${apiId}')].tags.['tag-name 3'][1]", "value 456")
			.validate("$.[?(@.id=='${apiId}')].tags.['tag-name 4'][0]", "value 789")
			.validate("$.[?(@.id=='${apiId}')].state", "published");
		
		// Finally, Change the Tags a published API, which will lead to a new API-ID!
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "published");
		createVariable("swaggerFile", "/com/axway/apim/test/files/security/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/tags/3_tags-config.json");
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
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.validate("$.[?(@.path=='${apiPath}')].tags.['tag-name 5'][0]", "value ABC")
			.validate("$.[?(@.path=='${apiPath}')].tags.['tag-name 6'][0]", "value DEF")
			.validate("$.[?(@.path=='${apiPath}')].tags.['tag-name 6'][1]", "value GHI")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
	}
}
