package com.axway.apim.test.quota;

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
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class QuotaStays4OtherAPIsTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest
	@Test
	public void run() throws IOException, InterruptedException {
		ImportTestAction swaggerImport = new ImportTestAction();
		description("Making sure, APIs for other APIs are not influences by Quota-Management for the actual API.");
		variable("useApiAdmin", "true");
		variable("firstApiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("firstApiPath", "/first-quota-api-${firstApiNumber}");
		variable("firstApiName", "First Quota-API-${firstApiNumber}");
		variable("secApiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("secApiPath", "/second-quota-api-${firstApiNumber}");
		variable("secApiName", "Second Quota-API-${firstApiNumber}");


		$(echo("####### Importing the first API: '${firstApiName}' on path: '${firstApiPath}' with some quotas #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("applicationPeriod", "hour");
        variable("applicationMb", "555");
        variable("systemPeriod", "day");
        variable("systemMessages", "666");
        variable("apiName", "${firstApiName}");
        variable("apiPath", "${firstApiPath}");
        $(action(swaggerImport));

		$(echo("####### Validate API: '${firstApiName}' has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${firstApiPath}')].name", "${firstApiName}")
			.expression("$.[?(@.path=='${firstApiPath}')].state", "unpublished")).extract(fromBody()
			.expression("$.[?(@.path=='${firstApiPath}')].id", "firstApiId")));
		$(echo("####### First API: '${firstApiName}' (ID: ${firstApiId}) has a been imported #######"));

		$(echo("####### Check System-Quotas have been setup as configured for the first API #######"));
        $(sleep().seconds(5));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].type", "throttle")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].method", "*")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].config.messages", "666")
			//.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.period", "day")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].config.per", "2")));

		$(echo("####### Check Application-Quotas have been setup as configured #######"));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000001"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].type", "throttlemb")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].method", "*")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].config.mb", "555")
			//.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.period", "hour")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].config.per", "1")));

		$(echo("####### Import a second API also with Quotas #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("applicationMb", "777");
        variable("systemMessages", "888");
        variable("systemPeriod", "week");
        variable("applicationPeriod", "second");
        variable("apiName", "${secApiName}");
        variable("apiPath", "${secApiPath}");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

		$(echo("####### Make sure quotas from the First API are still there! #######"));
		$(echo("####### Check System-Quotas have been setup as configured for the first API #######"));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].type", "throttle")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].method", "*")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].config.messages", "666")
			//.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.period", "day")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].config.per", "2")));

		$(echo("####### Check Application-Quotas have been setup as configured #######"));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000001"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].type", "throttlemb")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].method", "*")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].config.mb", "555")
			//.validate("$.restrictions.[?(@.api=='${firstApiId}')].config.period", "hour")
			.expression("$.restrictions.[?(@.api=='${firstApiId}')].config.per", "1")));
	}
}
