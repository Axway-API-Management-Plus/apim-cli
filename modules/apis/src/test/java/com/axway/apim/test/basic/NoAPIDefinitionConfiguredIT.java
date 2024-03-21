package com.axway.apim.test.basic;

import com.axway.apim.EndpointConfig;
import com.axway.apim.lib.error.ErrorCode;
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
public class NoAPIDefinitionConfiguredIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("If no api-definition is passed as argument and no apiDefinition attribute is found in configuration file, the tool must fail with a dedicated return code.");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
        variable("apiPath", "/my-no-api-def-${apiNumber}");
        variable("apiName", "No-API-DEF-CONFIGURED-${apiNumber}");

        $(echo("####### Calling the tool with a Non-Admin-User. #######"));
        variable(ImportTestAction.API_DEFINITION, "");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/minimal-config.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", String.valueOf(ErrorCode.NO_API_DEFINITION_CONFIGURED.getCode()));
        $(action(swaggerImport));
    }
}
