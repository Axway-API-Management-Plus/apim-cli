package com.axway.apim.test.cors;

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


@ContextConfiguration(classes = {EndpointConfig.class})
public class CorsProfileBasicTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest(name = "CorsProfileBasicTest")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Importing & validating CORS-Profile");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/api-cors-profile-test-${apiNumber}");
        variable("apiName", "API CORS-Profile Test ${apiNumber}");
        variable("status", "unpublished");


        $(echo("####### 1. Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("status", "unpublished");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/cors/1_api-with_default_cors.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
            .expression("$.[?(@.path=='${apiPath}')].corsProfiles.[?(@.name=='New CORS Profile')].allowedHeaders[0]", "Authorization")
            .expression("$.[?(@.path=='${apiPath}')].corsProfiles.[?(@.name=='New CORS Profile')].exposedHeaders[0]", "via")
            .expression("$.[?(@.path=='${apiPath}')].inboundProfiles._default.corsProfile", "New CORS Profile")));
    }
}
