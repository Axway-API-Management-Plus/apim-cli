package com.axway.apim.test.basic;

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

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;


@ContextConfiguration(classes = {EndpointConfig.class})
public class SwaggerFromURLRefFileTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;
	@CitrusTest
	@Test
	public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Validates a Swagger-File can be taken from a URL using a REF-File.");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/ref-file-swagger-${apiNumber}");
		variable("apiName", "Ref-File-Swagger from URL-${apiNumber}");
		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time from URL #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/swagger-file-with-username.url");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
		$(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")));

		$(echo("####### Re-Import API from URL without a change #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/swagger-file-with-username.url");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));
	}

}
