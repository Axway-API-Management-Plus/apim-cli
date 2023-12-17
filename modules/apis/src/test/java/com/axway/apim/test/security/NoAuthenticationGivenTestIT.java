package com.axway.apim.test.security;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

@ContextConfiguration(classes = {EndpointConfig.class})
public class NoAuthenticationGivenTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "NoAuthenticationGivenTest")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Verify no error appears, if Authentication is not configured! Must be Passthrough");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/no-authn-test-${apiNumber}");
		variable("apiName", "No AuthN Test ${apiNumber}");
		variable("status", "unpublished");

		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/2_no_authn.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' with Passthrough-Security #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].type", "passThrough")));
	}

}
