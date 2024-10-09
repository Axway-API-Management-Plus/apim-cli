package com.axway.apim.test.quota;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ValidateAppQuotaStaysTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;
	@CitrusTest
	@Test
	public void run() throws IOException, InterruptedException {
		ImportTestAction swaggerImport = new ImportTestAction();
		description("Validates potentially configured application quota stay after re-importing an API.");
        variable("useApiAdmin", "true"); // Use apiadmin account
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/app-quota-check-${apiNumber}");
		variable("apiName", "App Quota Check ${apiNumber}");

        variable("appName", "Test App with quota ${apiNumber}");
        $(echo("####### Creating test a application: '${appName}' used to configure some sample quotas #######"));
        $(http().client(apiManager).send().post("/applications").name("orgCreatedRequest").message().header("Content-Type", "application/json")
			.body("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "consumingTestAppId")
			.expression("$.name", "consumingTestAppName")));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/2_api-with-quota-app-subscription.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        variable("applicationPeriod", "hour");
        variable("systemPeriod", "day");
        variable("ignoreQuotas", "true");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "published")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Get the operations/methods for the created API #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}/operations"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.[?(@.name=='updatePetWithForm')].id", "testMethodId")));

        $(echo("####### Configure some Application-Quota for the imported API #######"));
        $(http().client(apiManager).send().post("/applications/${consumingTestAppId}/quota").name("createQuotaRequest").message()
			.header("Content-Type", "application/json")
			.body("{\"type\":\"APPLICATION\",\"name\":\"Client App Quota\",\"description\":\"\","
					+ "\"restrictions\":["
						+ "{\"api\":\"${apiId}\",\"method\":\"*\",\"type\":\"throttle\",\"config\":{\"period\":\"second\",\"per\":50,\"messages\":111}}, "
						+ "{\"api\":\"*\",\"method\":\"*\",\"type\":\"throttle\",\"config\":{\"period\":\"hour\",\"per\":1,\"messages\":1000}}, "
						+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId}\",\"type\":\"throttle\",\"config\":{\"period\":\"day\",\"per\":2,\"messages\":100000}} "
					+ "],"
					+ "\"system\":false}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED));
        $(echo("####### Enforce Re-Creation of API - Application quotas must stay #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/2_api-with-quota-app-subscription.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        variable("applicationPeriod", "hour");
        variable("systemPeriod", "day");
        variable("ignoreQuotas", "true");
        variable("enforce", "true");
        $(action(swaggerImport));

        $(sleep().seconds(10));
		$(echo("####### The API has been updated - Reload the the API-ID #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "published")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "newApiId")));

        $(echo("####### And reload the first methodId as we need it for validation #######"));
        $(http().client(apiManager).send().get("/proxies/${newApiId}/operations"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.[?(@.name=='updatePetWithForm')].id", "newTestMethodId")));

		$(echo("####### Load the application quota and validate it is still present #######"));
        $(echo("####### newApiId: '${newApiId}' #######"));
        $(echo("####### newTestMethodId: '${newTestMethodId}' #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestAppId}/quota"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.type", "APPLICATION")
			// First validate the "All methods" quota is still there
			.expression("$.restrictions.[?(@.api=='*')].type", "throttle")
			//.validate("$.restrictions.[?(@.api=='*')].config.period", "hour")
			.expression("$.restrictions.[?(@.api=='*')].config.per", "1")
			.expression("$.restrictions.[?(@.api=='*')].config.messages", "1000")
			// Next validate API-Specific quota is still present
			.expression("$.restrictions.[?(@.api=='${newApiId}' && @.method=='*')].type", "throttle")
			//.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='*')].config.period", "second")
			.expression("$.restrictions.[?(@.api=='${newApiId}' && @.method=='*')].config.per", "50")
			.expression("$.restrictions.[?(@.api=='${newApiId}' && @.method=='*')].config.messages", "111")
			// Lastly validate API-Method-Specific quota is still present
			.expression("$.restrictions.[?(@.api=='${newApiId}' && @.method=='${newTestMethodId}')].type", "throttle")
			//.validate("$.restrictions.[?(@.api=='${newApiId}' && @.method=='${newTestMethodId}')].config.period", "day")
			.expression("$.restrictions.[?(@.api=='${newApiId}' && @.method=='${newTestMethodId}')].config.per", "2")
			.expression("$.restrictions.[?(@.api=='${newApiId}' && @.method=='${newTestMethodId}')].config.messages", "100000")));
	}
}
