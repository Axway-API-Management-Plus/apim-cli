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

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class APIBasicQuotaTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException, InterruptedException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import an API containing a quota definition");
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/quota-api-${apiNumber}");
        variable("apiName", "Quota-API-${apiNumber}");
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("applicationPeriod", "hour");
        variable("applicationMb", "555");
        variable("systemPeriod", "day");
        variable("systemMessages", "666");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Check System-Quotas have been setup as configured #######"));
        $(echo("####### ############ Sleep 2 seconds ##################### #######"));
        $(sleep().seconds(2));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.messages", "666")
            //.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "2")));

        $(echo("####### Check Application-Quotas have been setup as configured #######"));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000001"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
            .expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.mb", "555")
            //.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "hour")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "1")));

        $(echo("####### Executing a Quota-No-Change import #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("applicationMb", "555");
        variable("systemMessages", "666");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

        $(echo("####### Perform a change in System-Default-Quota #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("applicationMb", "555");
        variable("systemMessages", "888");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Check System-Quotas have been updated #######"));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.messages", "888")
            //.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "2")));

        $(echo("####### Perform a change in Application-Default-Quota #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "published");
        variable("applicationMb", "777");
        variable("systemMessages", "888");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(sleep().seconds(5));
        $(echo("####### Check Application-Quotas have been updated #######"));
        $(http().client(apiManager).send().get("/quotas/" + APIManagerAdapter.APPLICATION_DEFAULT_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
            .expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.mb", "777")
            //.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "hour")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "1")));

        $(echo("####### Make sure, the System-Quota stays unchanged with the last update #######"));
        $(http().client(apiManager).send().get("/quotas/" + APIManagerAdapter.SYSTEM_API_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.messages", "888")
            //.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "2")));

        $(echo("####### Perform a breaking change, making sure, that defined Quotas persist #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "published");
        variable("enforce", "true");
        variable("systemMessages", "666");
        variable("applicationMb", "555");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));
        $(sleep().seconds(5));
        $(echo("####### Validate API: '${apiName}' has been re-imported with a new API-ID #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "published")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "newApiId")));

        $(echo("####### Check System-Quotas have been setup as configured for the new API #######"));
        $(http().client(apiManager).send().get("/quotas/" + APIManagerAdapter.SYSTEM_API_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "666")
            //.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "day")
            .expression("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.per", "2")));

        $(echo("####### Check Application-Quotas have been setup as configured for the new API  #######"));
        $(http().client(apiManager).send().get("/quotas/" + APIManagerAdapter.APPLICATION_DEFAULT_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${newApiId}')].type", "throttlemb")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.mb", "555")
            //.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "hour")
            .expression("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.per", "1")));
    }
}
