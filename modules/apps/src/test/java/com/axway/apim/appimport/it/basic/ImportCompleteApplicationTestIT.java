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
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportCompleteApplicationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;


    @CitrusTest
    @Test
    public void importApplicationBasicTest(@Optional @CitrusResource TestContext context) {
        description("Import application into API-Manager");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("appNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("appName", "Complete-App-${appNumber}");
        variable("phone", "123456789-${appNumber}");
        variable("description", "My App-Description ${appNumber}");
        variable("email", "email-${appNumber}@customer.com");
        variable("quotaMessages", "9999");
        variable("quotaPeriod", "week");
        variable("state", "approved");
        variable("appImage", "app-image.jpg");
        variable("oauthCorsOrigins", "");

        variable("scopeName1", "scope.READ");
        variable("scopeEnabled1", true);
        variable("scopeIsDefault1", false);

        variable("scopeName2", "scope.WRITE");
        variable("scopeEnabled2", false);
        variable("scopeIsDefault2", false);

        $(echo("####### Import application: '${appName}' #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/CompleteApplication.json",
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

        $(echo("####### Validate application: '${appName}' with id: ${appId} OAuth-Credentials have been imported #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/oauth"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$[0].id", "ClientConfidentialApp-${appNumber}")
            .expression("$[0].cert", "@assertThat(containsString(-----BEGIN CERTIFICATE-----))@")
            .expression("$[0].secret", "9cb76d80-1bc2-48d3-8d31-edeec0fddf6c")
            .expression("$[0].corsOrigins[0]", "")));

        $(echo("####### Validate application: '${appName}' with id: ${appId} API-Key has been imported #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/apikeys"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$[0].id", "6cd55c27-675a-444a-9bc7-ae9a7869184d-${appNumber}")
            .expression("$[0].secret", "34f2b2d6-0334-4dcc-8442-e0e7009b8950")
            .expression("$[0].corsOrigins[0]", "")));

        $(echo("####### Validate application: '${appName}' with id: ${appId} Ext client id has been imported #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/extclients"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.clientId=='ClientConfidentialClientID-${appNumber}')].clientId", "ClientConfidentialClientID-${appNumber}")
            .expression("$.[?(@.clientId=='ClientConfidentialClientID-${appNumber}')].enabled", "true")
            .expression("$[0].corsOrigins[0]", "")));

        $(echo("####### Validate application: '${appName}' with id: ${appId} Application-Scopes have been imported #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/oauthresource"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.scope=='${scopeName1}')].isDefault", "${scopeIsDefault1}")
            .expression("$.[?(@.scope=='${scopeName2}')].isDefault", "${scopeIsDefault2}")));

        $(sleep().seconds(5));
        $(echo("####### Re-Import same application - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 10 but got: " + returnCode);
        });

        $(echo("####### Re-Import slightly modified application - Existing App should be updated #######"));
        variable("email", "newemail-${appNumber}@customer.com");
        variable("quotaMessages", "1111");
        variable("quotaPeriod", "day");

        variable("scopeName2", "scope.WRITE");
        variable("scopeEnabled2", true);
        variable("scopeIsDefault2", true);

        // First scope is removed - Second scope becomes the first (See the config file: CompleteApplicationOnlyOneScope.json)
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/CompleteApplicationOnlyOneScope.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile2, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' (${appId}) has been updated #######"));
        $(http().client(apiManager).send().get("/applications/${appId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
            .expression("$.[?(@.name=='${appName}')].phone", "${phone}")
            .expression("$.[?(@.name=='${appName}')].description", "${description}")
            .expression("$.[?(@.name=='${appName}')].email", "${email}") // This should be the new email
            .expression("$.[?(@.name=='${appName}')].state", "${state}")
            .expression("$.[?(@.name=='${appName}')].image", "@assertThat(containsString(/api/portal/v))@")).extract(fromBody()
            .expression("$.[?(@.name=='${appName}')].id", "appId")));

        $(echo("####### Validate modified quota for application: '${appName}' with id: ${appId} has been updated #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/quota"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.type", "APPLICATION")
            .expression("$.restrictions[*].api", "@assertThat(hasSize(1))@")
            .expression("$.restrictions[0].api", "*")
            .expression("$.restrictions[0].method", "*")
            .expression("$.restrictions[0].type", "throttle")
            .expression("$.restrictions[0].config.messages", "${quotaMessages}")
            .expression("$.restrictions[0].config.period", "${quotaPeriod}")
            .expression("$.restrictions[0].config.per", "1")));

        $(echo("####### Validate modified scopes for application: '${appName}' with id: ${appId} #######"));
        $(sleep().seconds(3));
        $(http().client(apiManager).send().get("/applications/${appId}/oauthresource"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.scope=='${scopeName2}')].enabled", "${scopeEnabled2}")
            .expression("$.[?(@.scope=='${scopeName2}')].isDefault", "${scopeIsDefault2}")));

        $(echo("####### Update the application image only #######"));
        variable("appImage", "other-app-image.jpg");
        String updatedConfigFile3 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/CompleteApplicationOnlyOneScope.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile3, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Re-Import change corsorigins (oauth) - Existing App should be updated #######"));
        variable("oauthCorsOrigins", "*");
        String updatedConfigFile4 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/CompleteApplicationOnlyOneScope.json", context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile4, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(sleep().seconds(3));
        $(echo("####### Validate application: '${appName}' with id: ${appId} OAuth has been changed #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/oauth"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$[0].id", "ClientConfidentialApp-${appNumber}")
            .expression("$[0].cert", "@assertThat(containsString(-----BEGIN CERTIFICATE-----))@")
            .expression("$[0].secret", "9cb76d80-1bc2-48d3-8d31-edeec0fddf6c")
            .expression("$[0].corsOrigins[0]", "*")));

        $(echo("####### Validate application: '${appName}' with id: ${appId} API-Key has been changed #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/apikeys"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$[0].id", "6cd55c27-675a-444a-9bc7-ae9a7869184d-${appNumber}")
            .expression("$[0].secret", "34f2b2d6-0334-4dcc-8442-e0e7009b8950")
            .expression("$[0].corsOrigins[0]", "*")));

        $(echo("####### Validate application: '${appName}' with id: ${appId} Ext client id has been changed #######"));
        $(http().client(apiManager).send().get("/applications/${appId}/extclients"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.clientId=='ClientConfidentialClientID-${appNumber}')].clientId", "ClientConfidentialClientID-${appNumber}")
            .expression("$.[?(@.clientId=='ClientConfidentialClientID-${appNumber}')].enabled", "true")
            .expression("$[0].corsOrigins[0]", "*")));
    }
}
