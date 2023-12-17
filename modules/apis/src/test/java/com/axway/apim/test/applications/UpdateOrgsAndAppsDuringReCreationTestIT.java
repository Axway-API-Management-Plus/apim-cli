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
public class UpdateOrgsAndAppsDuringReCreationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Replicates a given scenario for issue: #58");
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("apiPath", "/update-orgs-apps-${apiNumber}");
        variable("apiName", "Update Org and Apps-${apiNumber}");
        // ############## Create 4 Consuming organizations #################
        variable("orgName1", "1 Non-Dev Org ${orgNumber}");
        variable("orgName2", "2 Non-Dev Org ${orgNumber}");
        variable("orgName3", "3 Non-Dev Org ${orgNumber}");
        variable("orgName4", "4 Non-Dev Org ${orgNumber}");

        $(http().client(apiManager).send().post("/organizations").message().header("Content-Type", "application/json")
            .body("{\"name\": \"${orgName1}\", \"description\": \"Org 1 without dev permission\", \"enabled\": true, \"development\": false }"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.name", "${orgName1}")).extract(fromBody()
            .expression("$.id", "orgId1")));

        $(http().client(apiManager).send().post("/organizations").message().header("Content-Type", "application/json")
            .body("{\"name\": \"${orgName2}\", \"description\": \"Org 2 without dev permission\", \"enabled\": true, \"development\": false }"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.name", "${orgName2}")).extract(fromBody()
            .expression("$.id", "orgId2")));

        $(http().client(apiManager).send().post("/organizations").message().header("Content-Type", "application/json")
            .body("{\"name\": \"${orgName3}\", \"description\": \"Org 3 without dev permission\", \"enabled\": true, \"development\": false }"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.name", "${orgName3}")).extract(fromBody()
            .expression("$.id", "orgId3")));

        $(http().client(apiManager).send().post("/organizations").message().header("Content-Type", "application/json")
            .body("{\"name\": \"${orgName4}\", \"description\": \"Org 4 without dev permission\", \"enabled\": true, \"development\": false }"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.name", "${orgName4}"))
            .extract(fromBody()
                .expression("$.id", "orgId4")));

        $(echo("####### Created 4 Test-Organizations #######"));

        // ############## Creating 4 Test-Applications #################
        variable("appName1", "App 1 in granted org ${apiNumber}");
        variable("appName2", "App 2 in granted org ${apiNumber}");
        variable("appName3", "App 3 in granted org ${apiNumber}");
        variable("appName4", "App 4 in granted org ${apiNumber}");

        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
            .body("{\"name\":\"${appName1}\",\"apis\":[],\"organizationId\":\"${orgId1}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.name", "${appName1}"))
            .extract(fromBody()
                .expression("$.id", "appId1")));

        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
            .body("{\"name\":\"${appName2}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.name", "${appName2}"))
            .extract(fromBody()
                .expression("$.id", "appId2")));

        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
            .body("{\"name\":\"${appName3}\",\"apis\":[],\"organizationId\":\"${orgId3}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.name", "${appName3}"))
            .extract(fromBody()
                .expression("$.id", "appId3")));

        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
            .body("{\"name\":\"${appName4}\",\"apis\":[],\"organizationId\":\"${orgId4}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.name", "${appName4}"))
            .extract(fromBody()
                .expression("$.id", "appId4")));

        $(echo("####### Created 4 Test-Applications #######"));

        $(echo("####### Importing Published API: '${apiName}' on path: '${apiPath}' #######"));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/applications/1_api-with-4-orgs-4-apps.json");
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

        $(echo("####### Validate each organization has access to this API #######"));
        $(http().client(apiManager).send().get("/organizations/${orgId1}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));
        $(http().client(apiManager).send().get("/organizations/${orgId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));
        $(http().client(apiManager).send().get("/organizations/${orgId3}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));
        $(http().client(apiManager).send().get("/organizations/${orgId4}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));

        $(echo("####### Validate each application has access to this API #######"));
        $(http().client(apiManager).send().get("/applications/${appId1}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${apiId}")));
        $(http().client(apiManager).send().get("/applications/${appId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${apiId}")));
        $(http().client(apiManager).send().get("/applications/${appId3}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${apiId}")));
        $(http().client(apiManager).send().get("/applications/${appId4}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${apiId}")));

        $(echo("############## Re-Create this API and Reduce number of Org & Apps #################"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/applications/1_api-with-3-orgs-3-apps.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("expectedReturnCode", "0");
        variable("enforce", "true");
        $(action(swaggerImport));

        $(echo("####### Validate Re-Created API exists #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "published"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "newApiId")));

        $(echo("####### Validate each organization has access to the Re-Created API #######"));
        $(http().client(apiManager).send().get("/organizations/${orgId1}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${newApiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${newApiId}')].enabled", "true")));
        $(http().client(apiManager).send().get("/organizations/${orgId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${newApiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${newApiId}')].enabled", "true")));
        $(http().client(apiManager).send().get("/organizations/${orgId3}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${newApiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${newApiId}')].enabled", "true")));

        $(echo("####### Validate each application has access to the Re-Created API #######"));
        $(http().client(apiManager).send().get("/applications/${appId1}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${newApiId}")));
        $(http().client(apiManager).send().get("/applications/${appId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${newApiId}")));
        $(http().client(apiManager).send().get("/applications/${appId3}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "${newApiId}")));
    }
}
