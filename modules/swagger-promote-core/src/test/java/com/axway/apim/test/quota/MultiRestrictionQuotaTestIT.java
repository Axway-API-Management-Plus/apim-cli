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
public class MultiRestrictionQuotaTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException, InterruptedException {
		swaggerImport = new ImportTestAction();
		
		description("Make sure you can configured multiple Quota-Restrictions for an API");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/multi-quota-restriction--api-${apiNumber}");
		variable("apiName", "Multi-Quota-Restriction-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/5_multi-restriction-quota.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		
		createVariable("systemPeriod1", "hour");
		createVariable("systemMessages1", "666");
		createVariable("systemPer1", "2");
		createVariable("systemPeriod2", "day");
		createVariable("systemMessages2", "100000");
		createVariable("systemPer2", "1");
		
		createVariable("applicationPeriod1", "hour");
		createVariable("applicationMB1", "30");
		createVariable("applicationPer1", "1");
		createVariable("applicationPeriod2", "day");
		createVariable("applicationMB2", "1024");
		createVariable("applicationPer2", "1");
		
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Check System-Quotas have been setup as configured #######");
		if(APIManagerAdapter.hasAPIManagerVersion("7.7.20200130")) {
			Thread.sleep(600); // Starting with this version, we need to wait a few milliseconds, otherwise the REST-API doesn't return the complete set of quotas
		}
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000000").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle,throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*,*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "${systemPeriod1},${systemPeriod2}")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "${systemMessages1},${systemMessages2}")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "${systemPer1},${systemPer2}"));
		
		echo("####### Check Application-Quotas have been setup as configured #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000001").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb,throttlemb")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*,*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "${applicationPeriod1},${applicationPeriod2}")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.mb", "${applicationMB1},${applicationMB2}")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "${applicationPer1},${applicationPer2}"));
		
		echo("####### Perform a No-Change test #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/5_multi-restriction-quota.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
	}
}
