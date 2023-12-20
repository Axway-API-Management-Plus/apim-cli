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

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ChangeDevelopmentOrgTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Changing the Development Org (See issue #138 and using a toggle #263)");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/change-dev-org-api-${apiNumber}");
        variable("apiName", "Change the Dev-Org API-${apiNumber}");
        // This test must be executed with an Admin-Account as we need to flip the organization
        variable("useApiAdmin", "true"); // Use apiadmin account
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/dynamic-organization.json");
        variable("state", "unpublished");
        variable("testOrgName", "${orgName}");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate the imported APIs initialy belongs to '${orgName}' (${orgId}) #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
                .expression("$.[?(@.path=='${apiPath}')].organizationId", "${orgId}"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Try to Re-Import the same API with a changed development organization: '${orgName3}' (${orgId3}). This must FAIL as the parameter: changeOrganization is not set #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/dynamic-organization.json");
        variable("state", "unpublished");
        variable("testOrgName", "${orgName3}");
        variable("expectedReturnCode", "7"); // Without providing the Force-Organization-Flip-Toggle we expect an error
        $(action(swaggerImport));

        $(echo("####### Re-Import the same API with a changed development organization: '${orgName3}' (${orgId3}) #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/dynamic-organization.json");
        variable("state", "unpublished");
        variable("testOrgName", "${orgName3}");
        variable("changeOrganization", "true");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate the re-imported APIs now belongs to '${orgName3}' (${orgId3}) #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
                .expression("$.[?(@.path=='${apiPath}')].organizationId", "${orgId3}"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Perform a No-Change test! #######"));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/dynamic-organization.json");
        variable("state", "unpublished");
        variable("testOrgName", "${orgName3}");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));
    }
}
