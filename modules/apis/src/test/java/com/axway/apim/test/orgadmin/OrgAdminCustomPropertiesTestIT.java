package com.axway.apim.test.orgadmin;

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
public class OrgAdminCustomPropertiesTestIT extends TestNGCitrusSpringSupport {

    @CitrusTest(name = "OrgAdminTriesToPublishTestIT")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("OrgAdmin wants to use a custom policy.");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/org-admin-published-${apiNumber}");
        variable("apiName", "OrgAdmin-Published-${apiNumber}");

        $(echo("####### Calling the tool with a Non-Admin-User. #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/customproperties/1_custom-properties-config_IT.json");
        variable("status", "unpublished");
        variable("customProperty1", "Test-Input 1");
        variable("customProperty2", "1");
        variable("customProperty3", "true");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));
    }
}
