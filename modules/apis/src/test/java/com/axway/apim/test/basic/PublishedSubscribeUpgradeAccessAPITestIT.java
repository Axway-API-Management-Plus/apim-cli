package com.axway.apim.test.basic;

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
public class PublishedSubscribeUpgradeAccessAPITestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        $(echo("####### Import a Published-API, subscribe to it and then Re-Import a new version. #######"));
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("apiPath", "/my-test-api-${apiNumber}");
        variable("apiName", "My-Test-API-${apiNumber}");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("state", "published");
        variable("version", "1.0.0");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "${state}"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId") // Remember the API-ID --> This is the FE-API
                .expression("$.[?(@.path=='${apiPath}')].apiId", "beApiId"))); // This is the BE-API

        // Subscribe to that API!
        $(echo("####### Subscribing API: ${apiName} with test-application: ${testAppName} #######"));
        $(http().client(apiManager).send().post("/applications/${testAppId}/apis/").message().contentType("application/json")
            .body("{\"apiId\":\"${apiId}\",\"enabled\":true}")
            .header("Content-Type", "application/json"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED));

        $(echo("####### Importing a new Swagger-File as a change #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("state", "published");
        variable("enforce", "true");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate the API is still there with right status #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "${state}"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "newApiId"))); // We have a new API-ID

        $(echo("####### Validate subscription is still present! #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${newApiId}')].enabled", "true")));

        $(echo("####### Validate the previous FE-API has been deleted #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.FORBIDDEN));

        $(echo("####### Validate the previous BE-API has been deleted #######"));
        $(http().client(apiManager).send().get("/apirepo/${beApiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.FORBIDDEN));
    }

}
