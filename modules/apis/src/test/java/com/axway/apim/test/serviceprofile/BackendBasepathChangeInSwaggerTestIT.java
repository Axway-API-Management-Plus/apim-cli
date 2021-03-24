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
		description("Test for issue #81 adjusting the backendBasePath in the Swagger-File during import. This makes sure, SSL-Certificates can be downloaded automatically.");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/basepath-changed-in-swagger-test-${apiNumber}");
		variable("apiName", "Basepath changed in Swagger Test ${apiNumber}");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore-with-invalid-host.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/serviceprofile/2_backend_basepath_test.json");
		createVariable("backendBasepath", "https://petstore.swagger.io");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

		echo("####### Validate the server-certificates could be loaded - even if the host was given wrong in the Swagger-File #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
				.validate("$.[?(@.path=='${apiPath}')].serviceProfiles._default.basePath", "${backendBasepath}")
				.validate("$.[?(@.path=='${apiPath}')].caCerts[?(@.sha1Fingerprint=='85:58:3C:71:FD:02:C0:E7:6B:60:4A:D5:DD:55:52:A9:28:DB:43:66')].name", "@assertThat(containsString(*.swagger.io))@")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Perform a no change to make sure, the API-Definition is not considered as changed because we change the host on-the-fly #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore-with-invalid-host.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/serviceprofile/2_backend_basepath_test.json");
		createVariable("backendBasepath", "https://petstore.swagger.io");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
		
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
				.validate("$.[?(@.path=='${apiPath}')].caCerts[?(@.md5Fingerprint=='81:AD:AF:05:B1:39:ED:FF:BE:EE:79:94:28:B3:F2:10')].name", "@assertThat(containsString(*.swagger.io))@"));
	}
}
