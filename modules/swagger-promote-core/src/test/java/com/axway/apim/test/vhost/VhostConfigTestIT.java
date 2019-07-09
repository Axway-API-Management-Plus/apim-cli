package com.axway.apim.test.vhost;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class VhostConfigTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Validate VHosts are handled correctly");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/vhost-test-${apiNumber}");
		variable("apiName", "VHost Test ${apiNumber}");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "published");
		createVariable("vhost", "api123.customer.com");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/vhost/1_vhost-config.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.validate("$.[?(@.path=='${apiPath}')].vhost", "api123.customer.com"));

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("status", "unpublished");
		createVariable("vhost", "api123.customer.com");
		createVariable("enforce", "true"); // as we are going back from published to unpublished
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/vhost/1_vhost-config.json");
		if(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3")) { // Starting from version 7.6.2 SP3 it is possible to set a VHost also for unpublished APIs
			createVariable("expectedReturnCode", "0");
		} else {
			createVariable("expectedReturnCode", "87");
		}
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported and VHost is set #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));
		if(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3")) {
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "${status}")
				.validate("$.[?(@.path=='${apiPath}')].vhost", "${vhost}")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		} else {
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
					.validate("$.*.name", "@assertThat(not(containsString(${apiName})))@"));
		}
	}
}
