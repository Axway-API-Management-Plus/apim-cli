package com.axway.apim.test.odata;

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
public class ODataV4ImportTestIT extends TestNGCitrusTestRunner {

    @CitrusTest
    @Test
    @Parameters("context")
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        ImportTestAction importAction = new ImportTestAction();
        description("Import an OData V4 specification that must be converted into an OpenAPI V3 specification.");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/odata-v4-api-${apiNumber}");
        variable("apiName", "OData-V4-API-${apiNumber}");
        variable("backendBasepath", "https://services.odata.org/TripPinRESTierService/(S(gnxtchg1e2of2ictemxrcv4r))");
        variable("state", "unpublished");

        echo("####### Importing OData V2 API: '${apiName}' on path: '${apiPath}' #######");
        createVariable(ImportTestAction.API_DEFINITION, "/api_definition_1/ODataV2NorthWindMetadata.xml");
        createVariable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/minimal-config-with-backendBasepath.json");
        createVariable("expectedReturnCode", "0");
        importAction.doExecute(context);

        echo("####### Validate OData V4 API: '${apiName}' on path: '${apiPath}' has been imported #######");
        http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
                .validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));

        echo("####### RE-Importing same API: '${apiName}' on path: '${apiPath}' without changes. Expecting No-Change. #######");
        createVariable("expectedReturnCode", "10");
        importAction.doExecute(context);
    }
}
