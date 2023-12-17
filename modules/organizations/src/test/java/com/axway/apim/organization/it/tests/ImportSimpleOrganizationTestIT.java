package com.axway.apim.organization.it.tests;

import com.axway.apim.EndpointConfig;
import com.axway.apim.organization.it.ExportOrganizationTestAction;
import com.axway.apim.organization.it.ImportOrganizationTestAction;
import com.axway.apim.test.actions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;



@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportSimpleOrganizationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    private static final String PACKAGE = "/com/axway/apim/organization/orgImport/";

    @CitrusTest
    @Test
    public void run(@Optional @CitrusResource TestContext context) {
        description("Import organization into API-Manager");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("orgName", "citrus:concat('My-Org-',  citrus:randomNumber(4))");

        variable("orgDescription", "A description for my org");
        // This test must be executed with an Admin-Account as we need to create a new organization
        //createVariable(PARAM_IGNORE_ADMIN_ACC, "fals");

        echo("####### Import organization: '${orgName}' #######");
        variable(TestParams.PARAM_CONFIGFILE, PACKAGE + "SingleOrganization.json");
        variable(TestParams.PARAM_EXPECTED_RC, "0");
        importApp.doExecute(context);

        echo("####### Validate organization: '${orgName}' has been imported #######");
        http(builder -> builder.client("apiManager").send().get("/organizations?field=name&op=eq&value=${orgName}").header("Content-Type", "application/json"));

        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.name=='${orgName}')].name", "@assertThat(hasSize(1))@")
            .extractFromPayload("$.[?(@.id=='${orgName}')].id", "orgId"));

        echo("####### Re-Import same organization - Should be a No-Change #######");
        variable(TestParams.PARAM_EXPECTED_RC, "10");
        importApp.doExecute(context);

        echo("####### Change the description and import it again #######");
        variable("orgDescription", "My changed org description");
        variable(TestParams.PARAM_EXPECTED_RC, "0");
        importApp.doExecute(context);

        echo("####### Export the organization #######");
        variable(TestParams.PARAM_TARGET, exportApp.getTestDirectory().getPath());
        variable(TestParams.PARAM_EXPECTED_RC, "0");
        variable(TestParams.PARAM_OUTPUT_FORMAT, "json");
        variable(TestParams.PARAM_NAME, "${orgName}");
        exportApp.doExecute(context);

        Assert.assertEquals(exportApp.getLastResult().getExportedFiles().size(), 1, "Expected to have one organization exported");
        String exportedConfig = exportApp.getLastResult().getExportedFiles().get(0);

        echo("####### Re-Import EXPORTED organization - Should be a No-Change #######");
        variable(TestParams.PARAM_CONFIGFILE, exportedConfig);
        variable("expectedReturnCode", "10");
        importApp.doExecute(context);
    }
}
