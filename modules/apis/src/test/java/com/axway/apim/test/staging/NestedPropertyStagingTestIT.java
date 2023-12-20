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
public class NestedPropertyStagingTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "NestedPropertyStagingTest")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Make sure nested properties can be staged as well!");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-stage-test-${apiNumber}");
		variable("apiName", "Stage-Test-${apiNumber}");
		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/staging/2_nested_prop-config.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.takeFrom", "QUERY")
			.expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.apiKeyFieldName", "KeyId")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with production settings #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/staging/2_nested_prop-config.json");
        variable("stage", "prod"); // << Program will search for file: 2_nested_prop-config.prod.json
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}").name("api"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "unpublished")
			.expression("$.[?(@.id=='${apiId}')].securityProfiles[0].devices[0].properties.takeFrom", "HEADER")
			.expression("$.[?(@.id=='${apiId}')].securityProfiles[0].devices[0].properties.apiKeyFieldName", "KeyId")));
	}
}
