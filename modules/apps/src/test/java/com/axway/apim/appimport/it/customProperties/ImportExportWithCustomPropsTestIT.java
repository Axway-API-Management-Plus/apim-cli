package com.axway.apim.appimport.it.customProperties;

import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
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

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportExportWithCustomPropsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;


    ObjectMapper mapper = new ObjectMapper();

    @CitrusTest
    @Test
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        description("Import application with Custom-Properties into API-Manager");
        variable("appName", "My-Custom-Prop-App-" +  RandomNumberFunction.getRandomNumber(4, true));
        variable("customProp2", "2");
        $(echo("####### Import application: '${appName}' #######"));

        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/customProperties/AppWithCustomProperties.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' has been imported with Custom-Properties #######"));
        $(http().client(apiManager).send().get("/applications?field=name&op=eq&value=${appName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
            .expression("$.[?(@.name=='${appName}')].appCustomProperty1", "Custom value 1")
            .expression("$.[?(@.name=='${appName}')].appCustomProperty2", "${customProp2}")
            .expression("$.[?(@.name=='${appName}')].appCustomProperty3", "true")).extract(fromBody()
            .expression("$.[?(@.id=='${appName}')].id", "appId")));

        $(echo("####### Re-Import same application - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 10 but got: " + returnCode);
        });

        $(echo("####### Export the application: '${appName}' - To validate custom properties are exported #######"));
        String tmpDirPath = TestUtils.createTestDirectory("apps").getPath();
        String appName = context.replaceDynamicContentInString("${appName}");

        $(testContext -> {
            String[] args = {"org", "get", "-n", appName, "-t", tmpDirPath, "-deleteTarget", "-h", testContext.getVariable("apiManagerHost"), "-u",
                testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass"), "-o", "json"};
            int returnCode = ApplicationExportApp.export(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });


        Assert.assertEquals(new File(tmpDirPath, appName).listFiles().length, 1, "Expected to have one application exported");
        String exportedConfig = new File(tmpDirPath, appName).listFiles()[0].getPath();
        ClientApplication exportedApp = mapper.readValue(new File(exportedConfig), ClientApplication.class);

        Assert.assertNotNull(exportedApp.getCustomProperties(), "Exported client application must have custom properties");
        Assert.assertEquals(exportedApp.getCustomProperties().size(), 3, "Exported client application must have 3 custom properties");
        Assert.assertEquals(exportedApp.getCustomProperties().get("appCustomProperty1"), "Custom value 1");
        Assert.assertEquals(exportedApp.getCustomProperties().get("appCustomProperty2"), "2");
        Assert.assertEquals(exportedApp.getCustomProperties().get("appCustomProperty3"), "true");

        $(echo("####### And Re-Import the exported application - Which should be a again a No-Change #######"));
        $(testContext -> {
            String[] args = {"app", "import", "-c", exportedConfig, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 10 but got: " + returnCode);
        });

        $(echo("####### Re-Import the exported application with changed custom prop #######"));
        variable("customProp2", "3");
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/customProperties/AppWithCustomProperties.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile2, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        $(echo("####### Validate application: '${appName}' - custom property has been changed #######"));
        $(http().client(apiManager).send().get("/applications?field=name&op=eq&value=${appName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
            .expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
            .expression("$.[?(@.name=='${appName}')].appCustomProperty1", "Custom value 1")
            .expression("$.[?(@.name=='${appName}')].appCustomProperty2", "${customProp2}")
            .expression("$.[?(@.name=='${appName}')].appCustomProperty3", "true")).extract(fromBody()
            .expression("$.[?(@.id=='${appName}')].id", "appId")));

    }
}
