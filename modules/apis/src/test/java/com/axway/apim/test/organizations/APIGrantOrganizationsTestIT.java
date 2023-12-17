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
public class APIGrantOrganizationsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


    @CitrusTest(name = "APIGrantOrganizationsTestIT")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import an API can grant access to a number of defined orgs");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/grant_org-api-${apiNumber}");
        variable("apiName", "Grant to some orgs API-${apiNumber}");
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "unpublished");
        variable("orgName", "${orgName}");
        variable("orgName2", "${orgName2}");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been imported without an error (defined orgs are ignored) #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("orgName2", "${orgName2}");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' is granted to defined organizations: '${orgName}', '${orgName2}' #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
            .expression("$.[?(@.id=='${apiId}')].state", "published")));

        $(http().client(apiManager).send().get("/organizations/${orgId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));

        $(http().client(apiManager).send().get("/organizations/${orgId2}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));

        $(echo("####### Execute the same definition - Tool must return with No-Change return code #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("orgName2", "${orgName2}");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Going back to unpublished forcing a breaking change #######"));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "unpublished");
        variable("orgName", "${orgName}");
        variable("orgName2", "${orgName2}");
        variable("enforce", "true");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been imported without an error in state unpublished #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")));
    }
}
