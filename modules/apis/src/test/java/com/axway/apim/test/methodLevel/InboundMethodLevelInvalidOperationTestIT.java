package com.axway.apim.test.methodLevel;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;


@ContextConfiguration(classes = {EndpointConfig.class})
public class InboundMethodLevelInvalidOperationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest
	@Test
	public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Make sure, the error that an invalid operationId is given is properly handled.");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/basic-method-level-api-${apiNumber}");
		variable("apiName", "Basic Method-Level-API-${apiNumber}");
		$(echo("####### Try to replicate an API having Method-Level settings declared #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-inbound-invalidOperation.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "72");
        variable("securityProfileName", "APIKeyBased${apiNumber}");
		$(action(swaggerImport));
	}
}
