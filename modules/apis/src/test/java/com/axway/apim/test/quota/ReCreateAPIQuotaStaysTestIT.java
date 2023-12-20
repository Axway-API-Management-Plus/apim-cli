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
public class ReCreateAPIQuotaStaysTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Validate the use-case described in issue #86 works as expected.");

        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/recreate-with-app-quota-${apiNumber}");
        variable("apiName", "Recreate-with-App-Quota-${apiNumber}");

        $(echo("####### Create an API as given in the issue #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/issue-86-api-with-app-quota.json");
        variable("state", "published");
        variable("image", "/com/axway/apim/test/files/basic/API-Logo.jpg");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "${state}")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        variable("appName", "Recreate-with-App-Quota ${apiNumber}");
        variable("appName2", "Recreate-with-App-Quota 2 ${apiNumber}");
        $(echo("####### Create an application: '${appName}', used to subscribe to that API #######"));
        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
            .body("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "testAppId")
            .expression("$.name", "testAppName")));

        $(echo("####### Create a second application: '${appName}', used to subscribe to that API #######"));
        $(http().client(apiManager).send().post("/applications").message().header("Content-Type", "application/json")
            .body("{\"name\":\"${appName2}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.id", "testAppId2")
            .expression("$.name", "testAppName2")));

        $(echo("####### Grant access to org2 for this API  #######"));
        $(http().client(apiManager).send().post("/proxies/grantaccess").message().header("Content-Type", "application/x-www-form-urlencoded")
            .body("action=orgs&apiId=${apiId}&grantOrgId=${orgId2}"));
        $(http().client(apiManager).receive().response(HttpStatus.NO_CONTENT));


        $(echo("####### Subscribe App 1 to the API #######"));
        $(http().client(apiManager).send().post("/applications/${testAppId}/apis").message().header("Content-Type", "application/json")
            .body("{\"apiId\":\"${apiId}\",\"enabled\":true}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED));

        $(echo("####### Subscribe App 2 to the API (but without App-Quota) #######"));
        $(http().client(apiManager).send().post("/applications/${testAppId2}/apis").message().header("Content-Type", "application/json")
            .body("{\"apiId\":\"${apiId}\",\"enabled\":true}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED));

        $(echo("####### Configure an Application specfic quota override for this APP, which must be taken over when re-creating this API #######"));
        $(http().client(apiManager).send().post("/applications/${testAppId}/quota").message().header("Content-Type", "application/json")
            .body("{\"type\":\"APPLICATION\",\"name\":\"Recreate-with-App-Quota 758 Quota\",\"restrictions\":[{\"api\":\"${apiId}\",\"method\":\"*\",\"type\":\"throttle\",\"config\":{\"period\":\"hour\",\"messages\":600,\"per\":4}}]}"));
        $(http().client(apiManager).receive().response(HttpStatus.CREATED));

        $(echo("####### For a Re-Creation of the API which fails according to the issue 86 #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/issue-86-api-with-app-quota.json");
        variable("state", "published");
        variable("image", "/com/axway/apim/test/files/basic/API-Logo.jpg");
        variable("enforce", "true");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate the new APIs has been created #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "${state}")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "newApiId")));

        $(echo("####### Validate the application DEFAULT quota is set for the API as before #######"));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000001"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "25")
            //.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "second")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.per", "60")));
        $(sleep().seconds(10));
        $(echo("####### Validate the application 1 SPECIFIC quota override is set for the API as before #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId}/quota"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "600")
            //.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "hour")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.per", "4")));

        $(echo("####### Validate the application 2 returns the App-Default-Quota #######"));
        $(http().client(apiManager).send().get("/applications/${testAppId2}/quota"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "25")
            //.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "second")
            .expression("$.restrictions.[?(@.api=='${newApiId}')].config.per", "60")));
    }
}
