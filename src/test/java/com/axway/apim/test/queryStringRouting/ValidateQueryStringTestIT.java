package com.axway.apim.test.queryStringRouting;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.axway.apim.test.lib.APIManagerConfig;
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
	public void run(@Optional @CitrusResource TestContext context) {
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
		http(builder -> builder.client("apiManager").send().put("/config").header("Content-Type", "application/json")
				.payload("${updatedConfig}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON));
		
		// Running tests with Query-Based Routing enabled
		// queryStringRouting.api_with_query_string.json
		echo("####### Register an API with query string enabled #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
		createVariable("state", "unpublished");
		createVariable("apiRoutingKey", "routeKeyA");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.validate("$.[?(@.path=='${apiPath}')].apiRoutingKey", "${apiRoutingKey}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
		createVariable("state", "published");
		createVariable("apiRoutingKey", "routeKeyB");
		swaggerImport.doExecute(context);
		
		echo("####### Has the routing key changed #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").name("api").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "${state}") // should be published now!
			.validate("$.[?(@.id=='${apiId}')].apiRoutingKey", "${apiRoutingKey}")); // Changed to routeKeyB
		
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
	}
}
