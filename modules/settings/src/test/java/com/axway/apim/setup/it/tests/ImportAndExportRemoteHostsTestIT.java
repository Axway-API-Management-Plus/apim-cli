package com.axway.apim.setup.it.tests;

import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
import com.axway.apim.setup.APIManagerSettingsApp;
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
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportAndExportRemoteHostsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;

    @CitrusTest
    @Test
    public void runRemoteHostsImport(@Optional @CitrusResource TestContext context) {
        description("Export/Import RemoteHosts from and into the API-Manager");
        $(echo("####### Add Remote-Host 1 #######"));
        variable("remoteHostName", "citrus:concat('sample.remote.host-',  citrus:randomNumber(4))");
        variable("remoteHostPort", "8888");
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/setup/it/tests/remote-host-1.json", context, "settings");
        $(testContext -> {
            String[] args = {"settings", "import", "-c", updatedConfigFile, "-h",
                testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = APIManagerSettingsApp.importConfig(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate remote host has ${remoteHostName} been added correctly #######"));
        $(http().client(apiManager).send().get("/remotehosts").message());
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.name=='${remoteHostName}')].name", "${remoteHostName}")
            .expression("$.[?(@.name=='${remoteHostName}')].port", "${remoteHostPort}")));

        $(echo("####### Add Remote-Host  ${remoteHostName}  #######"));
        variable("remoteHostName", "citrus:concat('sample.remote.host-',  citrus:randomNumber(4))");
        variable("remoteHostPort", "9999");
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/setup/it/tests/remote-host-1.json", context, "settings");

        $(testContext -> {
            String[] args = {"settings", "import", "-c", updatedConfigFile2, "-h",
                testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = APIManagerSettingsApp.importConfig(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate remote host has 2 been added correctly #######"));
        $(http().client(apiManager).send().get("/remotehosts").message());
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.name=='${remoteHostName}')].name", "${remoteHostName}")
            .expression("$.[?(@.name=='${remoteHostName}')].port", "${remoteHostPort}")).extract(fromBody()
            .expression("$.[?(@.name=='${remoteHostName}')].createdOn", "remoteHostCreatedOn")
            .expression("$.[?(@.name=='${remoteHostName}')].createdBy", "remoteHostCreatedBy")
            .expression("$.[?(@.name=='${remoteHostName}')].id", "remoteHostId")));


        $(echo("####### Update remote host 2 #######"));
        variable("$remoteHostPort", "9999");
        String updatedConfigFile3 = TestUtils.createTestConfig("/com/axway/apim/setup/it/tests/remote-host-1.json", context, "settings");

        $(testContext -> {
            String[] args = {"settings", "import", "-c", updatedConfigFile3, "-h",
                testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = APIManagerSettingsApp.importConfig(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate a new Remote-Host has been updated #######"));
        $(http().client(apiManager).send().get("/remotehosts").message());
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.name=='${remoteHostName}')].name", "${remoteHostName}")
            .expression("$.[?(@.name=='${remoteHostName}')].port", "${remoteHostPort}")
            .expression("$.[?(@.name=='${remoteHostName}' && @.port==${remoteHostPort})].createdBy", "${remoteHostCreatedBy}") // createdBy should not be changed/updated
            .expression("$.[?(@.name=='${remoteHostName}' && @.port==${remoteHostPort})].createdOn", "${remoteHostCreatedOn}"))); // createdOn should not be changed/updated
    }

    @Test
    @CitrusTest
    public void runRemoteHostsExport() {
        $(echo("####### Export remote host 2 #######"));
        String tmpDirPath = TestUtils.createTestDirectory("settings").getPath();
        $(testContext -> {
            String[] args = {"settings", "get", "-type", "remotehosts", "-o", "json", "-t", tmpDirPath, "-deleteTarget", "-h",
                testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = APIManagerSettingsApp.exportConfig(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        Assert.assertEquals(new File(tmpDirPath, "axway-api-manager").list().length, 1, "One remote host is expected");
    }
}
