package com.axway.apim.test.staging;

import com.axway.apim.EndpointConfig;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class SimpleStagingTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "SimpleStagingTest")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Import the API with production stage settings");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-stage-test-${apiNumber}");
		variable("apiName", "Stage-Test-${apiNumber}");

		$(echo("####### Must fail, as the base organization wrong #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/staging/1_no-change-config.json");
        variable("expectedReturnCode", "57"); // 57 is expected - Base org is wrong
        $(action(swaggerImport));

        $(echo("####### Must fail, as the organization is invalid in base- and stage-config #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/staging/1_no-change-config.json");
        variable("stage", "wrongOrg"); // << Will map to a config having an invalid organization
        variable("expectedReturnCode", "57"); // 57 is expected - Invalid organization in staged config
        $(action(swaggerImport));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' on stage prod #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/staging/1_no-change-config.json");
        variable("stage", "prod"); // << Program will search for file: 1_no-change-config.prod.json
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "published")).extract(fromBody() // State must be published in "prod"
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));
	}

}
