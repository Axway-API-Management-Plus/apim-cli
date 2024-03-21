package com.axway.apim.test.quota;

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
public class InvalidQuotaConfigTestIT extends TestNGCitrusSpringSupport {

	@CitrusTest
	@Test
	public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Try to import an API with invalid quota configuration.");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/invalid-quota-api-${apiNumber}");
		variable("apiName", "Invalid Quota-API-${apiNumber}");
        $(echo("####### Trying to import API: '${apiName}' on path: '${apiPath}' with invalid quota config #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/issue-109-invalid-quota-config-1.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "71");
        $(action(swaggerImport));

        $(echo("####### Trying to import API: '${apiName}' on path: '${apiPath}' with invalid quota config #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/issue-109-invalid-quota-config-2.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "71");
        $(action(swaggerImport));
	}
}
