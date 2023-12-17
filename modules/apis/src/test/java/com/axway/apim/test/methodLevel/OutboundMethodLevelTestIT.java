package com.axway.apim.test.methodLevel;

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
public class OutboundMethodLevelTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Validate Outbound Method level settings are applied");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/basic-outbound-method-level-api-${apiNumber}");
        variable("apiName", "Basic Outbound Method-Level-API-${apiNumber}");

        $(echo("####### Try to replicate an API having Outbound Method-Level settings declared #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/methodLevel/method-level-outboundbound-api-key.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("outboundProfileName", "HTTP Basic outbound Test ${apiNumber}");
        $(action(swaggerImport));

        $(echo("####### Validate the FE-API has been configured with outbound HTTP-Basic on method level #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "${state}")
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[?(@.name=='${outboundProfileName}')].type", "http_basic"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")
                .expression("$.[?(@.path=='${apiPath}')].apiId", "backendApiId")));

        $(http().client(apiManager).send().get("/proxies/${apiId}/operations"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.[?(@.name=='getOrderById')].id", "apiMethodId")));

        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].outboundProfiles.${apiMethodId}.authenticationProfile", "${outboundProfileName}")
            .expression("$.[?(@.id=='${apiId}')].outboundProfiles.${apiMethodId}.apiId", "${backendApiId}")));

        $(echo("####### Perform a No-Change #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/methodLevel/method-level-outboundbound-api-key.json");
        variable("state", "unpublished");
        variable("enforce", "false");
        variable("expectedReturnCode", "10");
        variable("outboundProfileName", "HTTP Basic outbound Test ${apiNumber}");
        $(action(swaggerImport));
    }
}
