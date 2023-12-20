package com.axway.apim.test.vhost;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class VhostConfigTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Validate VHosts are handled correctly");
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/vhost-test-${apiNumber}");
        variable("apiName", "VHost Test ${apiNumber}");

        $(echo("####### Importing unpublised API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("status", "unpublished");
        variable("vhost", "api123.customer.com");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/vhost/1_vhost-config.json");
        variable("expectedReturnCode", "0");
        swaggerImport.doExecute(context);

        $(echo("####### Validate unpublished API: '${apiName}' on path: '${apiPath}' is configured with V-Host #######"));
        $(http().client(apiManager).send().get("/proxies"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
            .expression("$.[?(@.path=='${apiPath}')].vhost", "api123.customer.com")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("status", "published");
        variable("vhost", "api123.customer.com");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/vhost/1_vhost-config.json");
        variable("expectedReturnCode", "0");
        swaggerImport.doExecute(context);

        $(echo("####### Validate published API: '${apiName}' on path: '${apiPath}' has V-Host configured #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "published")
            .expression("$.[?(@.id=='${apiId}')].vhost", "api123.customer.com")));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("status", "unpublished");
        variable("vhost", "api123.customer.com");
        variable("enforce", "true"); // as we are going back from published to unpublished
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/vhost/1_vhost-config.json");
        swaggerImport.doExecute(context);

        $(http().client(apiManager).send().get("/proxies/${apiId}"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "${status}")));
    }
}
