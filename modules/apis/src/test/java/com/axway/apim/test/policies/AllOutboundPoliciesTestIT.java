package com.axway.apim.test.policies;

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
public class AllOutboundPoliciesTestIT  extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest(name = "AllOutboundPoliciesTest")
    @Test
	public void run() {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Test a Request-Policy");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/all-outbound-policies-${apiNumber}");
		variable("apiName", "All Outbound Policies ${apiNumber}");
		variable("status", "unpublished");
		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######"));
        variable("requestPolicy", "Request policy 1");
        variable("responsePolicy", "Response policy 1");
        variable("routePolicy", "Routing policy 1");
        variable("faultHandlerPolicy", "Faulthandler policy 1");
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/policies/1_all-policies.json");
        variable("expectedReturnCode", "0");
		$(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has correct settings #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.expression("$.[?(@.path=='${apiPath}')].outboundProfiles._default.requestPolicy", "@assertThat(containsString(Request policy 1))@")
			.expression("$.[?(@.path=='${apiPath}')].outboundProfiles._default.responsePolicy", "@assertThat(containsString(Response policy 1))@")
			.expression("$.[?(@.path=='${apiPath}')].outboundProfiles._default.routePolicy", "@assertThat(containsString(Routing policy 1))@")
			.expression("$.[?(@.path=='${apiPath}')].outboundProfiles._default.faultHandlerPolicy", "@assertThat(containsString(Faulthandler policy 1))@")));
	}
}
