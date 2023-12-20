package com.axway.apim.test.basic;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;


@ContextConfiguration(classes = {EndpointConfig.class})
public class UnpublishDeleteMustBeBreakingTestIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        $(echo("####### This test makes sure, once an API is published, unpublishing or deleting it requires a force #######"));
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("apiPath", "/check-is-breaking-${apiNumber}");
        variable("apiName", "Check-is-Breaking-${apiNumber}");


        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' as Published #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("state", "published");
        variable("version", "1.0.0");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate unpublishing it, will fail, with the need to enforce it #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("state", "unpublished");
        variable("enforce", "false");
        variable("expectedReturnCode", "15");
        $(action(swaggerImport));

        $(echo("####### Validate deleting it, will fail, with the need to enforce it #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("state", "deleted");
        variable("expectedReturnCode", "15");
        $(action(swaggerImport));
    }
}
