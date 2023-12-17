package com.axway.apim.organization.it.tests;

import com.axway.apim.EndpointConfig;
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
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
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
public class ImportSimpleOrgAPIAccessTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    private final ImportTestAction apiImport = new ImportTestAction();

    private static final String PACKAGE = "/com/axway/apim/organization/orgImport/";

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

        echo("####### Importing Test API 1: '${apiName}' on path: '${apiPath}' #######");
        variable(ImportTestAction.API_DEFINITION, PACKAGE + "petstore.json");
        variable(ImportTestAction.API_CONFIG, PACKAGE + "test-api-config.json");
        variable("expectedReturnCode", "0");
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
        variable(ImportTestAction.API_DEFINITION, PACKAGE + "petstore.json");
        variable(ImportTestAction.API_CONFIG, PACKAGE + "test-api-config.json");
        variable("expectedReturnCode", "0");
        apiImport.doExecute(context);

        echo("####### Extract ID of imported API 2: '${apiName}' on path: '${apiPath}' #######");
        http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId2"));

        echo("####### Import organization to test: '${orgName}' #######");
        variable(TestParams.PARAM_CONFIGFILE, PACKAGE + "SingleOrgGrantAPIAccessTwoAPIs.json");
        variable(TestParams.PARAM_EXPECTED_RC, "0");
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
        variable(TestParams.PARAM_EXPECTED_RC, "10");
        importApp.doExecute(context);

        echo("####### Re-Import same organization - But reduced API-Access to API 1 #######");
        variable(TestParams.PARAM_CONFIGFILE, PACKAGE + "SingleOrgGrantAPIAccessOneAPI.json");
        variable(TestParams.PARAM_EXPECTED_RC, "0");
        importApp.doExecute(context);

        echo("####### Validate organization: '${orgName}' (${orgId}) has access to the imported API 1 only #######");
        http(builder -> builder.client("apiManager").send().get("/organizations/${orgId}/apis").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.apiId=='${apiId1}')].enabled", "true")
            .validate("$.*.apiId", "@assertThat(not(containsString(${apiId2})))@"));

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
