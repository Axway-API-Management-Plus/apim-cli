package com.axway.apim.test.vhost;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
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

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class VhostConfigOrgWithVHostTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


	@CitrusTest
	@Test
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Test VHost with an organization having a Default virtual host");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/vhost-org-test-${apiNumber}");
		variable("apiName", "VHost Org-Test ${apiNumber}");
		variable("vhost", "abc.company.com");
		variable("vhostOrgName", "VHost Org ${orgNumber}");
		// Directly use an admin-account, otherwise the OrgAdmin organization is used by default
		variable("oadminUsername1", "apiadmin");
		variable("oadminPassword1", "changeme");

        $(http().client(apiManager).send().post("/organizations").message().header("Content-Type", "application/json")
				.body("{\"name\": \"${vhostOrgName}\", \"description\": \"Org 1 with dev permission and VHost\", \"enabled\": true, \"development\": true, \"virtualHost\": \"${vhost}\" }"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
				.expression("$.name", "${vhostOrgName}")).extract(fromBody()
				.expression("$.id", "vhostOrgId")));

		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("status", "published");
        variable("vhost", "${vhost}"); // Using the same V-Host as configured for the organization.
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/vhost/2_vhost-config.json");
        variable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

		// Validate the API has been imported correctly
		$(echo("####### Validate the API has been imported correctly #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
				.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.expression("$.[?(@.path=='${apiPath}')].state", "${status}")
				.expression("$.[?(@.path=='${apiPath}')].vhost", "${vhost}")).extract(fromBody()
				.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Manually unpublish this API! #######"));
        $(http().client(apiManager).send().post("/proxies/${apiId}/unpublish"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
				.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")));

        $(echo("####### Re-Import the API and the VHost must be configured again #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/vhost/2_vhost-config.json");
        variable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
				.expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
				.expression("$.[?(@.id=='${apiId}')].state", "${status}")
				.expression("$.[?(@.id=='${apiId}')].vhost", "${vhost}")));
	}
}
