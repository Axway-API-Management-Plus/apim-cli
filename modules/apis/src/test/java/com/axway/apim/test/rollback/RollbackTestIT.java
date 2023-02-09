package com.axway.apim.test.rollback;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;

@Test
public class RollbackTestIT extends TestNGCitrusTestRunner {

	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
		ImportTestAction swaggerImport = new ImportTestAction();
		description("Making sure, items are completely rolled back in case of an error.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/rollback-api-${apiNumber}");
		variable("apiName", "Rollback-API-${apiNumber}");
		
		// This error only appears on 7.7 or maybe higher 
		// Has been fixed on 7.7 SP1 (See RDAPI-16491)
		if(APIManagerAdapter.hasAPIManagerVersion("7.7") && // Only execute on 7.7 without a se5rvice
				(!APIManagerAdapter.hasAPIManagerVersion("7.7 SP1"))) {
			echo("####### Try to replicate APIs, that will fail #######");		
			createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
			createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/rollback/backendbasepath-config.json");
			createVariable("status", "published");
			createVariable("clientOrg", "${orgName2}");
			createVariable("backendBasepath", "https://unknown.host.com:443");
			createVariable("expectedReturnCode", "35"); // Can't create API-Proxy
			swaggerImport.doExecute(context);
			
			echo("####### Validate the temp. FE-API has been rolled back #######");
			http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.*.path", "@assertThat(not(containsString(${apiPath})))@"));
			
			echo("####### Validate the temp. BE-API has been rolled back #######");
			http(builder -> builder.client("apiManager").send().get("/apirepo").header("Content-Type", "application/json"));
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.*.name", "@assertThat(not(containsString(${apiName})))@"));
		}
		
		echo("####### Create a valid API, which will be updated later, which then fails and must be rolled back #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0"); // Must fail!
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
	}
}
