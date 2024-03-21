package com.axway.apim.test.organizations;

import com.axway.apim.EndpointConfig;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class InvalidDevelopmentOrgTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest
    @Test
	public void runUnknownOrg() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Validates correct handling of invalid Owning-Organizations.");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/invalid-org-${apiNumber}");
		variable("apiName", "Invalid organization ${apiNumber}");
		// Directly use an admin-account, otherwise the OrgAdmin organization is used by default
		variable("oadminUsername1", "apiadmin");
		variable("oadminPassword1", "changeme");
		variable("testOrgName", "Invalid organization ${orgNumber}");
		variable("testOrgName", "Org without permission ${apiNumber}");
		$(echo("####### Try to import an API with an invalid organization - Must be handled with a proper error-code and message #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/dynamic-organization.json");
        variable("state", "published");
        variable("expectedReturnCode", "57");
		$(action(swaggerImport));
	}

	@CitrusTest
    @Test
	public void runNonDevOrg() {
        ImportTestAction swaggerImport = new ImportTestAction();
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/invalid-org-${apiNumber}");
		variable("apiName", "Invalid organization ${apiNumber}");
		// Directly use an admin-account, otherwise the OrgAdmin organization is used by default
		variable("oadminUsername1", "apiadmin");
		variable("oadminPassword1", "changeme");
		variable("testOrgName", "NonDevOrg ${apiNumber}");

        $(http().client(apiManager).send().post("/organizations").name("anotherOrgCreatedRequest").message()
			.header("Content-Type", "application/json")
			.body("{\"name\": \"NonDevOrg ${apiNumber}\", \"description\": \"Org without dev permission\", \"enabled\": true, \"development\": false }"));

        $(http().client(apiManager).receive().response(HttpStatus.CREATED)
            .message().type(MessageType.JSON).extract(jsonPath()
			.expression("$.name", "${testOrgName}"))
            .extract(fromBody()
			.expression("$.id", "noPermOrgId")));

        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/dynamic-organization.json");
        variable("state", "published");
        variable("expectedReturnCode", "57");
		$(action(swaggerImport));

	}


}
