package com.axway.apim.test.applications;

import com.axway.apim.APIExportApp;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

@Test(testName = "APIRevokeApplicationTestIT")

public class APIRevokeApplicationTestIT extends TestNGCitrusTestDesigner {

    @Autowired
    private ImportTestAction swaggerImport;

    @Value("${apiManagerHost}")
    private String host;


    @Value("${apiManagerUser}")
    private String username;

    @Value("${apiManagerPass}")
    private String password;


    @CitrusTest(name = "APIRevokeApplicationTestIT")
    public void run() {
        description("Import API and grant access to Application and revoke Application access");
        echo("#### Setup  Variables###");
        /* Org id and org name was set by pretest */
        String apiNumber = RandomNumberFunction.getRandomNumber(3, true);
        variable("apiNumber", apiNumber);
      //  variable("testOrgName", "${orgName}");
         variable("testOrgName", "grant_org-api-${apiNumber}-org");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiPath", "/grant_org-api-${apiNumber}");
        variable("apiName", "Grant to some orgs API-${apiNumber}");
        variable("appName", "Application API-${apiNumber}");

        echo("#### Create Organization  ###");
        http().client("apiManager")
            .send()
            .post("/organizations")
            .name("createOrganization")
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .payload("{\"name\": \"${testOrgName}\", \"description\": \"${testOrgName}\", \"enabled\": true, \"development\": true }");

        http().client("apiManager")
            .receive()
            .response(HttpStatus.CREATED).extractFromPayload("$.id", "testOrgId");


        echo("#### Create Application  ###");
        http().client("apiManager")
            .send()
            .post("/applications")
            .name("createApplication")
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .payload("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${testOrgId}\"}");

        http().client("apiManager")
            .receive()
            .response(HttpStatus.CREATED).extractFromPayload("$.id", "appId");

        http().client("apiManager")
            .send()
            .get()
            .path("/applications/${appId}")
            .name("organizationById")
            .header("Content-Type", "application/json");

        http().client("apiManager")
            .receive()
            .response(HttpStatus.OK);


        echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######");

        createVariable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        createVariable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/organizations/dynamic-organization.json");
        createVariable("state", "published");
        createVariable("expectedReturnCode", "0");
        action(swaggerImport);

        echo("####### Validate API: '${apiName}' has been imported without an error #######");
        http().client("apiManager")
            .send()
            .get("/proxies")
            .name("api")
            .header("Content-Type", "application/json");

        http().client("apiManager")
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .validate("$.[?(@.path=='${apiPath}')].state", "published")
            .extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");


        echo("### Grant Application ##");
        action(testContext -> {
            String[] args = {"api", "grant-access", "-h", host, "-u", username, "-p", password, "-n",
                testContext.getVariable("apiName"), "-orgName",
                testContext.getVariable("testOrgName"), "-appName",
                testContext.getVariable("appName"), "-force"};
            int returnCode = APIExportApp.grantAccess(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        http().client("apiManager")
            .send()
            .get("/applications/${appId}/apis")
            .name("listAppApiAccess")
            .header("Content-Type", "application/json");

        http().client("apiManager")
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .validate("$.[?(@.apiId=='${apiId}')].apiId", "${apiId}")
            .validate("$.[?(@.apiId=='${apiId}')].state", "approved")
            .validate("$.[?(@.apiId=='${apiId}')].enabled", "true");

        echo("### Revoke Application##");
        action(testContext -> {
            String[] args = {"api", "revoke-access", "-h", host, "-u", username, "-p", password, "-n",
                testContext.getVariable("apiName"), "-orgName",
                testContext.getVariable("testOrgName"), "-appName",
                testContext.getVariable("appName"), "-force"};
            int returnCode = APIExportApp.revokeAccess(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        http().client("apiManager")
            .send()
            .get("/applications/${appId}/apis")
            .name("listAppApiAccess")
            .header("Content-Type", "application/json");

        http().client("apiManager")
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .validate("$.[?(@.apiId=='${apiId}')]", "[]");
    }
}
