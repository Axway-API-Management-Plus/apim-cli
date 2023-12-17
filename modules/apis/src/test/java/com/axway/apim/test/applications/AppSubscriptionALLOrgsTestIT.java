package com.axway.apim.test.applications;

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

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class AppSubscriptionALLOrgsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


	@CitrusTest(name = "AppSubscriptionALLOrgsTestIT")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Test to validate, that application subscription works, when using an ALL org-mapping");
		variable("useApiAdmin", "true"); // Use apiadmin account
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/app-subs-mode-add-${apiNumber}");
		variable("apiName", "App-Subscription-Mode Add Test API-${apiNumber}");
		variable("appName1", "App Subscr-ALL-Org-Test 1 ${orgNumber}");
		variable("appName2", "App Subscr-ALL-Org-Test 2 ${orgNumber}");
		variable("appName3", "App Subscr-ALL-Org-Test 3 ${orgNumber}");
		// ############## Creating Test-Application 1 #################
        $(http().client(apiManager).send().post("/applications").name("orgCreatedRequest").message()
            .header("Content-Type", "application/json")
			.body("{\"name\":\"${appName1}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "consumingTestAppId1")
			.expression("$.name", "consumingTestAppName1")));

		$(echo("####### Created Test-Application 1: '${consumingTestAppName1}' with id: '${consumingTestAppId1}' #######"));
		// ############## Creating Test-Application 2 #################
        variable("extClientId", RandomNumberFunction.getRandomNumber(15, true));
        $(http().client(apiManager).send().post("/applications").name("orgCreatedRequest").message()
			.header("Content-Type", "application/json")
			.body("{\"name\":\"${appName2}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "consumingTestAppId2")
			.expression("$.name", "consumingTestAppName2")));

		$(echo("####### Created Test-Application 2: '${consumingTestAppName2}' with id: '${consumingTestAppId2}' #######"));
		// ############## Creating Test-Application 3 #################
        $(http().client(apiManager).send().post("/applications").name("orgCreatedRequest").message()
			.header("Content-Type", "application/json")
			.body("{\"name\":\"${appName3}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "consumingTestAppId3")
			.expression("$.name", "consumingTestAppName3")));

        $(echo("####### Created Test-Application 3: '${consumingTestAppName3}' with id: '${consumingTestAppId3}' #######"));
        $(echo("####### Import an API and create a subscription to application: '${appName2}' #######"));

        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-1-app.json");
        variable("state", "published");
        variable("orgName2", "ALL");
        variable("testAppName", "${appName2}"); // This app wants to have a subscription (located in another org)
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been created #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "published"))
            .extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### API has been created with ID: '${apiId}' #######"));
        $(echo("####### Validate created application 2 has an active subscription to the API #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestAppId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${apiId}")));
	}
}
