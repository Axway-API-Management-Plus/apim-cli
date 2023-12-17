package com.axway.apim.test.cacerts;

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
public class BasicCaCertTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest(name = "BasicCaCertTest")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Test to validate, that Certificates will be imported");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/cacerts-test-${apiNumber}");
        variable("apiName", "Certificates Test ${apiNumber}");
        variable("status", "unpublished");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/cacerts/1_basic_certs.json");
        variable("certFile4", "/com/axway/apim/test/files/cacerts/../certificates/DSTRootCAX3.crt");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' with correct settings #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
                .expression("$.[?(@.path=='${apiPath}')].caCerts[?(@.md5Fingerprint=='41:03:52:DC:0F:F7:50:1B:16:F0:02:8E:BA:6F:45:C5')].name", "@assertThat(containsString(Digital Signature Trust Co))@"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Simulate Re-Import without changes #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/cacerts/1_basic_certs.json");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Re-Import with a new certificate #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/cacerts/1_basic_certs.json");
        variable("certFile4", "/com/axway/apim/test/files/cacerts/../certificates/GlobalSignRootCA-R2.crt");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate the new certificate has been replaced #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
            .expression("$.[?(@.path=='${apiPath}')].caCerts[?(@.md5Fingerprint=='94:14:77:7E:3E:5E:FD:8F:30:BD:41:B0:CF:E7:D0:30')].name", "@assertThat(containsString(GlobalSign Root CA - R2))@")));
    }
}
