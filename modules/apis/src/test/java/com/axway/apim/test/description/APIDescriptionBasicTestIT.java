package com.axway.apim.test.description;

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
public class APIDescriptionBasicTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest(name = "APIDescriptionBasicTest")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import an API with manual description first!");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/description-api-${apiNumber}");
        variable("apiName", "Description-API-${apiNumber}");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/description/1_api_with_manual_description.json");
        variable("state", "published");
        variable("descriptionType", "manual");
        variable("descriptionManual", "This is my markdown description test!");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has a manual description configured #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "published")
                .expression("$.[?(@.path=='${apiPath}')].descriptionType", "manual")
                .expression("$.[?(@.path=='${apiPath}')].descriptionManual", "This is my markdown description test!"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/description/1_api_with_manual_description.json");
        variable("state", "published");
        variable("descriptionType", "manual");
        variable("descriptionManual", "This is my markdown description test slightly updated!");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has a manual description configured - Same API-ID #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "published")
            .expression("$.[?(@.id=='${apiId}')].descriptionType", "manual")
            .expression("$.[?(@.id=='${apiId}')].descriptionManual", "This is my markdown description test slightly updated!")));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/description/1_api_with_manual_description.json");
        variable("state", "published");
        variable("descriptionType", "original");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate description is back to original - Same API-ID #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "published")
            .expression("$.[?(@.id=='${apiId}')].descriptionType", "original")));
    }

}
