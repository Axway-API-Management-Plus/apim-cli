package com.axway.apim.test.basic;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class SpecialCharactersAPITestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import an API having some special characters in the Swagger & API-Config-File.");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/special-chars-${apiNumber}");
        variable("apiName", "Special-Chars-${apiNumber}");

        $(echo("####### Importing Special-Chars API: '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore-special-chars.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/special-chars-config.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));
        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathMessageValidationContext.Builder.jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}"))
            .extract(fromBody()
                //.validate("$.[?(@.path=='${apiPath}')].summary", "Ã�ï¿½Ã�Â´Ã�Â¿Ã�Â°Ã‘â€š Ã�Â¸Ã�Â»Ã�Â¸ Ã‘Æ’Ã�Â¼Ã‘â‚¬Ã�Â¸.")
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### RE-Importing same API: '${apiName}' on path: '${apiPath}' without changes. Expecting failure with RC 10. #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore-special-chars.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/special-chars-config.json");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));
    }

}
