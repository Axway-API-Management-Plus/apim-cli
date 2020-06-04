package com.axway.apim.test.rollback;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class RollbackTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Making sure, items are completely rolled back in case of an error.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/rollback-api-${apiNumber}");
		variable("apiName", "Rollback-API-${apiNumber}");
		
		// This error only appears on 7.7 or maybe higher 
		// Has been fixed on 7.7 SP1 (See RDAPI-16491)
		if(APIManagerAdapter.hasAPIManagerVersion("7.7") && // Only execute on 7.7 without a se5rvice
				(!APIManagerAdapter.hasAPIManagerVersion("7.7 SP1") || !APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP5"))) {
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
		createVariable("status", "published");
		createVariable("expectedReturnCode", "0"); // Must fail!
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${status}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		// In Version 7.6.2 SP2 (only this version) the API-Manager is able to create a FE-API based on host: https://unknown.host.com:443 for any reason
		// found no other way to force API-Manager to fail on initial FE-API creation!
		// Execute this test only on higher version for now
		if(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3") && 
				(!APIManagerAdapter.hasAPIManagerVersion("7.7 SP1") || !APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP5"))) {
			echo("####### This will re-create the API, but it fails #######");		
			createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
			createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/rollback/backendbasepath-config.json");
			createVariable("status", "published");
			createVariable("backendBasepath", "https://unknown.host.com:443");
			createVariable("expectedReturnCode", "35"); // Must fail!
			createVariable("enforce", "true"); // Must be enforced, as it's a breaking change
			swaggerImport.doExecute(context);
			
			echo("####### Validate the original API is still there #######");
			http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON));
			
			echo("####### Validate the replicate try has been rolled back #######");
			echo("####### Validate the temp. FE-API has been rolled back #######");
			http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.name=='${apiName}')].id", "@assertThat(hasSize(1))@")); // Only the original API is there
			
			echo("####### Validate the temp. BE-API has been rolled back #######");
			http(builder -> builder.client("apiManager").send().get("/apirepo").header("Content-Type", "application/json"));
			if(APIManagerAdapter.hasAPIManagerVersion("7.7")) {
				http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
					.validate("$.[?(@.name=='${apiName} HTTP')].id", "@assertThat(hasSize(1))@") // Only the original API is there
					.validate("$.[?(@.name=='${apiName} HTTPS')].id", "@assertThat(hasSize(1))@")); // Only the original API is there
			} else {
				http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
						.validate("$.[?(@.name=='${apiName}')].id", "@assertThat(hasSize(1))@")); // Only the original API is there			
			}
		}
	}
}
