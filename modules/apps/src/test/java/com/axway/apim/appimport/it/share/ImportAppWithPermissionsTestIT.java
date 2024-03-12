package com.axway.apim.appimport.it.share;

import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.ApplicationExportApp;
import com.axway.apim.appimport.ClientApplicationImportApp;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;
import java.net.URLEncoder;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportAppWithPermissionsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    @CitrusTest
    @Test
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        description("Import application incl. shares into API-Manager");
        ObjectMapper mapper = new ObjectMapper();
        String no =  RandomNumberFunction.getRandomNumber(4, true);
        variable("username1", "User-A-" + no);
        variable("username2", "User-B-" + no);
        variable("username3", "User-C-" + no);
        // Get organization name
        String orgName = URLEncoder.encode(context.getVariable("orgName"), "UTF-8");
        $(http().client(apiManager).send().get("/organizations?field=name&op=eq&value="+orgName));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[0].name", "@assertThat(hasSize(1))@")).extract(fromBody()
            .expression("$.[0].id", "orgId")));
        $(echo("####### Organization id -  ${orgId} #######"));
        $(http().client(apiManager).send().post("/users").message().header("Content-Type", "application/json")
            .body("{\"loginName\":\"${username1}\",\"name\":\"${username1}\",\"email\":\"${username1}@company.com\",\"role\":\"oadmin\",\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "userId-1")));
        $(echo("####### Created Test-User 1 to share with: '${username1}' (${userId-1}) #######"));

        $(http().client(apiManager).send().post("/users").message().header("Content-Type", "application/json")
            .body("{\"loginName\":\"${username2}\",\"name\":\"${username2}\",\"email\":\"${username2}@company.com\",\"role\":\"oadmin\",\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "userId-2")));
        $(echo("####### Created Test-User 2 to share with: '${username2}' (${userId-2}) #######"));

        $(http().client(apiManager).send().post("/users").message().header("Content-Type", "application/json")
            .body("{\"loginName\":\"${username3}\",\"name\":\"${username3}\",\"email\":\"${username3}@company.com\",\"role\":\"oadmin\",\"organizationId\":\"${orgId}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "userId-3")));
        $(echo("####### Created Test-User 3 to share with: '${username3}' (${userId-3}) #######"));

        variable("appName", "Shared-App-" + no);

        $(echo("####### Import application: '${appName}' #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/appPermissions/AppWith2Permissions.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.getVariable("apiManagerHost"),
                "-u", testContext.getVariable("oadminUsername1"), "-p", testContext.getVariable("oadminPassword1")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' has been imported #######"));
        $(http().client(apiManager).send().get("/applications?field=name&op=eq&value=${appName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")).extract(fromBody()
            .expression("$.[?(@.name=='${appName}')].id", "appId")));

        $(echo("####### Validate application: '${appName}' (${appId}) has defined permissions #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/permissions"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.*.id", "@assertThat(hasSize(3))@")   // Must be three, as the application is created by an OrgAdmin
            .expression("$.[?(@.userId=='${userId-1}')].permission", "manage")
            .expression("$.[?(@.userId=='${userId-2}')].permission", "view")));

       $(echo("####### Re-Import same application - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.getVariable("apiManagerHost"),
                "-u", testContext.getVariable("oadminUsername1"), "-p", testContext.getVariable("oadminPassword1")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 10 but got: " + returnCode);
        });

       $(echo("####### Reduce number of permissions #######"));
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/appPermissions/AppWith1Permission1Invalid.json", context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile2, "-h", testContext.getVariable("apiManagerHost"),
                "-u", testContext.getVariable("oadminUsername1"), "-p", testContext.getVariable("oadminPassword1")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' (${appId}) has reduced permissions #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/permissions"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.*.id", "@assertThat(hasSize(2))@")   // Must be three, as the application is created by an OrgAdmin
            .expression("$.[?(@.userId=='${userId-2}')].permission", "view")));

        $(echo("####### Replicate with ALL permissions #######"));
        String updatedConfigFile3 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/appPermissions/AppWithALLPermissions.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile3, "-h", testContext.getVariable("apiManagerHost"),
                "-u", testContext.getVariable("oadminUsername1"), "-p", testContext.getVariable("oadminPassword1")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' (${appId}) has permissions for ALL users #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/permissions"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.*.id", "@assertThat(hasSize(4))@")   // Must be four, as the application is created by an OrgAdmin
            .expression("$.[?(@.userId=='${userId-1}')].permission", "view")
            .expression("$.[?(@.userId=='${userId-2}')].permission", "view")
            .expression("$.[?(@.userId=='${userId-3}')].permission", "view")));

        $(echo("####### Replicate with ALL permissions and ONE Manage override for user: ${username2} #######"));
        String updatedConfigFile4 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/appPermissions/AppWithALLPermOneOverride.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile4, "-h", testContext.getVariable("apiManagerHost"),
                "-u", testContext.getVariable("oadminUsername1"), "-p", testContext.getVariable("oadminPassword1")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' (${appId}) has permissions for ALL users and ONE manage #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/permissions"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.*.id", "@assertThat(hasSize(4))@")   // Must be four, as the application is created by an OrgAdmin
            .expression("$.[?(@.userId=='${userId-1}')].permission", "view")
            .expression("$.[?(@.userId=='${userId-2}')].permission", "manage")
            .expression("$.[?(@.userId=='${userId-3}')].permission", "view")));

        $(echo("####### Export the application: '${appName}' - To validate permissions are exported #######"));
        String tmpDirPath = TestUtils.createTestDirectory("apps").getPath();
        String appName = context.replaceDynamicContentInString("${appName}");

        $(testContext -> {
            String[] args = {"org", "get", "-n", appName, "-t", tmpDirPath, "-deleteTarget",  "-h", testContext.getVariable("apiManagerHost"),
                "-u", testContext.getVariable("oadminUsername1"), "-p", testContext.getVariable("oadminPassword1"), "-o", "json"};
            int returnCode = ApplicationExportApp.export(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        Assert.assertEquals(new File(tmpDirPath, appName).listFiles().length, 1, "Expected to have one application exported");
        String exportedConfig = new File(tmpDirPath, appName).listFiles()[0].getPath();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        try {
            ClientApplication exportedApp = mapper.readValue(new File(exportedConfig), ClientApplication.class);
            Assert.assertNotNull(exportedApp.getPermissions(), "Exported client application must have permissions");
            Assert.assertEquals(exportedApp.getPermissions().size(), 4, "Exported client application must have 4 permissions");
        }finally {
            apiManagerAdapter.deleteInstance();
        }
    }
}
