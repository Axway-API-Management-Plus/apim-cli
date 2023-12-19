package com.axway.apim.test.applications;

import com.axway.apim.EndpointConfig;
import com.axway.apim.lib.CoreParameters.Mode;
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
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class IgnoreClientAppsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "IgnoreClientAppsTestIT")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("This test makes sure, no client-applications have got a subscription.");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/ignore-client-apps-test-${apiNumber}");
		variable("apiName", "Ignore Client-Apps-API-${apiNumber}");

		// ############## Creating Test-Application #################
        variable("testAppName", "Ignored Test App-Name ${apiNumber}");
		$(http().client(apiManager).send().post("/applications").name("orgCreatedRequest").message()
			.header("Content-Type", "application/json")
			.body("{\"name\":\"${testAppName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));

        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "testAppId")
			.expression("$.name", "testAppName")));

        $(echo("####### Created Test-Application to be ignored: '${testAppName}' with id: '${testAppId}' #######"));
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/applications/1_api-with-1-org-1-app.json");
        variable("state", "published");
        variable("orgName", "${orgName}");
        variable("clientAppsMode", String.valueOf(Mode.ignore));
        variable("expectedReturnCode", "0");
		$(action(swaggerImport));

		$(echo("####### Validate API: '${apiName}' has been imported without an error (defined apps are ignored) #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "published")));

		$(echo("####### Validate the application no Access to this API #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId}/apis"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.*.apiId", "@assertThat(not(containsString(${apiId})))@")));
	}
}
