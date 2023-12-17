package com.axway.apim.test.policies;

import com.axway.apim.EndpointConfig;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class RequestPolicyTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


    @CitrusTest(name = "RequestPolicyTest")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Test a Request-Policy");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/request-policy-test-${apiNumber}");
        variable("apiName", "Request Policy Test ${apiNumber}");
        variable("status", "unpublished");

        $(echo("####### Initially add API: '${apiName}' on path: '${apiPath}' without any custom policy #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
        variable(ImportTestAction.STATE, "unpublished");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has imported with correct settings #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Re-Importing API: '${apiName}' on path: '${apiPath}' including a Request-Policy #######"));
        variable("requestPolicy", "Request policy 1");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/policies/1_request-policy.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
            .expression("$.[?(@.path=='${apiPath}')].outboundProfiles._default.requestPolicy", "@assertThat(containsString(Request policy 1))@")));
    }
}
