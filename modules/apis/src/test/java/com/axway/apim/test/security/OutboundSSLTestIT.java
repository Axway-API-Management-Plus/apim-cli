package com.axway.apim.test.security;

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
public class OutboundSSLTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Test-Case to validate Outbound SSL authentication");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/outbound-ssl-${apiNumber}");
        variable("apiName", "Outbound-SSL-${apiNumber}");

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/api_outbound-ssl.json");
        variable("state", "unpublished");
        variable("certFile", "/com/axway/apim/test/files/certificates/clientcert.pfx");
        variable("password", "axway");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "${state}")
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "ssl")
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.trustAll", true)
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.pfx", "@assertThat(startsWith(data:application/x-pkcs12;base64,MIIJ0QIBAzCCCZcGCSqGSIb3DQEHAaCCCYgEggmEMIIJgDCCBDcGCSqGSIb3DQEHBqCCBCgwggQk))@"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.trustAll", "trustAll")
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Execute a No-Change test #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/api_outbound-ssl.json");
        variable("state", "unpublished");
        variable("password", "axway");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Provide the wrong password to the keystore #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/api_outbound-ssl.json");
        variable("state", "unpublished");
        variable("password", "wrongpassword");
        variable("expectedReturnCode", "81");
        $(action(swaggerImport));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/api_outbound-ssl.json");
        variable("state", "unpublished");
        variable("certFile", "/com/axway/apim/test/files/certificates/clientcert2.p12");
        variable("password", "axway2");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate the Client-Certificate has been updated #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "${state}")
            .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
            .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "ssl")
            .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].parameters.pfx", "@assertThat(startsWith(data:application/x-pkcs12;base64,MIIJ2QIBAzCCCZ8GCSqGSIb3DQEHAaCCCZAEggmMMIIJiDCCBD8GCSqGSIb3DQEHBqCCBDAwggQs))@")));

    }
}
