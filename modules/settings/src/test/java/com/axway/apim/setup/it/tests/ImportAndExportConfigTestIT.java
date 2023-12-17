package com.axway.apim.setup.it.tests;

import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
import com.axway.apim.setup.APIManagerSettingsApp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
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

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;

@ContextConfiguration(classes = {EndpointConfig.class})
@Test
public class ImportAndExportConfigTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    private final ObjectMapper mapper = new ObjectMapper();

    @CitrusTest
    @Test
    public void runConfigImportAndExport() throws Exception {
        description("Export/Import configuration from and into the API-Manager");

        $(echo("####### Export the configuration #######"));
        String tmpDirPath = TestUtils.createTestDirectory("settings").getPath();
        $(testContext -> {
            String[] args = {"settings", "get", "-type", "config", "-o", "json", "-t", tmpDirPath, "-deleteTarget", "-h",
                testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = APIManagerSettingsApp.exportConfig(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        String exportedConfig = new File(tmpDirPath, "axway-api-manager").listFiles()[0].getPath();
        JsonNode config = mapper.readTree(new File(exportedConfig));
        Assert.assertTrue(config.get("config").get("registrationEnabled").asBoolean());
        Assert.assertEquals(config.get("config").get("apiDefaultVirtualHost").asText(), "");
        Assert.assertTrue(config.get("config").get("apiRoutingKeyLocation").isEmpty());

        $(testContext -> {
            String[] args = {"settings", "import", "-c", exportedConfig, "-h",
                testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = APIManagerSettingsApp.importConfig(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

    }

    @CitrusTest
    @Test
    public void runUpdateConfiguration(@Optional @CitrusResource TestContext context) {
        description("Update API-Configuration with custom config file");
        $(echo("####### Import configuration #######"));
        context.setVariable("portalName", "MY API-MANAGER NAME");
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/setup/it/tests/apimanager-config.json", context, "settings");

        $(testContext -> {
            String[] args = {"settings", "import", "-c", updatedConfigFile, "-h",
                testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = APIManagerSettingsApp.importConfig(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate configuration has been applied #######"));
        $(http().client(apiManager).send().get("/config").message());
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.portalName", "${portalName}")));


        $(echo("####### Import configuration using organization administrator role#######"));
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/setup/it/tests/apimanager-config.json", context, "settings");
        $(testContext -> {
            String[] args = {"settings", "import", "-c", updatedConfigFile2, "-h",
                testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${oadminUsername1}"), "-p", testContext.replaceDynamicContentInString("${oadminPassword1}")};
            int returnCode = APIManagerSettingsApp.importConfig(args);
            if (returnCode != 17)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
    }
}
