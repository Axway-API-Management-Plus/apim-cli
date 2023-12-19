package com.axway.apim.organization.it.tests;

import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.organization.OrganizationApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
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
import java.io.IOException;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportExportOrgWithCustomPropsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    ObjectMapper mapper = new ObjectMapper();

    @CitrusTest
    @Test
    public void importAndExportOrg(@Optional @CitrusResource TestContext context) throws IOException {
        description("Import organization with custom properties into API-Manager");
        variable("orgName", "citrus:concat('My-Custom-Prop-Org-',  citrus:randomNumber(4))");
        variable("orgDescription", "A description for my custom properties org");

        $(echo("####### Import organization: '${orgName}' with custom properties #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/organization/orgImport/OrgWithCustomProps.json",
            context, "orgs", true);
        $(testContext -> {
            String[] args = {"org", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate organization: '${orgName}' has been imported incl. custom properties #######"));
        $(http().client(apiManager).send().get("/organizations?field=name&op=eq&value=${orgName}").message());
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.name=='${orgName}')].name", "@assertThat(hasSize(1))@")
            .expression("$.[?(@.name=='${orgName}')].orgCustomProperty1", "Org custom value 1")
            .expression("$.[?(@.name=='${orgName}')].orgCustomProperty2", "2")
            .expression("$.[?(@.name=='${orgName}')].orgCustomProperty3", "true")).extract(fromBody()
            .expression("$.[?(@.id=='${orgName}')].id", "orgId")));

        $(echo("####### Re-Import same organization - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"org", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = OrganizationApp.importOrganization(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        $(echo("####### Export the organization: '${orgName}' - To validate custom properties are exported #######"));
        String tmpDirPath = TestUtils.createTestDirectory("orgs").getPath();
        String orgName = context.replaceDynamicContentInString("${orgName}");
        $(testContext -> {
            String[] args = {"org", "get", "-n", orgName, "-o", "json", "-t", tmpDirPath, "-deleteTarget", "-h",
                testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"),
                "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = OrganizationApp.exportOrgs(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        Assert.assertEquals(new File(tmpDirPath, orgName).listFiles().length, 1, "Expected to have one organization exported");
        String exportedOrgPath = new File(tmpDirPath, orgName).listFiles()[0].getPath();
        ClientApplication exportedOrg = mapper.readValue(new File(exportedOrgPath), ClientApplication.class);
        Assert.assertNotNull(exportedOrg.getCustomProperties(), "Exported organization must have custom properties");
        Assert.assertEquals(exportedOrg.getCustomProperties().size(), 3, "Exported organization must have 3 custom properties");
        Assert.assertEquals(exportedOrg.getCustomProperties().get("orgCustomProperty1"), "Org custom value 1");
        Assert.assertEquals(exportedOrg.getCustomProperties().get("orgCustomProperty2"), "2");
        Assert.assertEquals(exportedOrg.getCustomProperties().get("orgCustomProperty3"), "true");

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
