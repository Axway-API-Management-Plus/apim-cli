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
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class UnpublishedSummaryAPITestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
	@Test @Parameters("context")
	public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Make sure, the summary gets updated");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-summary-test-${apiNumber}");
		variable("apiName", "My-Summary-test-${apiNumber}");
		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/3_4_unpublished-dynamic-summary-api.json");
        variable("expectedReturnCode", "0");
        variable("apiSummary", "My great summary 1!");
		$(action(swaggerImport));

		$(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
		$(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######"));
		$(echo("####### Expected API-Summary: ${apiSummary} #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.expression("$.[?(@.path=='${apiPath}')].summary", "${apiSummary}"))
            .extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Change the API-Summary, stay in the same status #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/3_4_unpublished-dynamic-summary-api.json");
        variable("expectedReturnCode", "0");
        variable("apiSummary", "Another great summary!!!");
        $(action(swaggerImport));

        $(echo("####### Validate the summary has been changed, which is not a breaking change #######"));
        $(echo("####### API-Summary should now be changed to: ${apiSummary} #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "unpublished")
			.expression("$.[?(@.id=='${apiId}')].summary", "${apiSummary}")));

        $(echo("####### Change the API back to a state without a summary #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/3_5_unpublished-no-summary-api.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate the summary has been REMOVED, which is not a breaking change #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "unpublished")
			.expression("$.[?(@.id=='${apiId}')].summary", null)));
	}

}
