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
public class OutboundBasicAuthTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Test to validate API-Outbound-AuthN set to HTTP-Basic.");

        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/outbound-authn-test-${apiNumber}");
        variable("apiName", "Outbound AuthN Test ${apiNumber}");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with standard HTTP-Basic outbound config set #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/5_3_api_outbound-authn-basic.json");
        variable("state", "unpublished");
        variable("username", "6xKFp3hL7znGM+sfb90NDjmt5t9mhvqR");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' with outbound security set to HTTP-Basic. #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "http_basic")
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].isDefault", "true")
                .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.username", "${username}"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
        variable("state", "unpublished");
        variable("username", "1234567890");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' with custom Outbound-Security-Profile set to HTTP-Basic. #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
                .expression("$.[?(@.id=='${apiId}')].state", "unpublished")
                .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[*].name", "@assertThat(hasSize(1))@") // Only one authn profile is expected!
                .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
                .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "http_basic")
                .expression("$.[?(@.id=='${apiId}')].outboundProfiles._default.authenticationProfile", "_default"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
        variable("state", "unpublished");
        variable("username", "1234567890");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Simulate a change to the outbound configuration in UNPUBLISHED mode, by changing the username #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
        variable("state", "unpublished");
        variable("username", "0987654321");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate the changed apiKey (username) is in place #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].id", "${apiId}")
            .expression("$.[?(@.id=='${apiId}')].state", "unpublished")
            .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
            .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "http_basic")
            .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].parameters.username", "${username}")));

        $(echo("####### Change API to status published: #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/5_api_outbound-basic.json");
        variable("state", "published");
        variable("username", "1234567890");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has status published. #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
                .expression("$.[?(@.id=='${apiId}')].state", "published")
                .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
                .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "http_basic")
                .expression("$.[?(@.id=='${apiId}')].authenticationProfiles[0].parameters.username", "${username}")
                .expression("$.[?(@.id=='${apiId}')].outboundProfiles._default.authenticationProfile", "_default"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Re-Import same API: '${apiName}' on path: '${apiPath}' with status published but NOW AN API-Key (default): #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/5_2_api_outbound-apikey.json");
        variable("state", "published");
        variable("apiKey", "1234567890");
        variable("enforce", "true");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' now configured with API-Key #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "published")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "apiKey")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
            .expression("$.[?(@.path=='${apiPath}')].outboundProfiles._default.authenticationProfile", "_default")));

        $(echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/5_2_api_outbound-apikey.json");
        variable("state", "published");
        variable("apiKey", "1234567890");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));
    }
}
