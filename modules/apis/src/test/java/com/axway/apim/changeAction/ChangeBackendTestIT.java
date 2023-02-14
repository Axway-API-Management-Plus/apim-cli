package com.axway.apim.changeAction;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;

@Test
public class ChangeBackendTestIT extends TestNGCitrusTestRunner {

    private static final String TEST_PACKAGE = "/com/axway/apim/changeAction/";

    @CitrusTest
    @Test
    @Parameters("context")
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        ChangeTestAction changeAction = new ChangeTestAction();
        ImportTestAction importAction = new ImportTestAction();

        description("This test imports an API including quota, subscription and granted access to some org the it changes the backend URL of it and validates it.");

        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/change-backend-${apiNumber}");
        variable("apiName", "Change-Backend-${apiNumber}");
        variable("state", "published");

        echo("####### Importing API: '${apiName}' on path: '${apiPath}' which should be changed #######");
        createVariable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        createVariable(ImportTestAction.API_CONFIG, TEST_PACKAGE + "changeBackendTestAPI-config.json");
        createVariable("expectedReturnCode", "0");
        importAction.doExecute(context);

        echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
        http(builder -> builder.client("apiManager").send().get("/proxies?field=name&op=eq&value=${apiName}").header("Content-Type", "application/json"));

        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
                .validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));

        echo("####### Try to change the backend of this published API, but without giving a force flag #######");
        createVariable("expectedReturnCode", "15");
        createVariable("name", "${apiName}");
        createVariable("enforce", "false");
        createVariable("newBackend", "https://api.axway.com");
        changeAction.doExecute(context);

        echo("####### Change the backend of this published API - Enforcing it! #######");
        createVariable("expectedReturnCode", "0");
        createVariable("name", "${apiName}");
        createVariable("newBackend", "https://api.axway.com");
        createVariable("enforce", "true");
        changeAction.doExecute(context);

        echo("####### Validate re-created API has properly created inlcuding all quota, apps, orgs #######");
        http(builder -> builder.client("apiManager").send().get("/proxies?field=name&op=eq&value=${apiName}").header("Content-Type", "application/json"));

        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
                .validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .validate("$.[?(@.path=='${apiPath}')].serviceProfiles._default.basePath", "https://api.axway.com")
                .extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"));
    }

}
