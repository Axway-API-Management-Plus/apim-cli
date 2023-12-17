package com.axway.apim.test.version;

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

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class UnpublishedAPIVersionTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "UnpublishedAPIVersionTest")
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Validate that API-Version is updated");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/version-test-${apiNumber}");
		variable("apiName", "Version-test-${apiNumber}");
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/version/1_flexible_version_and_state.json");
        variable("version", "1.0.0");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
		$(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.expression("$.[?(@.path=='${apiPath}')].version", "${version}")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Perform a no-change #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/version/1_flexible_version_and_state.json");
        variable("version", "1.0.0");
        variable("state", "unpublished");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Change the API-Version for the Unpublished API #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/version/1_flexible_version_and_state.json");
        variable("version", "1.0.1");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate that the same API (as it's still unpublished) has been updated #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "unpublished")
			.expression("$.[?(@.id=='${apiId}')].version", "${version}")));
	}
}
