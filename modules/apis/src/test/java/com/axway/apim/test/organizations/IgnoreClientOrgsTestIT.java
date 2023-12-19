package com.axway.apim.test.organizations;

import com.axway.apim.EndpointConfig;
import com.axway.apim.lib.CoreParameters.Mode;
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
public class IgnoreClientOrgsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest(name = "IgnoreClientOrgsTestIT")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("This test makes sure, no organizations have been granted permission.");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/ignore_client_orgs-api-${apiNumber}");
        variable("apiName", "Ignore Client orgs API-${apiNumber}");
        variable("testOrgName", "Org without permission ${apiNumber}");
        $(http().client(apiManager).send().post("/organizations").name("anotherOrgCreatedRequest").message()
            .header("Content-Type", "application/json")
            .body("{\"name\": \"${testOrgName}\", \"description\": \"Org without permission\", \"enabled\": true, \"development\": true }"));

        $(http().client(apiManager).receive().response(HttpStatus.CREATED)
            .message().type(MessageType.JSON).extract(jsonPath()
                .expression("$.name", "${testOrgName}"))
            .extract(fromBody()
                .expression("$.id", "noPermOrgId")));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("orgName2", "${testOrgName}");
        variable("clientOrgsMode", String.valueOf(Mode.ignore));
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been imported without an error (defined orgs are ignored) #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "published")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Validate second org has no permission #######"));
        $(http().client(apiManager).send().get("/organizations/${noPermOrgId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "@assertThat(not(containsString(${apiId})))@")));
    }
}
