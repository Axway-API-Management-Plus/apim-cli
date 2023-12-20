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
public class OutboundNoAuthWithPoliciesTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Test to validate behavior if OutboundProfiles are set, but no AuthN-Profile. It should result into no AuthN.");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/issue-133-noauth-${apiNumber}");
        variable("apiName", "Issue 133 No-Auth ${apiNumber}");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' Outbound-Profiles (for policies) not containing a AuthN-Profile. #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/issue-133-outbound-profile-no-auth.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' with outbound security set to No-AuthN as this should be the default. #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "none")
            .expression("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].isDefault", "true")
            .expression("$.[?(@.path=='${apiPath}')].outboundProfiles._default.authenticationProfile", "_default")
            .expression("$.[?(@.path=='${apiPath}')].outboundProfiles._default.routeType", "policy")));

        $(echo("####### No-Change test for '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/issue-133-outbound-profile-no-auth.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));
    }
}
