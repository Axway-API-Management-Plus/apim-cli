package com.axway.apim.organization.it.tests;

import com.axway.apim.organization.it.ExportOrganizationTestAction;
import com.axway.apim.organization.it.ImportOrganizationTestAction;
import com.axway.apim.test.ImportTestAction;
import com.axway.apim.test.actions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test
public class ImportSimpleOrgAPIAccessTestIT extends TestNGCitrusTestRunner {

    private final ImportTestAction apiImport = new ImportTestAction();

    private static final String PACKAGE = "/com/axway/apim/organization/orgImport/";

    @CitrusTest
    @Test
    @Parameters("context")
    public void run(@Optional @CitrusResource TestContext context) {
        description("Import organization into API-Manager including API Access");
        ExportOrganizationTestAction exportApp = new ExportOrganizationTestAction(context);
        ImportOrganizationTestAction importApp = new ImportOrganizationTestAction(context);
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("orgName", "My-Org-" + importApp.getRandomNum());
        variable("orgDescription", "Org with API-Access");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));

        variable("apiPath", "/test-app-api1-${apiNumber}");
        variable("apiName", "Test-App-API1-${apiNumber}");
        variable("apiName1", "${apiName}");

        echo("####### Importing Test API 1: '${apiName}' on path: '${apiPath}' #######");
        createVariable(ImportTestAction.API_DEFINITION, PACKAGE + "petstore.json");
        createVariable(ImportTestAction.API_CONFIG, PACKAGE + "test-api-config.json");
        createVariable("expectedReturnCode", "0");
        apiImport.doExecute(context);

        echo("####### Extract ID of imported API 1: '${apiName}' on path: '${apiPath}' #######");
        http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId1"));

        variable("apiPath", "/test-app-api2-${apiNumber}");
        variable("apiName", "Test-App-API2-${apiNumber}");
        variable("apiName2", "${apiName}");

        echo("####### Importing Test API 2: '${apiName}' on path: '${apiPath}' #######");
        createVariable(ImportTestAction.API_DEFINITION, PACKAGE + "petstore.json");
        createVariable(ImportTestAction.API_CONFIG, PACKAGE + "test-api-config.json");
        createVariable("expectedReturnCode", "0");
        apiImport.doExecute(context);

        echo("####### Extract ID of imported API 2: '${apiName}' on path: '${apiPath}' #######");
        http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId2"));

        echo("####### Import organization to test: '${orgName}' #######");
        createVariable(TestParams.PARAM_CONFIGFILE, PACKAGE + "SingleOrgGrantAPIAccessTwoAPIs.json");
        createVariable(TestParams.PARAM_EXPECTED_RC, "0");
        importApp.doExecute(context);

        echo("####### Validate organization: '${orgName}' has been imported #######");
        http(builder -> builder.client("apiManager").send().get("/organizations?field=name&op=eq&value=${orgName}").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.name=='${orgName}')].name", "@assertThat(hasSize(1))@")
            .extractFromPayload("$.[?(@.name=='${orgName}')].id", "orgId"));

        echo("####### Validate organization: '${orgName}' (${orgId}) has access to the imported API 1 and 2 #######");
        http(builder -> builder.client("apiManager").send().get("/organizations/${orgId}/apis").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.apiId=='${apiId1}')].enabled", "true")
            .validate("$.[?(@.apiId=='${apiId2}')].enabled", "true"));

        echo("####### Re-Import same organization - Should be a No-Change #######");
        createVariable(TestParams.PARAM_EXPECTED_RC, "10");
        importApp.doExecute(context);

        echo("####### Re-Import same organization - But reduced API-Access to API 1 #######");
        createVariable(TestParams.PARAM_CONFIGFILE, PACKAGE + "SingleOrgGrantAPIAccessOneAPI.json");
        createVariable(TestParams.PARAM_EXPECTED_RC, "0");
        importApp.doExecute(context);

        echo("####### Validate organization: '${orgName}' (${orgId}) has access to the imported API 1 only #######");
        http(builder -> builder.client("apiManager").send().get("/organizations/${orgId}/apis").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.apiId=='${apiId1}')].enabled", "true")
            .validate("$.*.apiId", "@assertThat(not(containsString(${apiId2})))@"));

        echo("####### Export the organization #######");
        createVariable(TestParams.PARAM_TARGET, exportApp.getTestDirectory().getPath());
        createVariable(TestParams.PARAM_EXPECTED_RC, "0");
        createVariable(TestParams.PARAM_OUTPUT_FORMAT, "json");
        createVariable(TestParams.PARAM_NAME, "${orgName}");
        exportApp.doExecute(context);

        Assert.assertEquals(exportApp.getLastResult().getExportedFiles().size(), 1, "Expected to have one organization exported");
        String exportedConfig = exportApp.getLastResult().getExportedFiles().get(0);

        echo("####### Re-Import EXPORTED organization - Should be a No-Change #######");
        createVariable(TestParams.PARAM_CONFIGFILE, exportedConfig);
        createVariable("expectedReturnCode", "10");
        importApp.doExecute(context);
    }
}
