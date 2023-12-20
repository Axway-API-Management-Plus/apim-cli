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
public class APIDescriptionChangeTypeTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest(name = "APIDescriptionChangeTypeTest")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Tests, that the description type can be changed");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/description-api-${apiNumber}");
        variable("apiName", "Description-API-${apiNumber}");


        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/description/1_api_with_manual_description.json");
        variable("state", "published");
        variable("descriptionType", "manual");
        variable("descriptionManual", "This is my manual markdown description!");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has a manual description configured #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "published")
                .expression("$.[?(@.path=='${apiPath}')].descriptionType", "manual")
                .expression("$.[?(@.path=='${apiPath}')].descriptionManual", "This is my manual markdown description!"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/description/1_api_with_url_description.json");
        variable("state", "published");
        variable("descriptionType", "url");
        variable("descriptionUrl", "https://any.url.com/serves/my/docu.md");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));


        $(echo("####### Validate API: '${apiName}' has a manual description configured - Same API-ID #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "published")
            .expression("$.[?(@.id=='${apiId}')].descriptionType", "url")
            .expression("$.[?(@.id=='${apiId}')].descriptionUrl", "https://any.url.com/serves/my/docu.md")));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/description/1_api_with_markdown_description.json");
        variable("state", "published");
        variable("descriptionType", "markdown");
        variable("descriptionMarkdown", "${//env.DOCUMENTS//}/api/docu.md");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate description is now set to markdown - Same API-ID #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "published")
            .expression("$.[?(@.id=='${apiId}')].descriptionType", "markdown")
            .expression("$.[?(@.id=='${apiId}')].descriptionMarkdown", "${//env.DOCUMENTS//}/api/docu.md")));
    }

}
