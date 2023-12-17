package com.axway.apim.test.quota;

import com.axway.apim.EndpointConfig;
import com.axway.apim.adapter.APIManagerAdapter;
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
public class APIQuotaIgnoredTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


	@CitrusTest(name = "APIQuotaIgnoredTestIT")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Import an API containing a quota definition, but it should be ignored-");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/ignored-quota-api-${apiNumber}");
		variable("apiName", "Ignored-Quota-API-${apiNumber}");


		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("applicationPeriod", "hour");
        variable("applicationMb", "111111");
        variable("systemPeriod", "day");
        variable("systemMessages", "2222");
        variable("ignoreQuotas", "true");
		$(action(swaggerImport));

		$(echo("####### Validate API: '${apiName}' has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Check that the API-ID doesn't exists in the System-Quotas #######"));
        $(http().client(apiManager).send().get("/quotas/"+ APIManagerAdapter.SYSTEM_API_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@")));

        $(echo("####### Check that the API-ID doesn't exists in the Application-Quotas #######"));
        $(http().client(apiManager).send().get("/quotas/"+ APIManagerAdapter.APPLICATION_DEFAULT_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@")));
	}
}
