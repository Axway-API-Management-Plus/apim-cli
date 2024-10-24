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
public class AppSubscriptionUnpublishedTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;
	@CitrusTest
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Validates, that App-Subscriptions are working on a Unpublished API!");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/app-subscr-unpublished-${apiNumber}");
		variable("apiName", "App Subsc Unpublished API-${apiNumber}");
		// ############## Creating Test-Application #################

        variable("appName", "Unpublished Test App ${apiNumber}");
        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
			.body("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "testAppId")
			.expression("$.name", "testAppName")));

		$(echo("####### Created Test-Application: '${testAppName}' with id: '${testAppId}' #######"));
        variable("appName2", "Unpublished Test App 2 ${apiNumber}");
        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
			.body("{\"name\":\"${appName2}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "testAppId2")
			.expression("$.name", "testAppName2")));

        $(echo("####### Created Test-Application: '${testAppName2}' with id: '${testAppId2}' #######"));
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
        variable("consumingTestAppName", "${testAppName}");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been created #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished"))
            .extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### API has been created with ID: '${apiId}' #######"));
        $(echo("####### Validate the application has already an active subscription to the API (Based on the name) #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId}/apis"));
            $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${apiId}")));

        $(echo("####### Trigger a Re-Create of the API: '${apiName}' - Subscription must stay  #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
        variable("consumingTestAppName", "${testAppName}");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been Re-Created #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished"))
            .extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "newApiId")));

        $(echo("####### Validate the application still has an active subscription to the API #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${newApiId}")));

        $(echo("####### Simulate the App-Subscription from before has been created manually (by removing it from the Config-File) #######"));
        $(echo("####### Trigger a Re-Create of the API: '${apiName}' - Subscription must stay  #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
        variable("consumingTestAppName", "${testAppName2}");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

		$(echo("####### Validate API: '${apiName}' has been Re-Created #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished"))
            .extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "newApiId2")));

        $(echo("####### Validate that BOTH applications still has an active subscription to the API #######"));
        $(echo("####### First app: ${testAppName} (${testAppId}) #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${newApiId2}")));
        $(echo("####### Second app: ${testAppName2} (${testAppId2}) #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${newApiId2}")));

        $(echo("####### Simulate the Config-File has no applications configured - Existing subscriptions must stay! #######"));
        $(echo("####### Trigger a Re-Create of the API: '${apiName}' - Subscription must stay  #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

		$(echo("####### Validate API: '${apiName}' has been Re-Created #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished"))
            .extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "newApiId3")));

        $(echo("####### Validate again that BOTH applications still has an active subscription to the API #######"));
        $(echo("####### First app: ${testAppName} (${testAppId}) #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${newApiId3}")));
		$(echo("####### Second app: ${testAppName2} (${testAppId2}) #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${newApiId3}")));
	}
}
