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
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportCompleteApplicationWithOauthResourcesTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    @CitrusTest
    @Test
    public void importApplicationBasicTest(@Optional @CitrusResource TestContext context) {
        description("Import application into API-Manager");
        variable("appNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("appName", "Complete-App-" + RandomNumberFunction.getRandomNumber(4, true));
        variable("phone", "123456789-" + RandomNumberFunction.getRandomNumber(1, true));
        variable("description", "My App-Description " + RandomNumberFunction.getRandomNumber(1, true));
        variable("email", "email-${appNumber}@customer.com");
        variable("quotaMessages", "9999");
        variable("quotaPeriod", "week");
        variable("state", "approved");
        variable("appImage", "app-image.jpg");
        variable("scope1", "READ");
        variable("isDefault1", true);
        variable("scope2", "WRITE");
        variable("isDefault2", false);

        $(echo("####### Import application: '${appName}' #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/CompleteApplicationWithOauthResources.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' has been imported #######"));
        $(http().client(apiManager).send().get("/applications?field=name&op=eq&value=${appName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
            .expression("$.[?(@.name=='${appName}')].phone", "${phone}")
            .expression("$.[?(@.name=='${appName}')].description", "${description}")
            .expression("$.[?(@.name=='${appName}')].email", "${email}")
            .expression("$.[?(@.name=='${appName}')].state", "${state}")
            .expression("$.[?(@.name=='${appName}')].image", "@assertThat(containsString(/api/portal/v))@")).extract(fromBody()
            .expression("$.[?(@.name=='${appName}')].id", "appId")));

        $(echo("####### Validate application: '${appName}' with id: ${appId} oauth resources has been imported #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/oauthresource"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$[1].applicationId", "${appId}")
            .expression("$.[?(@.scope=='${scope1}')].scope", "${scope1}")
            .expression("$.[?(@.scope=='${scope1}')].isDefault", "${isDefault1}")
            .expression("$.[?(@.scope=='${scope2}')].scope", "${scope2}")
            .expression("$.[?(@.scope=='${scope2}')].isDefault", "${isDefault2}")));


        $(echo("####### Validate application: '${appName}' with id: ${appId} OAuth has been imported #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/oauth"));
        $(http().client(apiManager).receive().response(HttpStatus.OK));

        $(echo("####### Re-Import same application - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 10 but got: " + returnCode);
        });

        $(echo("####### Re-Import with change in oauth resource isDefaultFlag - Existing App should be updated #######"));
        variable("isDefault2", true);
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/CompleteApplicationWithOauthResources.json", context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile2, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' with id: ${appId} oauth resources has been imported #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/oauthresource"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$[1].applicationId", "${appId}")
            .expression("$.[?(@.scope=='${scope1}')].scope", "${scope1}")
            .expression("$.[?(@.scope=='${scope1}')].isDefault", "${isDefault1}")
            .expression("$.[?(@.scope=='${scope2}')].scope", "${scope2}")
            .expression("$.[?(@.scope=='${scope2}')].isDefault", "${isDefault2}")));


        $(echo("####### Re-Import change in scope name - Existing App should be updated #######"));
        variable("scope2", "WRITE2");
        String updatedConfigFile3 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/CompleteApplicationWithOauthResources.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile3, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' (${appId}) has been updated #######"));
        $(echo("####### Validate application: '${appName}' with id: ${appId} oauth resources has been imported #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/oauthresource"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$[1].applicationId", "${appId}")
            .expression("$.[?(@.scope=='${scope1}')].scope", "${scope1}")
            .expression("$.[?(@.scope=='${scope1}')].isDefault", "${isDefault1}")
            .expression("$.[?(@.scope=='${scope2}')].scope", "${scope2}")
            .expression("$.[?(@.scope=='${scope2}')].isDefault", "${isDefault2}")));
    }
}
