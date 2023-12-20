package com.axway.apim.changeAction;

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
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;


@ContextConfiguration(classes = {EndpointConfig.class})
public class ChangeBackendTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    private static final String TEST_PACKAGE = "/com/axway/apim/changeAction/";

    @CitrusTest
    @Test
    public void run() throws IOException {
        ChangeTestAction changeAction = new ChangeTestAction();
        ImportTestAction importAction = new ImportTestAction();

        description("This test imports an API including quota, subscription and granted access to some org the it changes the backend URL of it and validates it.");
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/change-backend-${apiNumber}");
        variable("apiName", "Change-Backend-${apiNumber}");
        variable("state", "published");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' which should be changed #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, TEST_PACKAGE + "changeBackendTestAPI-config.json");
        variable("expectedReturnCode", "0");
        $(action(importAction));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies?field=name&op=eq&value=${apiName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Try to change the backend of this published API, but without giving a force flag #######"));
        variable("expectedReturnCode", "15");
        variable("name", "${apiName}");
        variable("enforce", "false");
        variable("newBackend", "https://api.axway.com");
        $(action(changeAction));

        $(echo("####### Change the backend of this published API - Enforcing it! #######"));
        variable("expectedReturnCode", "0");
        variable("name", "${apiName}");
        variable("newBackend", "https://api.axway.com");
        variable("enforce", "true");
        $(action(changeAction));

        $(echo("####### Validate re-created API has properly created inlcuding all quota, apps, orgs #######"));
        $(http().client(apiManager).send().get("/proxies?field=name&op=eq&value=${apiName}"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].serviceProfiles._default.basePath", "https://api.axway.com"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "newApiId")));
    }

}
