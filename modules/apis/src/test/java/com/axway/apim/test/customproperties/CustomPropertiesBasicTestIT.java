package com.axway.apim.test.customproperties;

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
public class CustomPropertiesBasicTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest(name = "CustomPropertiesBasicTest")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Importing & validating custom-properties");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/api-custom-prop-test-${apiNumber}");
        variable("apiName", "API Custom-Properties Test ${apiNumber}");
        variable("status", "unpublished");
        $(echo("####### 1. Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("status", "unpublished");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/customproperties/1_custom-properties-config_IT.json");
        variable("customProperty1", "Test-Input 1");
        variable("customProperty2", "1");
        variable("customProperty3", "true");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
                .expression("$.[?(@.path=='${apiPath}')].customProperty1", "Test-Input 1")
                .expression("$.[?(@.path=='${apiPath}')].customProperty2", "1")
                .expression("$.[?(@.path=='${apiPath}')].customProperty3", "true"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### 2. Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("status", "published");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/customproperties/1_custom-properties-config_IT.json");
        variable("customProperty1", "Test-Input 0815");
        variable("customProperty2", "2");
        variable("customProperty3", "false");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        // API-ID must be the same, as we changed an unpublished API!
        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "published")
            .expression("$.[?(@.id=='${apiId}')].customProperty1", "Test-Input 0815")
            .expression("$.[?(@.id=='${apiId}')].customProperty2", "2")
            .expression("$.[?(@.id=='${apiId}')].customProperty3", "false")));

        $(echo("####### 3. Re-Import with No-Change #######"));
        variable("status", "published");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/customproperties/1_custom-properties-config_IT.json");
        variable("customProperty1", "Test-Input 0815");
        variable("customProperty2", "2");
        variable("customProperty3", "false");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        // Finally, Change the Custom-Prop of a published API, which will lead to a new API-ID!
        $(echo("####### 4. Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("status", "published");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/customproperties/1_custom-properties-config_IT.json");
        variable("customProperty1", "Test-Input Final");
        variable("customProperty2", "3");
        variable("customProperty3", "false");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "published")
            .expression("$.[?(@.path=='${apiPath}')].customProperty1", "Test-Input Final")
            .expression("$.[?(@.path=='${apiPath}')].customProperty2", "3")
            .expression("$.[?(@.path=='${apiPath}')].customProperty3", "false")));
    }
}
