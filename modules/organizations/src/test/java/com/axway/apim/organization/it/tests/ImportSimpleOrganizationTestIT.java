package com.axway.apim.organization.it.tests;

import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
import com.axway.apim.organization.OrganizationApp;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.JsonPathSupport;
import org.citrusframework.exceptions.ValidationException;
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
public class ImportSimpleOrganizationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    @CitrusTest
    @Test
    public void run(@Optional @CitrusResource TestContext context) {
        description("Import organization into API-Manager");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("orgName", "citrus:concat('My-Org-',  citrus:randomNumber(4))");

        variable("orgDescription", "A description for my org");
        // This test must be executed with an Admin-Account as we need to create a new organization
        //createVariable(PARAM_IGNORE_ADMIN_ACC, "fals");

        $(echo("####### Import organization: '${orgName}' #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/organization/orgImport/SingleOrganization.json",
            context, "orgs", true);
        $(testContext -> {
            String[] args = {"org", "import", "-c", updatedConfigFile, "-h", testContext.getVariable("apiManagerHost"), "-u",
                testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });


        $(echo("####### Validate organization: '${orgName}' has been imported #######"));
        $(http().client(apiManager).send().get("/organizations?field=name&op=eq&value=${orgName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${orgName}')].name", "@assertThat(hasSize(1))@")).extract(fromBody()
            .expression("$.[?(@.id=='${orgName}')].id", "orgId")));

        $(echo("####### Re-Import same organization - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"org", "import", "-c", updatedConfigFile, "-h", testContext.getVariable("apiManagerHost"), "-u",
                testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Change the description and import it again #######"));
        variable("orgDescription", "My changed org description");
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/organization/orgImport/SingleOrganization.json",
            context, "orgs", true);
        $(testContext -> {
            String[] args = {"org", "import", "-c", updatedConfigFile2, "-h", testContext.getVariable("apiManagerHost"), "-u",
                testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

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
            String[] args = {"org", "import", "-c", exportedOrgPath, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
    }
}
