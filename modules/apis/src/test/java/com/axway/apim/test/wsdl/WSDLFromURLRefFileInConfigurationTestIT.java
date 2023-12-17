package com.axway.apim.test.wsdl;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;

@ContextConfiguration(classes = {EndpointConfig.class})
public class WSDLFromURLRefFileInConfigurationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
	@Test
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Validates a WSDL-File can be taken from a URL using a REF-File described in API json configuration");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/ref-file-wsdl-in-configuration-${apiNumber}");
		variable("apiName", "Ref-File-WSDL in configuration from URL-${apiNumber}");



		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time from URL #######"));
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
        variable(ImportTestAction.API_DEFINITION,"./src/test/resources/com/axway/apim/test/files/wsdl/wsdl-file-with-username.url");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Re-Import API from URL without a change #######"));
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
        variable(ImportTestAction.API_DEFINITION,"./src/test/resources/com/axway/apim/test/files/wsdl/wsdl-file-with-username.url");
        variable("state", "unpublished");
        variable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
	}
}
