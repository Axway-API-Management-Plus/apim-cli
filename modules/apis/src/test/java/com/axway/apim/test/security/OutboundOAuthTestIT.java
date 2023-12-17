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


@ContextConfiguration(classes = {EndpointConfig.class})
public class OutboundOAuthTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Test to validate API-Outbound-AuthN set to OAuth works as expected.");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/outbound-authn-oauth-test-${apiNumber}");
        variable("apiName", "Outbound AuthN OAuth Test ${apiNumber}");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with OAuth outbound config set #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/5_4_api_outbound-authn-oauth.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' with outbound security set to OAuth has been imported. #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[*].name", "@assertThat(hasSize(1))@") // Only one authn profile is expected!
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "oauth")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].isDefault", "true")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.providerProfile", "@assertThat(containsString(<key type='AuthProfilesGroup))@")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.ownerId", "TEST-SOMETHING")));

        $(echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/5_4_api_outbound-authn-oauth.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));
    }
}
