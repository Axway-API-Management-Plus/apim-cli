package com.axway.apim.test.quota;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.base.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.dsl.DefaultTestActionBuilder.action;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.json.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

/**
 * Proves that a quota update is surgical rather than a replacement of the complete quota document.
 * <p>
 * Each restriction of a quota is addressable below /quotas/{id}/constraints and carries an ID assigned by the
 * API-Manager. This test records those IDs and verifies that importing another API, and changing the quota of an
 * API, leaves the identity of every other restriction untouched. A replace-all update cannot guarantee that.
 */
@ContextConfiguration(classes = {EndpointConfig.class})
public class QuotaConstraintScopeChangeTestIT extends TestNGCitrusSpringSupport {

    private static final String SYSTEM_QUOTA = "/quotas/00000000-0000-0000-0000-000000000000";

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run(@Optional @CitrusResource TestContext context) throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Make sure updating the quota of one API does not disturb the restrictions of other APIs");
        variable("oadminUsername1", "${apiManagerUser}");
        variable("oadminPassword1", "${apiManagerPass}");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/quota-constraint-api-1-${apiNumber}");
        variable("apiName", "Quota-Constraint-API-1-${apiNumber}");
        variable("apiPath2", "/quota-constraint-api-2-${apiNumber}");
        variable("apiName2", "Quota-Constraint-API-2-${apiNumber}");

        $(echo("####### Importing the first API: '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("systemPeriod", "hour");
        variable("systemMessages", "1000");
        variable("applicationPeriod", "day");
        variable("applicationMb", "100");
        $(action(swaggerImport));

        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON)
            .validate(jsonPath().expression("$.[?(@.path=='${apiPath}')].name", "${apiName}"))
            .extract(fromBody().expression("$.[?(@.path=='${apiPath}')].id", "apiId")));
        $(sleep().seconds(5));

        $(http().client(apiManager).send().get("/config"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON)
            .extract(fromBody().expression("$.productVersion", "productVersion")));

        // Read the extracted test variable into a Java variable, e.g. "7.7-20260228" -> 20260228
        String productVersion = context.getVariable("productVersion");
        $(echo("####### Product version is: " + productVersion + " #######"));
        Matcher matcher = Pattern.compile("(\\d{8})$").matcher(productVersion);
        if (matcher.find() && Integer.parseInt(matcher.group(1)) < 20261130) {
            $(echo("Skipping the Quota constraint test, API-Manager " + productVersion + " does not support it"));
            //    return;
        }

        $(echo("####### Record the constraint ID the API-Manager assigned to the restriction of API 1 #######"));
        $(http().client(apiManager).send().get(SYSTEM_QUOTA + "/constraints"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON)
            .validate(jsonPath()
                .expression("$.[?(@.api=='${apiId}')].type", "throttle")
                .expression("$.[?(@.api=='${apiId}')].config.messages", "${systemMessages}"))
            .extract(fromBody().expression("$.[?(@.api=='${apiId}')].id", "api1ConstraintId")));

        $(echo("####### Importing a second API, which must not touch the restriction of API 1 #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("apiPath", "${apiPath2}");
        variable("apiName", "${apiName2}");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("systemMessages", "2000");
        $(action(swaggerImport));
        $(sleep().seconds(5));

        $(echo("####### The restriction of API 1 must still carry the very same constraint ID #######"));
        $(http().client(apiManager).send().get(SYSTEM_QUOTA + "/constraints"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON)
            .validate(jsonPath()
                .expression("$.[?(@.api=='${apiId}')].id", "${api1ConstraintId}")
                .expression("$.[?(@.api=='${apiId}')].config.messages", "1000")));

        $(echo("####### Changing the quota of API 1 must update the restriction in place, keeping its ID #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/1_api-with-quota.json");
        variable("apiPath", "/quota-constraint-api-1-${apiNumber}");
        variable("apiName", "Quota-Constraint-API-1-${apiNumber}");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("systemMessages", "3000");
        $(action(swaggerImport));
        $(sleep().seconds(5));

        $(http().client(apiManager).send().get(SYSTEM_QUOTA + "/constraints"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON)
            .validate(jsonPath()
                .expression("$.[?(@.api=='${apiId}')].id", "${api1ConstraintId}")
                .expression("$.[?(@.api=='${apiId}')].config.messages", "3000")));
    }
}
