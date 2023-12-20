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
public class AppOrSystemQuotaOnlyTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Validates quota is set when only System- or Application-Quota is configured (see bug #55)");
        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/only-one-quota-api-${apiNumber}");
        variable("apiName", "Only-One-Quota-API-${apiNumber}");
        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with System-Quota only #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/3_api-with-system-quota-only.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        variable("systemPeriod", "hour");
        variable("messages", "345");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has a been imported including System-Quota #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "published")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Check System-Quotas have been setup as configured #######"));
        $(echo("####### ############ Sleep 2 seconds ##################### #######"));
        $(sleep().seconds(2));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.messages", "345")
            //.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "hour")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "5")));

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with Appliction-Quota only #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/4_api-with-app-quota-only2.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        variable("applicationPeriod", "day");
        $(action(swaggerImport));

        $(echo("####### Check Application-Quotas have been setup as configured #######"));
        $(echo("####### ############ Sleep 2 seconds ##################### #######"));
        $(sleep().seconds(2));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000001").name("api"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
            .expression("$.restrictions.[?(@.api=='${apiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.mb", "787")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
            .expression("$.restrictions.[?(@.api=='${apiId}')].config.per", "4")));

    }
}
