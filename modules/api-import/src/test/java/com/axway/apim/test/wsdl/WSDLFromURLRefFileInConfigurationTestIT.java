package com.axway.apim.test.wsdl;

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
public class WSDLFromURLRefFileInConfigurationTestIT extends TestNGCitrusTestRunner {
	
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Validates a WSDL-File can be taken from a URL using a REF-File described in API json configuration");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/ref-file-wsdl-in-configuration-${apiNumber}");
		variable("apiName", "Ref-File-WSDL in configuration from URL-${apiNumber}");
		

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time from URL #######");
		createVariable(ImportTestAction.API_DEFINITION, "");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
		createVariable("testAPIDefinition","./src/test/resources/com/axway/apim/test/files/wsdl/wsdl-file-with-username.url");
		createVariable("status", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Re-Import API from URL without a change #######");
		createVariable(ImportTestAction.API_DEFINITION, "");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config-with-api-definition.json");
		createVariable("testAPIDefinition","./src/test/resources/com/axway/apim/test/files/wsdl/wsdl-file-with-username.url");
		createVariable("status", "unpublished");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
	}
}
