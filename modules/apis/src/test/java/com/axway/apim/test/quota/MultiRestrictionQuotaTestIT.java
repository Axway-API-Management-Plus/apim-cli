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
public class MultiRestrictionQuotaTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest
	@Test
	public void run() throws IOException {
		ImportTestAction swaggerImport = new ImportTestAction();
		description("Make sure you can configured multiple Quota-Restrictions for an API");
		variable("oadminUsername1","${apiManagerUser}");
		variable("oadminPassword1","${apiManagerPass}");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/multi-quota-restriction--api-${apiNumber}");
		variable("apiName", "Multi-Quota-Restriction-API-${apiNumber}");
		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/5_multi-restriction-quota.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("systemPeriod1", "hour");
        variable("systemMessages1", "666");
        variable("systemPer1", "2");
        variable("systemPeriod2", "day");
        variable("systemMessages2", "100000");
        variable("systemPer2", "1");
        variable("applicationPeriod1", "hour");
        variable("applicationMB1", "30");
        variable("applicationPer1", "1");
        variable("applicationPeriod2", "day");
        variable("applicationMB2", "1024");
        variable("applicationPer2", "1");
        $(action(swaggerImport));

		$(echo("####### Validate API: '${apiName}' has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "${state}")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));
        $(sleep().seconds(5));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttle,throttle")
			.expression("$.restrictions.[?(@.api=='${apiId}')].method", "*,*")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.period", "${systemPeriod1},${systemPeriod2}")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.messages", "${systemMessages1},${systemMessages2}")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "${systemPer1},${systemPer2}")));

        $(echo("####### Check Application-Quotas have been setup as configured #######"));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000001"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb,throttlemb")
			.expression("$.restrictions.[?(@.api=='${apiId}')].method", "*,*")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.period", "${applicationPeriod1},${applicationPeriod2}")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.mb", "${applicationMB1},${applicationMB2}")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "${applicationPer1},${applicationPer2}")));

		$(echo("####### Perform a No-Change test #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/5_multi-restriction-quota.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));
	}
}
