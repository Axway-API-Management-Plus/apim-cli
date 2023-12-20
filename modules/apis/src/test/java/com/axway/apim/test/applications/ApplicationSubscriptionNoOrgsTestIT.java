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
public class ApplicationSubscriptionNoOrgsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "ApplicationSubscriptionNoOrgsTestIT")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Import an API and create an application subscription while not having defined any organization in the configuration.");
		variable("useApiAdmin", "true"); // Use apiadmin account
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/app-subscription-no-orgs-${apiNumber}");
		variable("apiName", "App Subscription No-Orgs API-${apiNumber}");
		// ############## Creating Test-Application #################
        variable("appName", "Consuming Test App ${apiNumber}");
		$(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
			.body("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "consumingTestAppId")
			.expression("$.name", "consumingTestAppName")));

        $(echo("####### Created Test-Application: '${consumingTestAppName}' with id: '${consumingTestAppId}' #######"));
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));

        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
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

        $(echo("####### Validate created application has an active subscription to the API (Based on the name) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestAppId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

        $(echo("####### Re-Importing same API: '${apiName}' - must result in No-Change #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-0-org-1-app.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Make sure, the API-ID hasn't changed #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
		// Check the API is still exposed on the same path
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].id", "${apiId}"))); // Must be the same API-ID as before!
	}
}
