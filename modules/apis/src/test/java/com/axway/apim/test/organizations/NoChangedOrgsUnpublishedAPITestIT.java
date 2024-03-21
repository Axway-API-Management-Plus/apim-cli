package com.axway.apim.test.organizations;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;

@ContextConfiguration(classes = {EndpointConfig.class})
public class NoChangedOrgsUnpublishedAPITestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;
	@CitrusTest
	@Test
	public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Making sure, organization are not conisdered as changes if desired state is Unpublished");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/no-change-org-unpublished-${apiNumber}");
		variable("apiName", "No-Change-Org-Unpublished-${apiNumber}");

		// Replication must fail, is Query-String option is enabled, but API-Manager hasn't configured it
		$(echo("####### API-Config without queryString option - Must fail #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "unpublished");
        variable("orgName", "${orgName}");
        variable("orgName2", "${orgName2}");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

		$(echo("####### Re-Import the same - Must lead to a No-Change! #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/1_api-with-client-orgs.json");
        variable("state", "unpublished");
        variable("orgName", "${orgName}");
        variable("orgName2", "${orgName2}");
        variable("expectedReturnCode", "10"); // No-Change is expected!
		$(action(swaggerImport));
	}
}
