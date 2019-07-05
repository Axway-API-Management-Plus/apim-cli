package com.axway.apim.test.vhost;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="VhostConfigTest")
public class VhostConfigTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "VhostConfigTest")
	public void run() {
		description("Test a Request-Policy");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/vhost-test-${apiNumber}");
		variable("apiName", "VHost Test ${apiNumber}");
		variable("status", "unpublished");
		

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "unpublished");
		createVariable("vhost", "api123.customer.com");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/vhost/1_vhost-config.json");
		createVariable("expectedReturnCode", "87");
		action(swaggerImport);
		
		// Search for the API anyway!
		echo("####### Validate the API has been rolled back #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.name", "@assertThat(not(containsString(${apiName})))@");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "published");
		createVariable("vhost", "api123.customer.com");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/vhost/1_vhost-config.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		// Search for the API anyway!
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.validate("$.[?(@.path=='${apiPath}')].vhost", "api123.customer.com");
	}
}
