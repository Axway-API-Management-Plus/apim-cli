package com.axway.apim.test.organizations;

import com.axway.apim.APIExportApp;
import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class APIRevokeOrganizationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


    @Value("${apiManagerHost}")
    private String host;

    @Value("${apiManagerUser}")
    private String username;

    @Value("${apiManagerPass}")
    private String password;


    @CitrusTest(name = "APIRevokeOrganizationsTestIT")
    @Test
    public void run(@Optional @CitrusResource TestContext context) {
        ImportTestAction swaggerImport = new ImportTestAction();

        description("Import API and grant access to Organization and revoke Organization access");
        $(echo("#### Setup  Variables###"));

        String apiNumber = RandomNumberFunction.getRandomNumber(3, true);
        variable("apiNumber", apiNumber);
        variable("testOrgName", "${orgName}");
        // variable("orgNameTest", "grant_org-api-${apiNumber}-org");
        context.setVariable("orgNameGrantRevoke", "grant_org-api-" + apiNumber + "-org");
        context.setVariable("apiName", "Grant to some orgs API-" + apiNumber);
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiPath", "/grant_org-api-${apiNumber}");
        variable("apiName", "Grant to some orgs API-${apiNumber}");
        String orgName = context.getVariable("orgNameGrantRevoke");
        String apiName = context.getVariable("apiName");

        $(echo("#### Create Organization###"));
        $(http().client(apiManager)
            .send()
            .post("/organizations")
            .name("createOrg").message()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body("{\"name\": \"${orgNameGrantRevoke}\", \"description\": \"Test Org ${orgNameGrantRevoke}\", \"enabled\": true, \"development\": true }"));

        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "orgId")));


        $(http().client(apiManager).send().get().path("/organizations/${orgId}").name("organizationById"));
        $(http().client(apiManager).receive().response(HttpStatus.OK));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######"));

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/dynamic-organization.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been imported without an error #######"));
        $(http().client("apiManager").send().get("/proxies"));


        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "published"))
            .extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));


        $(echo("### Grant Organization##"));
        $(action(testContext -> {
            String[] args = {"api", "grant-access", "-h", host, "-u", username, "-p", password, "-n", apiName, "-orgName", orgName, "-force"};
            int returnCode = APIExportApp.grantAccess(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        }));
        $(http().client(apiManager).send().get("/organizations/${orgId}/apis").name("listOrgApiAccess"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].apiId", "${apiId}")
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));

        $(echo("### Revoke Organization##"));
        $(action(testContext -> {
            String[] args = {"api", "revoke-access", "-h", host, "-u", username, "-p", password, "-n", apiName, "-orgName", orgName, "-force"};
            int returnCode = APIExportApp.revokeAccess(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        }));
        $(http().client(apiManager).send().get("/organizations/${orgId}/apis").name("listOrgApiAccess"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')]", "[]")));

    }
}
