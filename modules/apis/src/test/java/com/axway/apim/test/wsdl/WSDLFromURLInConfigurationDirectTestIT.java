package com.axway.apim.test.wsdl;

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
public class WSDLFromURLInConfigurationDirectTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Validates a WSDL-File can be taken from a URL described in API json configuration.");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/direct-url-wsdl-in-configuration-${apiNumber}");
        variable("apiName", "Direct-URL-WSDL in configuration from URL-${apiNumber}");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time from URL #######"));
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/minimal-config-with-backendBasepath-and-apidefinition.json");
        variable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
        variable("backendBasepath", "https://any.server.com:7676");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));


        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported with correct backend base path #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
            .expression("$.[?(@.path=='${apiPath}')].serviceProfiles._default.basePath", "https://any.server.com:7676")).extract(fromBody() // Make sure the backend base path is configured for SOAP-Services as well
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Re-Import API with ID: '${apiId}' from URL without a change #######"));
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
        variable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
        variable("state", "unpublished");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Setting the status to Published #######"));
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
        variable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "published")));
    }
}
