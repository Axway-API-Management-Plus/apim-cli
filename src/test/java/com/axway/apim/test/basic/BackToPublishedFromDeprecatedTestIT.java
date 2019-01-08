package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="BackToPublishedFromDeprecatedTest")
public class BackToPublishedFromDeprecatedTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "BackToPublishedFromDeprecatedTest")
	public void setupDevOrgTest() {
		description("Once an API in status deprecated, it must be possible to go back to published, basically remove the deperation status");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-test-api-${apiNumber}");
		variable("apiName", "My-Test-API-${apiNumber}");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("status", "published");
		createVariable("configFile", "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with Status Published #######");
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
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Setting API: '${apiName}' on path: '${apiPath}' to deprecated #######");		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("status", "deprecated");
		createVariable("configFile", "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has Status Deprecated #######");
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
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].deprecated", "true");
		
		echo("####### Going back to status Published  for API: '${apiName}' on path: '${apiPath}' #######");		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("status", "published");
		createVariable("configFile", "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' hasn't anymore deprecation #######");
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
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].deprecated", "false");
	}
}
