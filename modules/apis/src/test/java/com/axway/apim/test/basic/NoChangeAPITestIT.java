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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;


@ContextConfiguration(classes = {EndpointConfig.class})
public class NoChangeAPITestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;


    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import an API and re-import it without any change. It must be detected, that no change happened.");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/my-no-change-${apiNumber}");
        variable("apiName", "No-Change-${apiNumber}");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with an unknown RemoteHost #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/api-with-remote-host-config.json");
        variable("expectedReturnCode", "63"); // Must fail, as the RemoteHost is unknown
        variable("remoteHostName", "my.host-${apiNumber}.com");
        variable("remoteHostPort", "8786");
        $(action(swaggerImport));
        $(echo("####### Creating remote host ${remoteHostName}:${remoteHostPort} #######"));
        $(http().client(apiManager).send().post("/remotehosts").message().contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("{\"name\":\"${remoteHostName}\",\"port\":\"${remoteHostPort}\",\"organizationId\":\"${orgId}\"}"));

        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' incl. a RemoteHost #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/api-with-remote-host-config.json");
        variable("expectedReturnCode", "0"); // Must fail, as the RemoteHost is unknown
        $(action(swaggerImport));
        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### RE-Importing same API: '${apiName}' on path: '${apiPath}' without changes. Expecting failure with RC 99. #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/api-with-remote-host-config.json");
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
