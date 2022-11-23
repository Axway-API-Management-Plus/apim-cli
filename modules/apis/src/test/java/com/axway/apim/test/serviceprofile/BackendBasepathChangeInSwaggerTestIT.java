package com.axway.apim.test.serviceprofile;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class BackendBasepathChangeInSwaggerTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Import Swagger Spec without any host");
		echo("####### Try to import an API without having the host configured at all #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore-without-any-host.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/serviceprofile/2_backend_basepath_test.json");
		createVariable("backendBasepath", "https://petstore.swagger.io");
		createVariable("apiPath", "/basepath-changed-in-swagger-test-2-${apiNumber}");
		createVariable("apiName", "Basepath changed in Swagger Test 2 ${apiNumber}");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Also for this case, the server-certificates should have been loaded #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
				.validate("$.[?(@.path=='${apiPath}')].serviceProfiles._default.basePath", "${backendBasepath}")
				.validate("$.[?(@.path=='${apiPath}')].caCerts[?(@.md5Fingerprint=='1B:73:FB:B3:57:7B:FC:8A:B4:C1:74:E3:BD:75:9B:93')].name", "@assertThat(containsString(*.swagger.io))@"));
	}
}