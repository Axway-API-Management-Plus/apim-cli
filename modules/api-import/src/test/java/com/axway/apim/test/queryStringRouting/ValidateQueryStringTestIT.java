package com.axway.apim.test.queryStringRouting;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.test.ImportTestAction;
import com.axway.lib.APIManagerConfig;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class ValidateQueryStringTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Validate query string routing can be controlled and works as expected.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/query-string-api-${apiNumber}");
		variable("apiName", "Query-String-API-${apiNumber}");
		
		// A feature that must be turned On before (and as it becomes global it must be turned off again afterwards)
		// Turn Query-Based-Routing ON
		echo("Turn Query-String feature ON in API-Manager to test it");
		http(builder -> builder.client("apiManager").send().get("/config").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON).extractFromPayload("$", "managerConfig"));
		variable("updatedConfig", APIManagerConfig.enableQueryBasedRouting(context.getVariable("managerConfig"), "abc"));
		echo("updatedConfig: ${updatedConfig}");
		http(builder -> builder.client("apiManager").send().put("/config").header("Content-Type", "application/json")
				.payload("${updatedConfig}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON));
		
		// Running tests with Query-Based Routing enabled
		// Replication must fail, is Query-String option is enabled, but API-Manager hasn't configured it 
/*		echo("####### API-Config without queryString option - Must fail #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "54"); // Must fail!
		swaggerImport.doExecute(context);*/
		
		echo("####### Register an API WITHOUT a query string enabled #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/queryStringRouting/api_without_query_string.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Register the SAME API but with query string #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		createVariable("apiRoutingKey", "routeKeyA");
		createVariable("apiName", "apiName-${apiRoutingKey}");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].state", "unpublished")
			.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey==null)].state", "published")
			.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='routeKeyA')].apiRoutingKey", "routeKeyA")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Re-Import the same API with same Routing-Key must lead to a No-Change #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "10");
		createVariable("apiRoutingKey", "routeKeyA");
		swaggerImport.doExecute(context);
		
		echo("####### Re-Import the same API with a DIFFERENT Routing-Key must lead to a NEW API #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		createVariable("apiRoutingKey", "routeKeyB");
		createVariable("apiName", "apiName-${apiRoutingKey}");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the second API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].state", "${state}")
				.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].apiRoutingKey", "routeKeyB")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId2"));
		
		echo("####### Perform a No-Change #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
		createVariable("state", "published");
		createVariable("apiRoutingKey", "routeKeyB");
		createVariable("expectedReturnCode", "10"); // No-Change
		swaggerImport.doExecute(context);
		
		// Turn Query-Based-Routing OFF
		variable("updatedConfig", APIManagerConfig.disableQueryBasedRouting(context.getVariable("managerConfig")));
		http(builder -> builder.client("apiManager").send().put("/config").header("Content-Type", "application/json")
				.payload("${updatedConfig}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON));
		
		APIManagerAdapter.deleteInstance();
		
		echo("####### Try to register an API with Query-String, but API-Manager has this option disabled #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
		createVariable("apiPath", "/query-string-api-fail-${apiNumber}");
		createVariable("apiName", "Query-String-API-Fail-${apiNumber}");
		createVariable("state", "unpublished");
		createVariable("apiRoutingKey", "routeKeyC");
		createVariable("apiName", "apiName-${apiRoutingKey}");
		createVariable("expectedReturnCode", "53"); // Must fail!
		swaggerImport.doExecute(context);
		
		echo("####### Try to register an API with Query-String (using OrgAdminOnly) - Leads to a Warning-Message #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
		createVariable("apiPath", "/query-string-api-oadmin-${apiNumber}");
		createVariable("apiName", "Query-String-API-OAdmin-${apiNumber}");
		createVariable("state", "unpublished");
		createVariable("ignoreAdminAccount", "true"); // This tests simulate to use only an Org-Admin-Account
		createVariable("apiRoutingKey", "routeKeyD");
		createVariable("apiName", "apiName-${apiRoutingKey}");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].state", "${state}")
				.validate("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].apiRoutingKey", "${apiRoutingKey}"));
	}
}
