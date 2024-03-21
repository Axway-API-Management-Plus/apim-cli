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

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class DuplicateApplicationSubscriptionTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Verify subscription handling, if App isn't unique based on the name (See issue #217)");
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("apiPath", "/duplicate.app-subscription-${apiNumber}");
        variable("apiName", "Duplicate-App Subscription API-${apiNumber}");
        // ############## Creating Test-Application 1 #################
        variable("app1Name", "Consuming Test App 1 ${apiNumber}");
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

        $(echo("####### Added an API-Key: '${consumingTestApp1ApiKey}' to Test-Application 1 #######"));
        // ############## Creating the same application, having the same name, different organization #################
        variable("extClientId", RandomNumberFunction.getRandomNumber(15, true));
        variable("app2Name", "Consuming Test App 1 ${apiNumber}");
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

        $(echo("####### Added an Ext-ClientID: '${extClientId}' to Test-Application 2 #######"));
        $(echo("####### Try to importing API: '${apiName}' on path: '${apiPath}' having a Non-Unique subscription #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/applications/1_api-with-1-org-1-app.json");
        // Try to create a subscription with a Non-Unique application name
        variable("testAppName", "${app1Name}");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("expectedReturnCode", "89");
        $(action(swaggerImport));

        $(echo("####### Importing the same API: '${apiName}' on path: '${apiPath}' given the orgname as part of the application-name #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/applications/1_api-with-1-org-1-app.json");
        // Try to create a subscription with a Non-Unique application name
        variable("testAppName", "${app1Name}|${orgName2}");
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

        $(echo("####### Validate created application 2 has an active subscription to the API (NOT THE FIRST APPLICATION) #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp2Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${apiId}")));

        $(echo("####### Validate created application 1 has NO active subscription to the API #######"));
        $(http().client(apiManager).send().get("/applications/${consumingTestApp1Id}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "@assertThat(not(containsString(${apiId})))@")));
    }
}
