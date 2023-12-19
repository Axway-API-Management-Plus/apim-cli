package com.axway.apim.organization.it.tests;

import com.axway.apim.APIImportApp;
import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
import com.axway.apim.organization.OrganizationApp;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.JsonPathSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.io.File;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportSimpleOrgAPIAccessTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;


    @CitrusTest
    @Test
    public void run(@Optional @CitrusResource TestContext context) {
        description("Import organization into API-Manager including API Access");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("orgName", "citrus:concat('My-Org-',  citrus:randomNumber(4))");
        variable("orgDescription", "Org with API-Access");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("apiPath", "/test-app-api1-${apiNumber}");
        variable("apiName", "Test-App-API1-${apiNumber}");
        variable("apiName1", "${apiName}");
        $(echo("####### Importing Test API 1: '${apiName}' on path: '${apiPath}' #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/organization/orgImport/test-api-config.json",
            context, "orgs", true);
        String specFile = TestUtils.createTestConfig("/com/axway/apim/organization/orgImport/petstore.json",
            context, "orgs", false);

        $(testContext -> {
            String[] args = {"api", "import", "-c", updatedConfigFile, "-a", specFile, "-h",
                testContext.getVariable("apiManagerHost"), "-u", testContext.getVariable("apiManagerUser"),
                "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = APIImportApp.importAPI(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        $(echo("####### Extract ID of imported API 1: '${apiName}' on path: '${apiPath}' #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId1")));

        variable("apiPath", "/test-app-api2-${apiNumber}");
        variable("apiName", "Test-App-API2-${apiNumber}");
        variable("apiName2", "${apiName}");

        $(echo("####### Importing Test API 2: '${apiName}' on path: '${apiPath}' #######"));
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/organization/orgImport/test-api-config.json",
            context, "orgs", true);
        $(testContext -> {
            String[] args = {"api", "import", "-c", updatedConfigFile2, "-a", specFile, "-h", testContext.getVariable("apiManagerHost"),
                "-u", testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Extract ID of imported API 2: '${apiName}' on path: '${apiPath}' #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId2")));

        $(echo("####### Import organization to test: '${orgName}' #######"));

        $(echo("####### Import organization: '${orgName}' #######"));
        String updatedConfigFile3 = TestUtils.createTestConfig("/com/axway/apim/organization/orgImport/SingleOrgGrantAPIAccessTwoAPIs.json",
            context, "orgs", true);
        $(testContext -> {
            String[] args = {"org", "import", "-c", updatedConfigFile3, "-h", testContext.getVariable("apiManagerHost"), "-u",
                testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        $(echo("####### Validate organization: '${orgName}' has been imported #######"));
        $(http().client(apiManager).send().get("/organizations?field=name&op=eq&value=${orgName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${orgName}')].name", "@assertThat(hasSize(1))@")).extract(fromBody()
            .expression("$.[?(@.name=='${orgName}')].id", "orgId")));

        $(echo("####### Validate organization: '${orgName}' (${orgId}) has access to the imported API 1 and 2 #######"));
        $(http().client(apiManager).send().get("/organizations/${orgId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.apiId=='${apiId1}')].enabled", "true")
            .expression("$.[?(@.apiId=='${apiId2}')].enabled", "true")));

        $(echo("####### Re-Import same organization - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"org", "import", "-c", updatedConfigFile, "-h", testContext.getVariable("apiManagerHost"), "-u",
                testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        $(echo("####### Re-Import same organization - But reduced API-Access to API 1 #######"));
        String updatedConfigFile4 = TestUtils.createTestConfig("/com/axway/apim/organization/orgImport/SingleOrgGrantAPIAccessOneAPI.json", context, "orgs", true);
        $(testContext -> {
            String[] args = {"org", "import", "-c", updatedConfigFile4, "-h", testContext.getVariable("apiManagerHost"), "-u",
                testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate organization: '${orgName}' (${orgId}) has access to the imported API 1 only #######"));
        $(http().client(apiManager).send().get("/organizations/${orgId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.apiId=='${apiId1}')].enabled", "true")
            .expression("$.*.apiId", "@assertThat(not(containsString(${apiId2})))@")));

        $(echo("####### Export the organization #######"));

        String tmpDirPath = TestUtils.createTestDirectory("orgs").getPath();
        String orgName = context.replaceDynamicContentInString("${orgName}");
        $(testContext -> {
            String[] args = {"org", "get", "-n", orgName, "-t", tmpDirPath, "-deleteTarget", "-h", testContext.getVariable("apiManagerHost"), "-u",
                testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass"), "-o", "json"};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        Assert.assertEquals(new File(tmpDirPath, orgName).listFiles().length, 1, "Expected to have one organization exported");
        String exportedOrgPath = new File(tmpDirPath, orgName).listFiles()[0].getPath();

        $(echo("####### Re-Import EXPORTED organization - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"org", "import", "-c", exportedOrgPath, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
    }
}
