package com.axway.apim.test.tags;

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
public class APINoTagsChangedTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "APINoTagsChangedTest")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Make sure, Desired and Actual tags are compared correctly. Must lead to a No-Change");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/api-tags-test-${apiNumber}");
		variable("apiName", "API Tags Test ${apiNumber}");
		variable("status", "unpublished");
		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("status", "unpublished");
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/tags/1_tags-config.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######"));
        $(http().client(apiManager).send().get("/proxies").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.expression("$.[?(@.path=='${apiPath}')].tags.['tag-name 2'][0]", "value 3")
			.expression("$.[?(@.path=='${apiPath}')].tags.['tag-name 2'][1]", "value 4")
			.expression("$.[?(@.path=='${apiPath}')].tags.['tag-name 1'][0]", "value 1")
			.expression("$.[?(@.path=='${apiPath}')].tags.['tag-name 1'][1]", "value 2")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Importing exactly the same API with the same tags --> No-Change: #######"));
        variable("status", "unpublished");
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/tags/1_tags-config.json");
        variable("expectedReturnCode", "10"); // This is the real test here!
        $(action(swaggerImport));
	}
}
