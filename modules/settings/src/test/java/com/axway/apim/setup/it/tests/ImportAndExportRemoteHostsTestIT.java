package com.axway.apim.setup.it.tests;

import com.axway.apim.test.actions.TestParams;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.setup.it.ExportManagerConfigTestAction;
import com.axway.apim.setup.it.ImportManagerConfigTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;

@Test
public class ImportAndExportRemoteHostsTestIT extends TestNGCitrusTestRunner {

    private static final String PACKAGE = "/com/axway/apim/setup/it/tests/";

    @CitrusTest
    @Test
    @Parameters("context")
    public void runRemoteHostsImportExport(@Optional @CitrusResource TestContext context) {
        description("Export/Import RemoteHosts from and into the API-Manager");
        ImportManagerConfigTestAction importApp = new ImportManagerConfigTestAction(context);
        ExportManagerConfigTestAction exportApp = new ExportManagerConfigTestAction(context);
        echo("####### Add Remote-Host 1 #######");
        createVariable("useApiAdmin", "true"); // Use admin account
        createVariable(TestParams.PARAM_CONFIGFILE, PACKAGE + "remote-host-1.json");
        createVariable(TestParams.PARAM_EXPECTED_RC, "0");
        createVariable("${remoteHostName}", "sample.remote.host" + importApp.getRandomNum());
        createVariable("${remoteHostPort}", "8888");
        importApp.doExecute(context);
        echo("####### Validate remote host has 1 been added correctly #######");
        http(builder -> builder.client("apiManager").send().get("/remotehosts").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.name=='${remoteHostName}')].name", "${remoteHostName}")
            .validate("$.[?(@.name=='${remoteHostName}')].port", "${remoteHostPort}"));
        echo("####### Add Remote-Host 2 #######");
        createVariable(TestParams.PARAM_CONFIGFILE, PACKAGE + "remote-host-1.json");
        createVariable(TestParams.PARAM_EXPECTED_RC, "0");
        createVariable("${remoteHostName}", "another.remote.host" + importApp.getRandomNum());
        createVariable("${remoteHostPort}", "9999");
        importApp.doExecute(context);
        echo("####### Validate remote host has 2 been added correctly #######");
        http(builder -> builder.client("apiManager").send().get("/remotehosts").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.name=='${remoteHostName}')].name", "${remoteHostName}")
            .validate("$.[?(@.name=='${remoteHostName}')].port", "${remoteHostPort}")
            .extractFromPayload("$.[?(@.name=='${remoteHostName}')].createdOn", "remoteHostCreatedOn")
            .extractFromPayload("$.[?(@.name=='${remoteHostName}')].createdBy", "remoteHostCreatedBy")
            .extractFromPayload("$.[?(@.name=='${remoteHostName}')].id", "remoteHostId"));
        echo("####### Update remote host 2 #######");
        createVariable(TestParams.PARAM_CONFIGFILE, PACKAGE + "remote-host-1.json");
        createVariable(TestParams.PARAM_EXPECTED_RC, "0");
        createVariable("${remoteHostPort}", "9999");
        importApp.doExecute(context);
        echo("####### Validate a new Remote-Host has been updated #######");
        http(builder -> builder.client("apiManager").send().get("/remotehosts").header("Content-Type", "application/json"));
        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
            .validate("$.[?(@.name=='${remoteHostName}' && @.port==${remoteHostPort})].name", "${remoteHostName}")
            .validate("$.[?(@.name=='${remoteHostName}' && @.port==${remoteHostPort})].port", "${remoteHostPort}")
            .validate("$.[?(@.name=='${remoteHostName}' && @.port==${remoteHostPort})].createdBy", "${remoteHostCreatedBy}") // createdBy should not be changed/updated
            .validate("$.[?(@.name=='${remoteHostName}' && @.port==${remoteHostPort})].createdOn", "${remoteHostCreatedOn}") // createdOn should not be changed/updated
        );
        echo("####### Export remote host 2 #######");
        createVariable(TestParams.PARAM_EXPECTED_RC, "0");
        createVariable(TestParams.PARAM_TARGET, exportApp.getTestDirectory().getPath());
        createVariable(TestParams.PARAM_OUTPUT_FORMAT, "json");
        createVariable(TestParams.PARAM_NAME, "another*" + importApp.getRandomNum());
        exportApp.doExecute(context);
        Assert.assertEquals(exportApp.getLastResult().getExportedFiles().size(), 1, "One remote host is expected");
    }
}
