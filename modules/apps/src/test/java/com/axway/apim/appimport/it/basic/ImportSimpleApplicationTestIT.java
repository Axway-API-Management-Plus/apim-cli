package com.axway.apim.appimport.it.basic;

import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
import com.axway.apim.appimport.ClientApplicationImportApp;
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
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportSimpleApplicationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    @CitrusTest
    @Test
    public void run(@Optional @CitrusResource TestContext context) {
        description("Import application into API-Manager");
        variable("appName", "My-App-" + RandomNumberFunction.getRandomNumber(4, true));
        variable("useApiAdmin", "true");
        $(echo("####### Import application: '${appName}' #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/SimpleTestApplication.json", context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' has been imported #######"));
        $(http().client(apiManager).send().get("/applications?field=name&op=eq&value=${appName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")));

        $(echo("####### Re-Import same application - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 10 but got: " + returnCode);
        });
    }

    @CitrusTest
    @Test
    @Parameters("context")
    public void importAsAdmin(@Optional @CitrusResource TestContext context) {
        description("Import application into API-Manager using an admin account");
        variable("appName", "My-Admin-App-" + RandomNumberFunction.getRandomNumber(4, true));
        // Directly use an admin-account, otherwise the OrgAdmin organization is used by default
        variable("useApiAdmin", "true");
        $(echo("####### Import application: '${appName}' #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/SimpleTestApplication.json", context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' has been imported #######"));
        $(http().client(apiManager).send().get("/applications?field=name&op=eq&value=${appName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")));
        $(echo("####### Re-Import same application - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 10 but got: " + returnCode);
        });
    }

    @CitrusTest
    @Test
    @Parameters("context")
    public void importDisabledApplication(@Optional @CitrusResource TestContext context) {
        description("Import application into API-Manager which is disabled");
        variable("appNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("appName", "Disabled-App-" + RandomNumberFunction.getRandomNumber(4, true));

        $(echo("####### Import application: '${appName}' #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/DisabledApplication.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate disabled application: '${appName}' has been imported #######"));
        $(http().client(apiManager).send().get("/applications?field=name&op=eq&value=${appName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
            .expression("$.[?(@.name=='${appName}')].enabled", "false")));

        $(echo("####### Re-Import same application - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 10 but got: " + returnCode);
        });
    }
}
