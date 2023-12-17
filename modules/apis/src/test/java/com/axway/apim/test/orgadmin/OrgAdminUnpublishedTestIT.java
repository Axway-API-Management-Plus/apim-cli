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


@ContextConfiguration(classes = {EndpointConfig.class})
public class OrgAdminUnpublishedTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "OrgAdminUnpublishedTestIT")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("OrgAdmins should be able to register unpublished APIs and manage them.");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/org-admin-${apiNumber}");
		variable("apiName", "OrgAdmin-Unpublished-${apiNumber}");

		$(echo("####### Calling the tool with a Non-Admin-User. #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/1_no-change-config.json");
        variable("expectedReturnCode", "0");
		$(action(swaggerImport));

		$(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported using OrgAdmin only #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")));

        $(echo("####### Re-Import another Swagger-File still unpublished. #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/1_no-change-config.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));
	}

}
