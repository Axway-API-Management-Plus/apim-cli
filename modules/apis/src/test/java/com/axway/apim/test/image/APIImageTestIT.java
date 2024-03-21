package com.axway.apim.test.image;

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
public class APIImageTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import an API including an image!");
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/my-image-api-${apiNumber}");
        variable("apiName", "My-Image-API-${apiNumber}");
        variable("state", "unpublished");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/image/2_image_included_flex_state.json");
        variable("image", "/com/axway/apim/test/files/basic/API-Logo.jpg");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
                .expression("$.[?(@.path=='${apiPath}')].image", "@assertThat(containsString(/image))@")) // Just checking there is at least an image
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Doing the same again must lead to a No-Change #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/image/2_image_included_flex_state.json");
        variable("image", "/com/axway/apim/test/files/basic/API-Logo.jpg");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Check, if a another image is realized as a change #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/image/2_image_included_flex_state.json");
        variable("image", "/com/axway/apim/test/files/basic/otherAPIImage.jpg");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Doing the same again must lead to a No-Change #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/image/2_image_included_flex_state.json");
        variable("image", "/com/axway/apim/test/files/basic/otherAPIImage.jpg");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Re-Import with a 798KB Image #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/image/2_image_included_flex_state.json");
        variable("image", "/com/axway/apim/test/files/basic/LargeImage.jpg");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Check for a No-Change with the 798KB Image #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/image/2_image_included_flex_state.json");
        variable("image", "/com/axway/apim/test/files/basic/LargeImage.jpg");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Finally try to delete this API (See issue: #188) #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/image/2_image_included_flex_state.json");
        variable("image", "/com/axway/apim/test/files/basic/otherAPIImage.jpg");
        variable("state", "deleted");
        variable("enforce", "true");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));
    }
}
