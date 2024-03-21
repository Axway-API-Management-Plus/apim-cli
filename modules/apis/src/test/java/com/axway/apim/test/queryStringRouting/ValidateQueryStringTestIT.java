package com.axway.apim.test.queryStringRouting;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ValidateQueryStringTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;
    private static final ObjectMapper mapper = new ObjectMapper();

    @CitrusTest
    @Test
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Validate query string routing can be controlled and works as expected.");
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/query-string-api-${apiNumber}");
        variable("apiName", "Query-String-API-${apiNumber}");

        // A feature that must be turned On before (and as it becomes global it must be turned off again afterwards)
        // Turn Query-Based-Routing ON
        $(echo("Turn Query-String feature ON in API-Manager to test it"));
        $(http().client(apiManager).send().get("/config"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$", "managerConfig")));
        variable("updatedConfig", enableQueryBasedRouting(context.getVariable("managerConfig"), "abc"));
        $(echo("updatedConfig: ${updatedConfig}"));
        $(http().client(apiManager).send().put("/config").message().header("Content-Type", "application/json")
            .body("${updatedConfig}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK));

        $(echo("####### Register an API WITHOUT Query string, having query string routing option enabled in API-Manager #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/queryStringRouting/api_without_query_string.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' without the routing key has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
                .expression("$.[?(@.path=='${apiPath}')].state", "published")
                .expression("$.[?(@.path=='${apiPath}')].apiRoutingKey", ""))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiIdWithoutRoutingKey")));

        $(echo("####### Register the SAME API but with a query string version #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("apiRoutingKey", "routeKeyA");
        variable("apiName", "apiName-${apiRoutingKey}");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' with a routing key has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].state", "unpublished")
                .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey==null)].state", "published")
                .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='routeKeyA')].apiRoutingKey", "routeKeyA"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiIdWithRoutingKey")));

        $(echo("####### Re-Import the same API with same Routing-Key must lead to a No-Change #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "10");
        variable("apiRoutingKey", "routeKeyA");
        variable("apiName", "apiName-${apiRoutingKey}");
        $(action(swaggerImport));

        $(echo("####### Re-Import the same API with a DIFFERENT Routing-Key must lead to a NEW API #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        variable("apiRoutingKey", "routeKeyB");
        variable("apiName", "apiName-${apiRoutingKey}");
        $(action(swaggerImport));

        $(echo("####### Validate the second API: '${apiName}' with a new API-Routing key has a been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].state", "${state}")
                .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].apiRoutingKey", "routeKeyB"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId2")));

        $(echo("####### Perform a No-Change #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
        variable("state", "published");
        variable("apiRoutingKey", "routeKeyB");
        variable("expectedReturnCode", "10"); // No-Change
        $(action(swaggerImport));

        $(echo("####### Change the main API not having an API-Routing key #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/queryStringRouting/api_without_query_string.json");
        variable("state", "published");
        variable("expectedReturnCode", "0");
        variable("apiRoutingKey", "");
        variable("apiName", "apiName");
        $(action(swaggerImport));

        // Turn Query-Based-Routing OFF
        variable("updatedConfig", disableQueryBasedRouting(context.getVariable("managerConfig")));
        $(http().client(apiManager).send().put("/config").message().header("Content-Type", "application/json")
            .body("${updatedConfig}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK));


        $(echo("####### Try to register an API with Query-String, but API-Manager has this option disabled #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
        variable("apiPath", "/query-string-api-fail-${apiNumber}");
        variable("apiName", "Query-String-API-Fail-${apiNumber}");
        variable("state", "unpublished");
        variable("apiRoutingKey", "routeKeyC");
        variable("apiName", "apiName-${apiRoutingKey}");
        variable("expectedReturnCode", "53"); // Must fail!
        $(action(swaggerImport));

        $(echo("####### Try to register an API with Query-String (using OrgAdminOnly) - Leads to a Warning-Message #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/queryStringRouting/api_with_query_string.json");
        variable("apiPath", "/query-string-api-oadmin-${apiNumber}");
        variable("apiName", "Query-String-API-OAdmin-${apiNumber}");
        variable("state", "unpublished");
        variable("useApiAdmin", "false"); // This tests simulate to use only an Org-Admin-Account
        variable("apiRoutingKey", "routeKeyD");
        variable("apiName", "apiName-${apiRoutingKey}");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].name", "${apiName}")
            .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].state", "${state}")
            .expression("$.[?(@.path=='${apiPath}' && @.apiRoutingKey=='${apiRoutingKey}')].apiRoutingKey", "${apiRoutingKey}")));
    }

    public static String enableQueryBasedRouting(String managerConfig, String versionParameter) throws IOException {
        // "apiRoutingKeyLocation": null, --> This is what we have
        // "apiRoutingKeyLocation":"query|ver", --> This is what we need
        JsonNode config = mapper.readTree(managerConfig);
        ((ObjectNode) config).put("apiRoutingKeyEnabled", true);
        ((ObjectNode) config).put("apiRoutingKeyLocation", "query|" + versionParameter);
        return mapper.writeValueAsString(config);
    }

    public static String disableQueryBasedRouting(String managerConfig) throws IOException {
        JsonNode config = mapper.readTree(managerConfig);
        ((ObjectNode) config).put("apiRoutingKeyEnabled", false);
        return mapper.writeValueAsString(config);
    }
}
