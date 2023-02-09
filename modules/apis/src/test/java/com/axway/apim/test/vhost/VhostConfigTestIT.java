package com.axway.apim.test.vhost;

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
public class VhostConfigTestIT extends TestNGCitrusTestRunner {

    @CitrusTest
    @Test
    @Parameters("context")
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Validate VHosts are handled correctly");
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/vhost-test-${apiNumber}");
        variable("apiName", "VHost Test ${apiNumber}");

        echo("####### Importing unpublised API: '${apiName}' on path: '${apiPath}' with following settings: #######");
        createVariable("status", "unpublished");
        createVariable("vhost", "api123.customer.com");
        createVariable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        createVariable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/vhost/1_vhost-config.json");
        createVariable("expectedReturnCode", "0");
        swaggerImport.doExecute(context);

        echo("####### Validate unpublished API: '${apiName}' on path: '${apiPath}' is configured with V-Host #######");
        http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
                .validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
                .validate("$.[?(@.path=='${apiPath}')].vhost", "api123.customer.com")
                .extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));

        echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
        createVariable("status", "published");
        createVariable("vhost", "api123.customer.com");
        createVariable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        createVariable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/vhost/1_vhost-config.json");
        createVariable("expectedReturnCode", "0");
        swaggerImport.doExecute(context);

        echo("####### Validate published API: '${apiName}' on path: '${apiPath}' has V-Host configured #######");
        http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
                .validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
                .validate("$.[?(@.id=='${apiId}')].state", "published")
                .validate("$.[?(@.id=='${apiId}')].vhost", "api123.customer.com"));

        echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
        createVariable("status", "unpublished");
        createVariable("vhost", "api123.customer.com");
        createVariable("enforce", "true"); // as we are going back from published to unpublished
        createVariable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        createVariable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/vhost/1_vhost-config.json");
        swaggerImport.doExecute(context);

        http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

        http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
                .validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
                .validate("$.[?(@.id=='${apiId}')].state", "${status}"));
    }
}
