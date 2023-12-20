package com.axway.apim.appimport.it.basic;

import com.axway.apim.APIImportApp;
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
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportApplicationWithAPIAccessTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    @CitrusTest
    @Test
    public void importApplicationBasicTest(@Optional @CitrusResource TestContext context) {
        description("Import application that has a subscription to an API");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("useApiAdmin", "true"); // Use apiadmin account

        variable("apiPath", "/test-app-api1-${apiNumber}");
        variable("apiName", "Test-App-API1-${apiNumber}");
        variable("apiName1", "${apiName}");

        $(echo("####### Importing Test API 1 : '${apiName}' on path: '${apiPath}' #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/test-api-config.json",
            context, "apps", true);
        String specFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/petstore.json",
            context, "apps", false);
        $(testContext -> {
            String[] args = {"api", "import", "-c", updatedConfigFile, "-a", specFile, "-h", testContext.getVariable("apiManagerHost"),
                "-u", testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
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

        $(echo("####### Importing Test API 2 : '${apiName}' on path: '${apiPath}' #######"));
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/test-api-config.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"api", "import", "-c", updatedConfigFile2, "-a", specFile, "-h", testContext.getVariable("apiManagerHost"), "-u", testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = APIImportApp.importAPI(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Extract ID of imported API 2: '${apiName}' on path: '${apiPath}' #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId2")));

        $(echo("###### API 1: '${apiName1}' (${apiId1})"));
        $(echo("###### API 2: '${apiName2}' (${apiId2})"));


        variable("appNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("appName", "Complete-App-${appNumber}");
        $(echo("####### Import application: '${appName}' with access to ONE API #######"));
        String updatedConfigFile3 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/AppWithAPIAccess.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile3, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' has been imported #######"));
        $(http().client(apiManager).send().get("/applications?field=name&op=eq&value=${appName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")).extract(fromBody()
            .expression("$.[?(@.name=='${appName}')].id", "appId")));

        $(echo("####### Validate application: '${appName}' has access to the imported API 1 #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.apiId=='${apiId1}')].state", "approved")));

        $(echo("####### Import application: '${appName}' with access to TWO APIs #######"));
        String updatedConfigFile4 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/AppWithAPITwoAccesses.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile4, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });


        $(echo("####### Validate application: '${appName}' has been imported now having access to two APIs #######"));
        $(http().client(apiManager).send().get("/applications/${appId}"));

        // Make sure, the application id is unchanged
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.id=='${appId}')].name", "${appName}")));

        $(echo("####### Validate application: '${appName}' has access to the imported API 1 and 2 #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.apiId=='${apiId1}')].state", "approved")
            .expression("$.[?(@.apiId=='${apiId2}')].state", "approved")));

        $(echo("####### Reduce access of application: '${appName}' to only ONE API #######"));

        String updatedConfigFile5 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/AppWithAPIAccess.json", context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile5, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' has been imported now having access to two APIs #######"));
        $(http().client(apiManager).send().get("/applications/${appId}"));
        // Make sure, the application id is unchanged
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.id=='${appId}')].name", "${appName}")));

        $(echo("####### Validate application: '${appName}' has access to ONLY ONE API #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.apiId=='${apiId1}')].state", "approved")
            .expression("$.*.id", "@assertThat(hasSize(1))@"))); // We expect only ONE subscription!
    }
}
