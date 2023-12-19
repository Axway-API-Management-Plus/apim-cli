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
public class SubscriptionAppInUngrantedOrgTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "SubscriptionAppInUngrantedOrgTestIT")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("This test validates the behavior if a Client-App-Subscription is configured for an org without API-Permission.");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/app-in-ungranted-org-${apiNumber}");
		variable("apiName", "App-Subscription wrong Org-${apiNumber}");
		// ############## Creating Test-Application 1 #################
        variable("appName1", "App in granted org ${apiNumber}");
        variable("appName2", "App in ungranted org ${apiNumber}");
        $(http().client(apiManager).send().post("/applications").name("orgCreatedRequest").message().header("Content-Type", "application/json")
			.body("{\"name\":\"${appName1}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "testAppId1")
			.expression("$.name", "testAppName1")));

		$(echo("####### Created Test-Application: '${testAppName1}' with id: '${testAppId1}' #######"));

		// ############## Creating Test-Application 2 #################
        variable("appName1", "App in granted org ${apiNumber}");
        variable("appName2", "App in ungranted org ${apiNumber}");
        $(http().client(apiManager).send().post("/applications").name("orgCreatedRequest").message().header("Content-Type", "application/json")
			.body("{\"name\":\"${appName2}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "testAppId2")
			.expression("$.name", "testAppName2")));

        $(echo("####### Created Test-Application: '${testAppName2}' with id: '${testAppId2}' #######"));
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-2-app.json");
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

		$(echo("####### Validate App-1: '${testAppName1}' (ID: ${testAppId1}) has access #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId1}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "@assertThat(containsString(${apiId}))@")));

		$(echo("####### Validate App-2: '${testAppName2}' (ID: ${testAppId2}) has NO access #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "@assertThat(not(containsString(${apiId})))@")));
	}
}
