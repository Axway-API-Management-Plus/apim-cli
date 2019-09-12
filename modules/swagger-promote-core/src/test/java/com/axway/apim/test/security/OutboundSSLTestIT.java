package com.axway.apim.test.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class OutboundSSLTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Test-Case to validate Outbound SSL authentication");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/outbound-ssl-${apiNumber}");
		variable("apiName", "Outbound-SSL-${apiNumber}");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/api_outbound-ssl.json");
		createVariable("state", "unpublished");
		createVariable("certFile", "/com/axway/apim/test/files/certificates/clientcert.pfx");
		createVariable("password", "axway");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].name", "_default")
			.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].type", "ssl")
			.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.password", "${password}")
			.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.trustAll", true)
			.validate("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.pfx", "@assertThat(startsWith(data:application/x-pkcs12;base64,MIIJ0QIBAzCCCZcGCSqGSIb3DQEHAaCCCYgEggmEMIIJg))@")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].authenticationProfiles[0].parameters.trustAll", "trustAll")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Execute a No-Change test #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/api_outbound-ssl.json");
		createVariable("state", "unpublished");
		createVariable("password", "axway");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
		
		echo("####### Provide the wrong password to the keystore #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/api_outbound-ssl.json");
		createVariable("state", "unpublished");
		createVariable("password", "wrongpassword");
		createVariable("expectedReturnCode", "81");
		swaggerImport.doExecute(context);
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/api_outbound-ssl.json");
		createVariable("state", "unpublished");
		createVariable("certFile", "/com/axway/apim/test/files/certificates/clientcert2.p12");
		createVariable("password", "axway2");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the Client-Certificate has been updated #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").name("api").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
				.validate("$.[?(@.id=='${apiId}')].state", "${state}")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].name", "_default")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].type", "ssl")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].parameters.password", "${password}")
				.validate("$.[?(@.id=='${apiId}')].authenticationProfiles[0].parameters.pfx", "@assertThat(startsWith(data:application/x-pkcs12;base64,MIIJ2QIBAzCCCZ8GCSqGSIb3DQEHAaCCCZAEggmMMIIJiDCCBD8GCSqGSIb3DQEHBqCCBDAwggQs))@"));
	}
}
