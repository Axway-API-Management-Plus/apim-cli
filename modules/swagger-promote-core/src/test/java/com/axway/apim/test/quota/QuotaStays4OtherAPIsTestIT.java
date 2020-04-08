package com.axway.apim.test.quota;

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
public class QuotaStays4OtherAPIsTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException, InterruptedException {
		swaggerImport = new ImportTestAction();
		description("Making sure, APIs for other APIs are not influences by Quota-Management for the actual API.");
		
		variable("firstApiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("firstApiPath", "/first-quota-api-${firstApiNumber}");
		variable("firstApiName", "First Quota-API-${firstApiNumber}");
		variable("secApiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("secApiPath", "/second-quota-api-${firstApiNumber}");
		variable("secApiName", "Second Quota-API-${firstApiNumber}");

		
		echo("####### Importing the first API: '${firstApiName}' on path: '${firstApiPath}' with some quotas #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		createVariable("applicationPeriod", "hour");
		createVariable("applicationMb", "555");
		createVariable("systemPeriod", "day");
		createVariable("systemMessages", "666");
		createVariable("apiName", "${firstApiName}");
		createVariable("apiPath", "${firstApiPath}");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${firstApiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${firstApiPath}')].name", "${firstApiName}")
			.validate("$.[?(@.path=='${firstApiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${firstApiPath}')].id", "firstApiId"));
		echo("####### First API: '${firstApiName}' (ID: ${firstApiId}) has a been imported #######");
		
		echo("####### Check System-Quotas have been setup as configured for the first API #######");
		if(APIManagerAdapter.hasAPIManagerVersion("7.7.20200130")) {
			System.out.println("Sleep");
			Thread.sleep(1000); // Starting with this version, we need to wait a few milliseconds, otherwise the REST-API doesn't return the complete set of quotas
		} else {
			System.out.println("Don't sleep");
		}
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000000").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.messages", "666")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.per", "2"));
		
		echo("####### Check Application-Quotas have been setup as configured #######");
		if(APIManagerAdapter.hasAPIManagerVersion("7.7.20200130")) {
			System.out.println("Sleep");
			Thread.sleep(1000); // Starting with this version, we need to wait a few milliseconds, otherwise the REST-API doesn't return the complete set of quotas
		} else {
			System.out.println("Don't sleep");
		}
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000001").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.mb", "555")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.per", "1"));
		
		echo("####### Import a second API also with Quotas #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("applicationMb", "777");
		createVariable("systemMessages", "888");
		createVariable("systemPeriod", "week");
		createVariable("applicationPeriod", "second");
		createVariable("apiName", "${secApiName}");
		createVariable("apiPath", "${secApiPath}");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Make sure quotas from the First API are still there! #######");		
		echo("####### Check System-Quotas have been setup as configured for the first API #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000000").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.messages", "666")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.per", "2"));
		
		echo("####### Check Application-Quotas have been setup as configured #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000001").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.mb", "555")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.per", "1"));
	}
}
