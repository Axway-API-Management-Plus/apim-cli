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
public class WSDLFromURLDirectTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest
	@Test
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
		ImportTestAction swaggerImport = new ImportTestAction();
		description("Validates a WSDL-File can be taken from a URL using the direct instruction.");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/direct-url-wsdl-${apiNumber}");
		variable("apiName", "Direct-URL-WSDL from URL-${apiNumber}");

		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time from URL #######"));
		variable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/wsdl/wsdl-minimal-config.json");
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
        variable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/wsdl/wsdl-minimal-config.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);

        $(echo("####### Setting the status to Published #######"));
        variable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/wsdl/wsdl-minimal-config.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "published")));

        $(echo("####### Now performing a change, which required to Re-Create the API #######"));
        variable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/wsdl/wsdl-minimal-config-with-tags.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);



	}
}
