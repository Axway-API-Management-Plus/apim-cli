package com.axway.apim.test.quota;

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
public class ValidateAppQuotaStaysTestIT extends TestNGCitrusTestRunner {

	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, InterruptedException {
		ImportTestAction swaggerImport = new ImportTestAction();

		description("Validates potentially configured application quota stay after re-importing an API.");

		createVariable("useApiAdmin", "true"); // Use apiadmin account
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/app-quota-check-${apiNumber}");
		variable("apiName", "App Quota Check ${apiNumber}");

		createVariable("appName", "Test App with quota ${apiNumber}");
		echo("####### Creating test a application: '${appName}' used to configure some sample quotas #######");
		http(builder -> builder.client("apiManager").send().post("/applications").name("orgCreatedRequest")	.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestAppId")
			.extractFromPayload("$.name", "consumingTestAppName"));

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/2_api-with-quota-app-subscription.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		createVariable("applicationPeriod", "hour");
		createVariable("systemPeriod", "day");
		createVariable("ignoreQuotas", "true");
		swaggerImport.doExecute(context);

		echo("####### Validate API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));

		echo("####### Get the operations/methods for the created API #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}/operations").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON).extractFromPayload("$.[?(@.name=='updatePetWithForm')].id", "testMethodId"));

		echo("####### Configure some Application-Quota for the imported API #######");
		http(builder -> builder.client("apiManager").send()
			.post("/applications/${consumingTestAppId}/quota")
			.name("createQuotaRequest")
			.header("Content-Type", "application/json")
			.payload("{\"type\":\"APPLICATION\",\"name\":\"Client App Quota\",\"description\":\"\","
					+ "\"restrictions\":["
						+ "{\"api\":\"${apiId}\",\"method\":\"*\",\"type\":\"throttle\",\"config\":{\"period\":\"second\",\"per\":50,\"messages\":111}}, "
						+ "{\"api\":\"*\",\"method\":\"*\",\"type\":\"throttle\",\"config\":{\"period\":\"hour\",\"per\":1,\"messages\":1000}}, "
						+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId}\",\"type\":\"throttle\",\"config\":{\"period\":\"day\",\"per\":2,\"messages\":100000}} "
					+ "],"
					+ "\"system\":false}"));

		echo("####### Enforce Re-Creation of API - Application quotas must stay #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/2_api-with-quota-app-subscription.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		createVariable("applicationPeriod", "hour");
		createVariable("systemPeriod", "day");
		createVariable("ignoreQuotas", "true");
		createVariable("enforce", "true");
		swaggerImport.doExecute(context);
        sleep(12000);

		echo("####### The API has been updated - Reload the the API-ID #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"));

		echo("####### And reload the first methodId as we need it for validation #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${newApiId}/operations").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON).extractFromPayload("$.[?(@.name=='updatePetWithForm')].id", "newTestMethodId"));

		echo("####### Load the application quota and validate it is still present #######");
		echo("####### newApiId: '${newApiId}' #######");
		echo("####### newTestMethodId: '${newTestMethodId}' #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestAppId}/quota"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.type", "APPLICATION")
			// First validate the "All methods" quota is still there
			.validate("$.restrictions.[?(@.api=='*')].type", "throttle")
			//.validate("$.restrictions.[?(@.api=='*')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='*')].config.per", "1")
			.validate("$.restrictions.[?(@.api=='*')].config.messages", "1000")
			// Next validate API-Specific quota is still present
			.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='*')].type", "throttle")
			//.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='*')].config.period", "second")
			.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='*')].config.per", "50")
			.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='*')].config.messages", "111")
			// Lastly validate API-Method-Specific quota is still present
			.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='${newTestMethodId}')].type", "throttle")
			//.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='${newTestMethodId}')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='${newTestMethodId}')].config.per", "2")
			.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='${newTestMethodId}')].config.messages", "100000"));
	}
}
