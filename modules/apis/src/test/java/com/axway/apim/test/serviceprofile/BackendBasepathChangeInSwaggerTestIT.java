package com.axway.apim.test.serviceprofile;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.DefaultTestActionBuilder;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.actions.EchoAction.Builder.echo;

@ContextConfiguration(classes = {EndpointConfig.class})
public class BackendBasepathChangeInSwaggerTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest
	@Test
	public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Import Swagger Spec without any host");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/basepath-changed-in-swagger-test-${apiNumber}");
		variable("apiName", "Basepath changed in Swagger Test ${apiNumber}");
		$(echo("####### Try to import an API without having the host configured at all #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore-without-any-host.json");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/serviceprofile/2_backend_basepath_test.json");
        variable("backendBasepath", "https://petstore.swagger.io");
        variable("apiPath", "/basepath-changed-in-swagger-test-2-${apiNumber}");
        variable("apiName", "Basepath changed in Swagger Test 2 ${apiNumber}");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        $(DefaultTestActionBuilder.action(swaggerImport));
	}
}
