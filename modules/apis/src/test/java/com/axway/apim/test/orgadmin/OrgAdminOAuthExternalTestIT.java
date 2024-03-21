package com.axway.apim.test.orgadmin;

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

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class OrgAdminOAuthExternalTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest(name = "OrgAdminOAuthExternalTestIT")
    @Test
    public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Org-Admin only account tests for API-OAuth (External) Security configuration");
        variable("useApiAdmin", "true");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/oadmin-oauth-test-${apiNumber}");
        variable("apiName", "OAdmin OAuth-External Test ${apiNumber}");
        variable("status", "unpublished");

        $(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("tokenInfoPolicy", "Tokeninfo policy 1");
        variable("accessTokenLocation", "HEADER");
        variable("scopes", "resource.WRITE, resource.READ, resource.ADMIN");
        variable("removeCredentialsOnSuccess", "false");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/4_api-oauth_external.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' with correct settings #######"));
        $(http().client(apiManager).send().get("/proxies"));

        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].type", "oauthExternal")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.tokenStore", "@assertThat(containsString(${tokenInfoPolicy}))@")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.scopes", "${scopes}")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.useClientRegistry", "true")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.['oauth.token.client_id']", "${//oauth.token.client_id//}")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.['oauth.token.scopes']", "${//oauth.token.scopes//}")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.['oauth.token.valid']", "${//oauth.token.valid//}")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.accessTokenLocation", "${accessTokenLocation}"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Simulate re-import with no-change #######"));
        variable("tokenInfoPolicy", "Tokeninfo policy 1");
        variable("accessTokenLocation", "HEADER");
        variable("scopes", "resource.WRITE, resource.READ, resource.ADMIN");
        variable("removeCredentialsOnSuccess", "false");
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/security/4_api-oauth_external.json");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));
    }
}
