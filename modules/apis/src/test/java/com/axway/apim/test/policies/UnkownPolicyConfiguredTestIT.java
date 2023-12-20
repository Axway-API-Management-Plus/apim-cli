package com.axway.apim.test.policies;

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
public class UnkownPolicyConfiguredTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "UnkownPolicyConfiguredTest")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();

        description("A dedicated return-code is expected, when an unknown policy is configured.");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/unknown-policy-test-${apiNumber}");
		variable("apiName", "Unknown Policy Test ${apiNumber}");
		variable("status", "unpublished");


		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("requestPolicy", "Brand new policy!");
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/policies/1_request-policy.json");
        variable("expectedReturnCode", "85");
		$(action(swaggerImport));
	}
}
