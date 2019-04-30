package com.axway.apim.test.quota;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class ValidateAppQuotaStaysTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	public void run() {
		description("Validates potentially configured application quota stay after re-importing an API.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/app-quota-check-${apiNumber}");
		variable("apiName", "App Quota Check ${apiNumber}");
		
		createVariable("appName", "Test App with quota ${orgNumber}");
		echo("####### Creating test a application: '${appName}' used to configure some sample quotas #######");
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestAppId")
			.extractFromPayload("$.name", "consumingTestAppName");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/2_api-with-quota-app-subscription.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		createVariable("applicationPeriod", "hour");
		createVariable("systemPeriod", "days");
		createVariable("ignoreQuotas", "true");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Configure some Application-Quota for the imported API #######");
		http().client("apiManager")
			.send()
			.post("/applications/${consumingTestAppId}/quota")
			.name("createQuotaRequest")
			.header("Content-Type", "application/json")
			.payload("{\"type\":\"APPLICATION\",\"name\":\"Client App Quota\",\"description\":\"\","
					+ "\"restrictions\":["
						+ "{\"api\":\"${apiId}\",\"method\":\"*\",\"type\":\"throttle\",\"config\":{\"period\":\"second\",\"per\":50,\"messages\":111}}, "
						+ "{\"api\":\"*\",\"method\":\"*\",\"type\":\"throttle\",\"config\":{\"period\":\"hour\",\"per\":1,\"messages\":1000}} "
					+ "],"
					+ "\"system\":false}");
		
		echo("####### Enforce Re-Creation of API - Application quotas must stay #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/2_api-with-quota-app-subscription.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		createVariable("applicationPeriod", "hour");
		createVariable("systemPeriod", "days");
		createVariable("ignoreQuotas", "true");
		createVariable("enforce", "true");
		action(swaggerImport);
		
		echo("####### The API has been updated - Reload the the API-ID #######");
		http().client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId");
		
		echo("####### Load the application quota and validate it is still present #######");
		http().client("apiManager").send().get("/applications/${consumingTestAppId}/quota");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.type", "APPLICATION")
			// First validate the "All methods" quota is still there
			.validate("$.restrictions.[?(@.api=='*')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='*')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='*')].config.per", "1")
			.validate("$.restrictions.[?(@.api=='*')].config.messages", "1000")
			// Next validate API-Specific quota is still present
			.validate("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "second")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.per", "50")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "111");
	}
}
