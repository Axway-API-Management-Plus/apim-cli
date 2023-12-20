package com.axway.apim.test.applications;

import com.axway.apim.APIExportApp;
import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
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
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})

public class APIRevokeApplicationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @Value("${apiManagerHost}")
    private String host;


    @Value("${apiManagerUser}")
    private String username;

    @Value("${apiManagerPass}")
    private String password;


    @CitrusTest(name = "APIRevokeApplicationTestIT")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import API and grant access to Application and revoke Application access");
        $(echo("#### Setup  Variables###"));
        /* Org id and org name was set by pretest */
        String apiNumber = RandomNumberFunction.getRandomNumber(3, true);
        variable("apiNumber", apiNumber);
      //  variable("testOrgName", "${orgName}");
         variable("testOrgName", "grant_org-api-${apiNumber}-org");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiPath", "/grant_org-api-${apiNumber}");
        variable("apiName", "Grant to some orgs API-${apiNumber}");
        variable("appName", "Application API-${apiNumber}");

        $(echo("#### Create Organization  ###"));
        $(http().client(apiManager).send().post("/organizations").name("createOrganization").message()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body("{\"name\": \"${testOrgName}\", \"description\": \"${testOrgName}\", \"enabled\": true, \"development\": true }"));

        $(http().client(apiManager).receive()
            .response(HttpStatus.CREATED).message().extract(fromBody().expression("$.id", "testOrgId")));

        $(echo("#### Create Application  ###"));
        $(http().client(apiManager).send().post("/applications").name("createApplication").message()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${testOrgId}\"}"));
        $(http().client(apiManager).receive()
            .response(HttpStatus.CREATED).message().extract(fromBody().expression("$.id", "appId")));

        $(http().client(apiManager).send().get().path("/applications/${appId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK));


        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/dynamic-organization.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has been imported without an error #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "published"))
            .extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("### Grant Application ##"));
        $(action(testContext -> {
            String[] args = {"api", "grant-access", "-h", host, "-u", username, "-p", password, "-n",
                testContext.getVariable("apiName"), "-orgName",
                testContext.getVariable("testOrgName"), "-appName",
                testContext.getVariable("appName"), "-force"};
            int returnCode = APIExportApp.grantAccess(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        }));
        $(http().client(apiManager).send().get("/applications/${appId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')].apiId", "${apiId}")
            .expression("$.[?(@.apiId=='${apiId}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId}')].enabled", "true")));

        $(echo("### Revoke Application##"));
        $(action(testContext -> {
            String[] args = {"api", "revoke-access", "-h", host, "-u", username, "-p", password, "-n",
                testContext.getVariable("apiName"), "-orgName",
                testContext.getVariable("testOrgName"), "-appName",
                testContext.getVariable("appName"), "-force"};
            int returnCode = APIExportApp.revokeAccess(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        }));
        $(http().client(apiManager).send().get("/applications/${appId}/apis").name("listAppApiAccess"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.apiId=='${apiId}')]", "[]")));
    }
}
