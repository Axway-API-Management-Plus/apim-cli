package com.axway.apim.test.quota;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class AppOrSystemQuotaOnlyTestIT extends TestNGCitrusTestRunner {
	
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException, InterruptedException {
		swaggerImport = new ImportTestAction();

		description("Validates quota is set when only System- or Application-Quota is configured (see bug #55)");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/only-one-quota-api-${apiNumber}");
		variable("apiName", "Only-One-Quota-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with System-Quota only #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/3_api-with-system-quota-only.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		createVariable("systemPeriod", "hour");
		createVariable("messages", "345");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported including System-Quota #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Check System-Quotas have been setup as configured #######");
		echo("####### ############ Sleep 5 seconds ##################### #######");
		Thread.sleep(5000);
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000000").name("api").header("Content-Type", "application/json"));
		Thread.sleep(5000);

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "345")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "5"));
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with Appliction-Quota only #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/4_api-with-app-quota-only2.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		createVariable("applicationPeriod", "day");
		swaggerImport.doExecute(context);
		
		echo("####### Check Application-Quotas have been setup as configured #######");
		echo("####### ############ Sleep 5 seconds ##################### #######");
		Thread.sleep(5000);
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000001").name("api")	.header("Content-Type", "application/json"));
		Thread.sleep(5000);
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.mb", "787")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day") 
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "4"));
		
	}
}
