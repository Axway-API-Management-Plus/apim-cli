package com.axway.apim.test.envProperties;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;

@ContextConfiguration(classes = {EndpointConfig.class})
public class UsingEnvironmentTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


    @CitrusTest(name = "UsingEnvironmentTestIT")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import an API using the API-Environment with a certain stage only.");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/envrionment-stage-api-${apiNumber}");
        variable("apiName", "envrionment-stage-API-${apiNumber}");


        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable("state", "unpublished");
        variable("stage", "localhost");
        variable("useEnvironmentOnly", "true");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore2.json");
        variable("state", "unpublished");
        variable("stage", "localhost");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));
    }

}
