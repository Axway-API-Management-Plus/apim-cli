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
public class DontOverwriteManualQuotaTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException, InterruptedException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Swagger-Promote Quota should only overwrite configured Quota-Information and leave manual Quota unchanged!");
        /*
         * The Keys inside restrictions to identify an existing System- or Application-Default-Quota
         * - the API the quota should be applied
         * - the Method the quota should be applied
         * - the type (throttle or throttlemb), as you may have for one API/API-Method two different types
         * - the period, as you may want to configured a overall quota for a day and for one hour
         * - the per - You may want to configured 1 quota per 1 hour and another for 8 hours
         *
         * If all these keys are the same, the quota-setting is the same and should be overwritten!
         */

        variable("useApiAdmin", "true"); // Use apiadmin account
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/dont-overwrite-quota-restriction-api-${apiNumber}");
        variable("apiName", "Quota-${apiNumber}-Multi-Restriction-API");

        $(echo("####### Import a very basic API without any quota #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
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

        $(echo("####### Define a manual application- and system-quotas for the API: ${apiId} on specific methods #######"));
        $(http().client(apiManager).send().put("/quotas/" + APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).message().header("Content-Type", "application/json")
            .body("{\"id\":\"" + APIManagerAdapter.APPLICATION_DEFAULT_QUOTA + "\", \"type\":\"APPLICATION\",\"name\":\"Application Default\","
                + "\"description\":\"Maximum message rates per application. Applied to each application unless an Application-Specific quota is configured\","
                + "\"restrictions\":["
                + "{\"api\":\"${apiId}\",\"method\":\"${testMethodId1}\",\"type\":\"throttlemb\",\"config\":{\"period\":\"hour\",\"per\":1,\"mb\":700}}, "
                + "{\"api\":\"${apiId}\",\"method\":\"${testMethodId2}\",\"type\":\"throttle\",\"config\":{\"period\":\"day\",\"per\":2,\"messages\":100000}} "
                + "],"
                + "\"system\":true}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK));
        $(http().client(apiManager).send().put("/quotas/" + APIManagerAdapter.SYSTEM_API_QUOTA).message().header("Content-Type", "application/json")
            .body("{\"id\":\"" + APIManagerAdapter.SYSTEM_API_QUOTA + "\", \"type\":\"API\",\"name\":\"System\","
                + "\"description\":\"Maximum message rates aggregated across all client applications\","
                + "\"restrictions\":["
                + "{\"api\":\"${apiId}\",\"method\":\"${testMethodId3}\",\"type\":\"throttle\",\"config\":{\"period\":\"hour\",\"per\":3,\"messages\":1003}}, "
                + "{\"api\":\"${apiId}\",\"method\":\"${testMethodId4}\",\"type\":\"throttlemb\",\"config\":{\"period\":\"day\",\"per\":4,\"mb\":500}} "
                + "],"
                + "\"system\":true}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK));
        $(echo("####### RECREATE the same API without any configured quotas #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}')].state", "${state}")).extract(fromBody()
            .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));
        $(echo("####### API: '${apiName}' on path: '${apiPath}' with ID: '${apiId}' imported (RECREATED) #######"));

        $(echo("####### Get the operations/methods for the RE-CREATED API #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}/operations"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.[?(@.name=='updatePetWithForm')].id", "testMethodId1")
            .expression("$.[?(@.name=='findPetsByStatus')].id", "testMethodId2")
            .expression("$.[?(@.name=='getPetById')].id", "testMethodId3")
            .expression("$.[?(@.name=='updateUser')].id", "testMethodId4")));

        $(echo("####### Validate all previously configured APPLICATION quotas (manually configured) do exists #######"));
        $(sleep().seconds(10));
        $(http().client(apiManager).send().get("/quotas/" + APIManagerAdapter.APPLICATION_DEFAULT_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].type", "throttlemb")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].config.per", "1")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].config.mb", "700")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].config.per", "2")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].config.messages", "100000")));

        $(echo("####### Validate all previously configured SYSTEM quotas (manually configured) do exists #######"));
        $(http().client(apiManager).send().get("/quotas/" + APIManagerAdapter.SYSTEM_API_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].type", "throttle")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].config.per", "3")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].config.messages", "1003")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].type", "throttlemb")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].config.per", "4")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].config.mb", "500")));

        $(echo("####### Replicate the same API with some Quotas configured, which are different to the one manually defined before #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("applicationPeriod", "day");
        variable("applicationMb", "555");
        variable("systemPeriod", "week");
        variable("systemMessages", "666");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate all APPLICATION quotas (manually configured & API-Config) do exists #######"));
        $(echo("####### ############ Sleep 2 seconds ##################### #######"));
        $(sleep().seconds(5));
        $(http().client(apiManager).send().get("/quotas/" + APIManagerAdapter.APPLICATION_DEFAULT_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].type", "throttlemb")
            //.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].config.period", "hour")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].config.per", "1")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].config.mb", "700")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].type", "throttle")
            //.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].config.period", "day")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].config.per", "2")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].config.messages", "100000")
            // These quota settings are inserted by Swagger-Promote based on configuration
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.mb", "555")
            //.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.period", "day")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.per", "1")));

        $(echo("####### Validate all SYSTEM quotas (manually configured & API-Config) do exists #######"));
        $(sleep().seconds(5));
        $(http().client(apiManager).send().get("/quotas/" + APIManagerAdapter.SYSTEM_API_QUOTA));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].type", "throttle")
            //.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].config.period", "hour")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].config.per", "3")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].config.messages", "1003")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].type", "throttlemb")
            //.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].config.period", "day")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].config.per", "4")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].config.mb", "500")
            // These quota settings are inserted based on configuration by Swagger-Promote
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.messages", "666")
            //.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.period", "week")
            .expression("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.per", "2")));
    }
}
