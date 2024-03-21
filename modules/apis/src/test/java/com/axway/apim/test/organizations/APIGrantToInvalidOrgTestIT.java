package com.axway.apim.test.organizations;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;

@ContextConfiguration(classes = {EndpointConfig.class})
public class APIGrantToInvalidOrgTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest(name = "APIGrantToInvalidOrgTestIT")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Tool must fail with a defined error, if a configured org is invalid");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/grant_invalid_org-api-${apiNumber}");
        variable("apiName", "Grant to invalid orgs API-${apiNumber}");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("orgName2", "Invalid Org 0815");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Perform the No-Change, as the additionally configured invalid org should not lead to a change #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("orgName2", "Invalid Org 0815");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));
    }
}
