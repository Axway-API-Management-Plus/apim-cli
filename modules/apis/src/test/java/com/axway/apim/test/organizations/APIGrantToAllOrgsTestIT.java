package com.axway.apim.test.organizations;

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
public class APIGrantToAllOrgsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest(name = "APIGrantToAllOrgsTestIT")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Tool must fail is not all organizations are asigned to an API.");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/grant_all_orgs-api-${apiNumber}");
        variable("apiName", "Grant access to all orgs API-${apiNumber}");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-all-orgs.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been imported and get generated API-ID #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "published"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Re-Import without a change. #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-all-orgs.json");
        variable("state", "published");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' reduce the number of orgs to TWO #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("orgName2", "${orgName2}");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(http().client(apiManager).send().get("/organizations/${orgId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));

        $(http().client(apiManager).send().get("/organizations/${orgId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));
    }
}
