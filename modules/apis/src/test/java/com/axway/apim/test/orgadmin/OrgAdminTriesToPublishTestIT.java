package com.axway.apim.test.orgadmin;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;


@ContextConfiguration(classes = {EndpointConfig.class})
public class OrgAdminTriesToPublishTestIT extends TestNGCitrusSpringSupport {

	@CitrusTest
	@Test
	public void allowOrgAdminsToPublishApi() {
		ImportTestAction swaggerImport = new ImportTestAction();
		description("But OrgAdmins should not being allowed to register published APIs.");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/org-admin-published-${apiNumber}");
		variable("apiName", "OrgAdmin-Published-${apiNumber}");
		$(echo("####### Calling the tool with a Non-Admin-User. #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/2_initially_published.json");
        variable("expectedReturnCode", "0");
		$(action(swaggerImport));
	}
}
