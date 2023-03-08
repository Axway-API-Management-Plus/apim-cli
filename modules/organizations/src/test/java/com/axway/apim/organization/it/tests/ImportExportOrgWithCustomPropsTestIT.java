package com.axway.apim.organization.it.tests;

import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.organization.it.ExportOrganizationTestAction;
import com.axway.apim.organization.it.ImportOrganizationTestAction;
import com.axway.apim.test.actions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

@Test
public class ImportExportOrgWithCustomPropsTestIT extends TestNGCitrusTestRunner {

    private static final String PACKAGE = "/com/axway/apim/organization/orgImport/";

    ObjectMapper mapper = new ObjectMapper();

    @CitrusTest
    @Test
    @Parameters("context")
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        description("Import organization with custom properties into API-Manager");
        ExportOrganizationTestAction exportApp = new ExportOrganizationTestAction(context);
        ImportOrganizationTestAction importApp = new ImportOrganizationTestAction(context);
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("orgName", "My-Custom-Prop-Org-" + importApp.getRandomNum());
        variable("orgDescription", "A description for my custom properties org");

        echo("####### Import organization: '${orgName}' with custom properties #######");
        createVariable(TestParams.PARAM_CONFIGFILE, PACKAGE + "OrgWithCustomProps.json");
        createVariable(TestParams.PARAM_EXPECTED_RC, "0");
        importApp.doExecute(context);

        echo("####### Validate organization: '${orgName}' has been imported incl. custom properties #######");
        http(builder -> builder.client("apiManager").send().get("/organizations?field=name&op=eq&value=${orgName}").header("Content-Type", "application/json"));

        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.name=='${orgName}')].name", "@assertThat(hasSize(1))@")
            .validate("$.[?(@.name=='${orgName}')].orgCustomProperty1", "Org custom value 1")
            .validate("$.[?(@.name=='${orgName}')].orgCustomProperty2", "2")
            .validate("$.[?(@.name=='${orgName}')].orgCustomProperty3", "true")
            .extractFromPayload("$.[?(@.id=='${orgName}')].id", "orgId"));

        echo("####### Re-Import same organization - Should be a No-Change #######");
        createVariable(TestParams.PARAM_EXPECTED_RC, "10");
        importApp.doExecute(context);

        echo("####### Export the organization: '${orgName}' - To validate custom properties are exported #######");
        createVariable(TestParams.PARAM_TARGET, exportApp.getTestDirectory().getPath());
        createVariable(TestParams.PARAM_EXPECTED_RC, "0");
        createVariable(TestParams.PARAM_OUTPUT_FORMAT, "json");
        createVariable(TestParams.PARAM_NAME, "${orgName}");
        exportApp.doExecute(context);

        Assert.assertEquals(exportApp.getLastResult().getExportedFiles().size(), 1, "Expected to have one organization exported");
        String exportedConfig = exportApp.getLastResult().getExportedFiles().get(0);

        ClientApplication exportedOrg = mapper.readValue(new File(exportedConfig), ClientApplication.class);

        Assert.assertNotNull(exportedOrg.getCustomProperties(), "Exported organization must have custom properties");
        Assert.assertEquals(exportedOrg.getCustomProperties().size(), 3, "Exported organization must have 3 custom properties");
        Assert.assertEquals(exportedOrg.getCustomProperties().get("orgCustomProperty1"), "Org custom value 1");
        Assert.assertEquals(exportedOrg.getCustomProperties().get("orgCustomProperty2"), "2");
        Assert.assertEquals(exportedOrg.getCustomProperties().get("orgCustomProperty3"), "true");

        echo("####### Re-Import EXPORTED organization - Should be a No-Change #######");
        createVariable(TestParams.PARAM_CONFIGFILE, exportedConfig);
        createVariable("expectedReturnCode", "10");
        importApp.doExecute(context);
    }
}
