package com.axway.apim.test.orgadmin;

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


@ContextConfiguration(classes = {EndpointConfig.class})
public class OrgAdminCustomPoliciesTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


    @CitrusTest(name = "OrgAdminTriesToPublishTestIT")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("OrgAdmin wants to use a custom policy.");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/org-admin-published-${apiNumber}");
        variable("apiName", "OrgAdmin-Published-${apiNumber}");
        $(echo("####### Calling the tool with a Non-Admin-User. #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/policies/1_request-policy.json");
        variable("requestPolicy", "Request policy 1");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));
        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
            .expression("$.[?(@.path=='${apiPath}')].outboundProfiles._default.requestPolicy", "@assertThat(containsString(Request policy 1))@")));
    }
}
