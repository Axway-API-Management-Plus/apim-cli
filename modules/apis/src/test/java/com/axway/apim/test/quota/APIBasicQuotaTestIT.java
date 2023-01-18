package com.axway.apim.test.quota;

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
public class APIBasicQuotaTestIT extends TestNGCitrusTestRunner {

	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, InterruptedException {
		ImportTestAction swaggerImport = new ImportTestAction();
		description("Import an API containing a quota definition");
		variable("useApiAdmin", "true");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/quota-api-${apiNumber}");
		variable("apiName", "Quota-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		createVariable("applicationPeriod", "hour");
		createVariable("applicationMb", "555");
		createVariable("systemPeriod", "day");
		createVariable("systemMessages", "666");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Check System-Quotas have been setup as configured #######");
		echo("####### ############ Sleep 2 seconds ##################### #######");
		Thread.sleep(2000);
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000000").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "666")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "2"));
		
		echo("####### Check Application-Quotas have been setup as configured #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000001").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.mb", "555")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "1"));
		
		echo("####### Executing a Quota-No-Change import #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("applicationMb", "555");
		createVariable("systemMessages", "666");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
		
		echo("####### Perform a change in System-Default-Quota #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("applicationMb", "555");
		createVariable("systemMessages", "888");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Check System-Quotas have been updated #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000000").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "888")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "2"));
		
		echo("####### Perform a change in Application-Default-Quota #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "published");
		createVariable("applicationMb", "777");
		createVariable("systemMessages", "888");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Check Application-Quotas have been updated #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/"+ APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.mb", "777")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "1"));
		
		echo("####### Make sure, the System-Quota stays unchanged with the last update #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/"+ APIManagerAdapter.SYSTEM_API_QUOTA).header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "888")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "2"));
		
		echo("####### Perform a breaking change, making sure, that defined Quotas persist #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "published"); 
		createVariable("enforce", "true");
		createVariable("systemMessages", "666");
		createVariable("applicationMb", "555");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has been re-imported with a new API-ID #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"));
		
		echo("####### Check System-Quotas have been setup as configured for the new API #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/"+ APIManagerAdapter.SYSTEM_API_QUOTA).header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "666")
			//.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "day")
			.validate("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.per", "2"));
		
		echo("####### Check Application-Quotas have been setup as configured for the new API  #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/"+ APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${newApiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.mb", "555")
			//.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "hour")
			.validate("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.per", "1"));
	}
}
