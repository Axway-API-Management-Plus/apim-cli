package com.axway.apim.test.applications;

import com.axway.apim.EndpointConfig;
import com.axway.apim.lib.CoreParameters.Mode;
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
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class ApplicationSubscriptionTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest
	@Test
	public void run() throws IOException {
		//TestIndicator.getInstance().setTestRunning(false);
		ImportTestAction swaggerImport = new ImportTestAction();
		description("Import an API, grant access to an org and create an application subscription.");
        variable("useApiAdmin", "true"); // use apiadmin account
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/app-subscription-${apiNumber}");
		variable("apiName", "App Subscription API-${apiNumber}");
		// ############## Creating Test-Application 1 #################
        variable("app1Name", "Test-SubApp 1 ${apiNumber}");
        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
			.body("{\"name\":\"${app1Name}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "consumingTestApp1Id")
			.expression("$.name", "consumingTestApp1Name")));

		$(echo("####### Created Test-Application 1: '${consumingTestApp1Name}' with id: '${consumingTestApp1Id}' #######"));
        $(http().client(apiManager).send().post("/applications/${consumingTestApp1Id}/apikeys").message()
			.header("Content-Type", "application/json")
			.body("{\"applicationId\":\"${consumingTestApp1Id}\",\"enabled\":\"true\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "consumingTestApp1ApiKey")));

		$(echo("####### Generated API-Key: '${consumingTestApp1ApiKey}' for Test-Application 1: '${consumingTestApp1Name}' with id: '${consumingTestApp1Id}' #######"));
		// ############## Creating Test-Application 2 #################
        variable("extClientId", RandomNumberFunction.getRandomNumber(15, true));
        variable("app2Name", "Test-SubApp 2 ${apiNumber}");
        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
			.body("{\"name\":\"${app2Name}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "consumingTestApp2Id")
			.expression("$.name", "consumingTestApp2Name")));

		$(echo("####### Created Test-Application 2: '${consumingTestApp2Name}' with id: '${consumingTestApp2Id}' #######"));
        $(http().client(apiManager).send().post("/applications/${consumingTestApp2Id}/extclients").message().header("Content-Type", "application/json")
			.body("{\"clientId\":\"${extClientId}\",\"enabled\":\"true\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "consumingTestApp2ClientId")));

        $(echo("####### Added an Ext-ClientID: '${extClientId}' to Test-Application 2: '${consumingTestApp2Name}' with id: '${consumingTestApp2Id}' #######"));
		// ############## Creating Test-Application 3 #################
        variable("app3Name", "Test-SubApp 3 ${apiNumber}");
        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
			.body("{\"name\":\"${app3Name}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
			.expression("$.id", "consumingTestApp3Id")
			.expression("$.name", "consumingTestApp3Name")));

        $(echo("####### Created Test-Application 3: '${consumingTestApp3Name}' with id: '${consumingTestApp3Id}' withouth App-Credentials #######"));
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("version", "1.0.0");
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
        $(echo("####### Validate API with ID: '${apiId}' is granted to Org2: ${orgName2} (${orgId2}) #######"));
        $(http().client(apiManager).send().get("/organizations/${orgId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
		.expression("$.[?(@.apiId=='${apiId}')].state", "approved")
		.expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));

        $(echo("####### Validate created application 3 has an active subscription to the API (Based on the name) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp3Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

		$(echo("####### Validate Application 1 has an active subscription to the API (based on the API-Key) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp1Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

		$(echo("####### Validate Application 2 has an active subscription to the API (based on the Ext-Client-Id) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp2Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

        $(echo("####### Re-Importing same API: '${apiName}' - must result in No-Change #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
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

        $(echo("####### Changing FE-API Settings only for: '${apiName}' - Mode: Unpublish/Publish and make sure subscriptions stay #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("version", "2.0.0");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been reconfigured (Unpublich/Publish) and appscriptions are recreated #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "published")));

        $(echo("####### Validate Re-Puslished API with ID: '${apiId}' is still granted to Org2: ${orgName2} (${orgId2}) #######"));
        $(http().client(apiManager).send().get("/organizations/${orgId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
		.expression("$.[?(@.apiId=='${apiId}')].state", "approved")
		.expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));

        $(echo("####### Validate Application 3 still has an active subscription to the API (Based on the name) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp3Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

        $(echo("####### Validate Application 1 still has an active subscription to the API (based on the API-Key) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp1Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

        $(echo("####### Validate Application 2 still has an active subscription to the API (based on the Ext-Client-Id) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp2Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

        $(echo("####### Slightly modify the API: '${apiName}' - Without applications given in the config and mode add (which is the default) (See issue: #117) #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("enforce", "true");
        variable("version", "3.0.0");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate previous application subscriptions have been restored after the API has been unpublished/updated/published #######"));
        $(echo("####### Validate Application 3 still has an active subscription to the API (Based on the name) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp3Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

        $(echo("####### Validate Application 1 still has an active subscription to the API (based on the API-Key) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp1Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

        $(echo("####### Validate Application 2 still has an active subscription to the API (based on the Ext-Client-Id) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp2Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${apiId}")));

        $(echo("####### Re-Importing same API: '${apiName}' - Without applications subscriptions and mode replace #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("enforce", "true");
        variable("version", "4.0.0");
        variable("clientAppsMode", String.valueOf(Mode.replace));
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been re-created and subscriptions has been removed #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
				.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.expression("$.[?(@.path=='${apiPath}')].state", "published"))
            .extract(fromBody()
				.expression("$.[?(@.path=='${apiPath}')].id", "newApiId")));

        $(echo("####### Validate the application no Access to this API #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp1Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "@assertThat(not(contains(${newApiId})))@")));

        $(http().client(apiManager).send().get("/applications/${consumingTestApp2Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "@assertThat(not(contains(${newApiId})))@")));

        $(http().client(apiManager).send().get("/applications/${consumingTestApp3Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "@assertThat(not(contains(${newApiId})))@")));

        $(echo("####### Changing the state to unpublished #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-some-apps.json");
        variable("state", "unpublished");
        variable("enforce", "true");
        variable("clientAppsMode", String.valueOf(Mode.add));
        variable("orgName", "${orgName}");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Re-Import the API forcing a re-creation with an ORG-ADMIN ONLY account, making sure App-Subscriptions a re-created #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-2-app.json");
        variable("state", "unpublished");
        variable("enforce", "false");
        variable("orgName", "${orgName}");
        variable("expectedReturnCode", "0");
        variable("useApiAdmin", "false"); // We need to ignore any given admin account!
		// We only provide two apps instead of three, but the existing third subscription must stay!
        variable("testAppName1", "${consumingTestApp1Name}");
        variable("testAppName2", "${consumingTestApp2Name}");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been RE-CREATED #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished"))
            .extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "newApiId")));

		$(echo("####### API has been RE-CREATED with ID: '${newApiId}' #######"));

        $(echo("####### Validate Application 1 STILL has an active subscription to the API (based on the API-Key) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp1Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.*.apiId", "${newApiId}")));

		// As the apps 3 & 2 now belong to a different organization the org-admin cannot see / re-subscribe them
		/*
		echo("####### Validate created application 3 STILL has an active subscription to the API (Based on the name) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp3Id}/apis").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}"));
			echo("####### Validate Application 2 STILL has an active subscription to the API (based on the Ext-Client-Id) #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${consumingTestApp2Id}/apis")
			.header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.apiId", "${newApiId}")); */
	}
}
