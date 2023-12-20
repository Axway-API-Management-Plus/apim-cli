package com.axway.apim.test.methodLevel;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;


@ContextConfiguration(classes = {EndpointConfig.class})
public class MethodLevelInvalidProfileTestIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    @Test
    public void runInboundProfileValidation() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Make sure only valid profile names are referenced");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/invalid-sec-profile-api-${apiNumber}");
        variable("apiName", "Invalid-SecProfile-API-${apiNumber}");

        $(echo("####### Try to replicate an API having invalid profiles referenced #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/methodLevel/method-level-inbound-invalidProfileRefercence.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "73");
        variable("securityProfileName1", "APIKeyBased${apiNumber}");
        variable("securityProfileName2", "SomethingWrong${apiNumber}");
        $(action(swaggerImport));
    }

    @CitrusTest
    @Test
    public void runOutboundProfileValidation() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Make sure only valid profile names are referenced");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/invalid-authn-profile-api-${apiNumber}");
        variable("apiName", "Invalid AuthN-Profile-API-${apiNumber}");

        $(echo("####### Try to replicate an API having invalid profiles referenced #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/methodLevel/method-level-outboundbound-invalidProfileReference.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "73");
        variable("authenticationProfileName1", "HTTP Basic");
        variable("authenticationProfileName2", "SomethingWrong");
        $(action(swaggerImport));
    }

    @CitrusTest
    @Test
    public void runInboundCorsProfileValidation() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Is the CORS-Profile not know - Error must be handled");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/basic-method-level-api-${apiNumber}");
        variable("apiName", "Basic Method-Level-API-${apiNumber}");

        $(echo("####### Try to replicate an API having invalid profiles referenced #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/methodLevel/method-level-inbound-invalidCorsProfileRefercence.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "73");
        $(action(swaggerImport));
    }
}
