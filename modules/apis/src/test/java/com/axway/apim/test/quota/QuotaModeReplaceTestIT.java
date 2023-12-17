package com.axway.apim.test.quota;

import com.axway.apim.EndpointConfig;
import com.axway.apim.adapter.APIManagerAdapter;
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

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class QuotaModeReplaceTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest
	@Test
	public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("If the Quota-Mode is set to replace, evtl. existing quotas should be replaced.");
		variable("useApiAdmin", "true");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/quota-replace-api-${apiNumber}");
		variable("apiName", "Quota-${apiNumber}-Replace-API");
		variable("quotaMode", String.valueOf(Mode.replace));

		$(echo("####### Import a very basic API without any quota #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("state", "unpublished");
        variable("version", "1.0.0");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "${state}")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));
		$(echo("####### API: '${apiName}' on path: '${apiPath}' with ID: '${apiId}' imported #######"));

		$(echo("####### Get the operations/methods for the created API #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}/operations"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
				.expression("$.[?(@.name=='updatePetWithForm')].id", "testMethodId1")
				.expression("$.[?(@.name=='findPetsByStatus')].id", "testMethodId2")
				.expression("$.[?(@.name=='getPetById')].id", "testMethodId3")
				.expression("$.[?(@.name=='updateUser')].id", "testMethodId4")));

		$(echo("####### Define a manual application- and system-quota for the API: ${apiId} #######"));
        $(http().client(apiManager).send().put("/quotas/"+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).message().header("Content-Type", "application/json")
		.body("{\"id\":\""+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA+"\", \"type\":\"APPLICATION\",\"name\":\"Application Default\","
				+ "\"description\":\"Maximum message rates per application. Applied to each application unless an Application-Specific quota is configured\","
				+ "\"restrictions\":["
					+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId1}\",\"type\":\"throttlemb\",\"config\":{\"period\":\"hour\",\"per\":1,\"mb\":700}}, "
					+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId2}\",\"type\":\"throttle\",\"config\":{\"period\":\"day\",\"per\":2,\"messages\":100000}} "
				+ "],"
				+ "\"system\":true}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK));
        $(http().client(apiManager).send().put("/quotas/"+APIManagerAdapter.SYSTEM_API_QUOTA).message().header("Content-Type", "application/json")
		.body("{\"id\":\""+APIManagerAdapter.SYSTEM_API_QUOTA+"\", \"type\":\"API\",\"name\":\"System\","
				+ "\"description\":\"Maximum message rates aggregated across all client applications\","
				+ "\"restrictions\":["
					+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId3}\",\"type\":\"throttle\",\"config\":{\"period\":\"hour\",\"per\":3,\"messages\":1003}}, "
					+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId4}\",\"type\":\"throttlemb\",\"config\":{\"period\":\"day\",\"per\":4,\"mb\":500}} "
				+ "],"
				+ "\"system\":true}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK));

		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with Quotas configured that must replace existing quotas #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("systemPeriod", "day");
        variable("systemMessages", "666");
        variable("applicationPeriod", "hour");
        variable("applicationMb", "555");
        $(action(swaggerImport));

		$(echo("####### Check Application-Quotas have been setup as configured #######"));
        $(sleep().seconds(2));
        $(http().client(apiManager).send().get("/quotas/"+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			// The method specific quotas must be have been removed
			.expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].type", "@assertThat(empty())@")
			.expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].type", "@assertThat(empty())@")
			// These quota settings are inserted based on configuration by Swagger-Promote
			.expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.mb", "555")
			.expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.period", "hour")
			.expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.per", "1")));

		$(echo("####### Check that only the configured quota remains and previouls configured are removed #######"));
        $(sleep().seconds(2));
        $(http().client(apiManager).send().get("/quotas/"+APIManagerAdapter.SYSTEM_API_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			// The method specific quotas must be have been removed
			.expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].type", "@assertThat(empty())@")
			.expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].type", "@assertThat(empty())@")
			// These quota settings are inserted by Swagger-Promote based on configuration
			.expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.messages", "666")
			//.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.period", "day")
			.expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.per", "2")));

		$(echo("####### Executing a Quota-No-Change import #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("applicationMb", "555");
        variable("systemMessages", "666");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Perform a change in System-Default-Quota #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("applicationMb", "555");
        variable("systemMessages", "888");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Check System-Quotas have been updated #######"));
        $(sleep().seconds(2));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.messages", "888")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "2")));

		$(echo("####### Perform a change in Application-Default-Quota #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "published");
        variable("applicationMb", "777");
        variable("systemMessages", "888");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

		$(echo("####### Check Application-Quotas have been updated #######"));
        $(http().client(apiManager).send().get("/quotas/"+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
			.expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.mb", "777")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "hour")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "1")));

		$(echo("####### Make sure, the System-Quota stays unchanged with the last update #######"));
        $(http().client(apiManager).send().get("/quotas/"+APIManagerAdapter.SYSTEM_API_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.messages", "888")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
			.expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "2")));

		$(echo("####### Perform a breaking change, making sure, that defined Quotas persist #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "published");
        variable("enforce", "true");
        variable("systemMessages", "999");
        variable("applicationMb", "1888");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

		$(echo("####### Validate API: '${apiName}' has been re-imported with a new API-ID #######"));
        $(http().client(apiManager).send().get("/proxies").message().header("Content-Type", "application/json"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "published")).extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "newApiId")));

		$(echo("####### Check System-Quotas have been setup as configured for the new API #######"));
        $(sleep().seconds(2));
        $(http().client(apiManager).send().get("/quotas/"+APIManagerAdapter.SYSTEM_API_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
			.expression("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.expression("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "999")
			//.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "day")
			.expression("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
			.expression("$.restrictions.[?(@.api=='${newApiId}')].config.per", "2")));

		$(echo("####### Check Application-Quotas have been setup as configured for the new API  #######"));
        $(sleep().seconds(2));
        $(http().client(apiManager).send().get("/quotas/"+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.restrictions.[?(@.api=='${newApiId}')].type", "throttlemb")
			.expression("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.expression("$.restrictions.[?(@.api=='${newApiId}')].config.mb", "1888")
			//.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "hour")
			.expression("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
			.expression("$.restrictions.[?(@.api=='${newApiId}')].config.per", "1")));

	}
}
