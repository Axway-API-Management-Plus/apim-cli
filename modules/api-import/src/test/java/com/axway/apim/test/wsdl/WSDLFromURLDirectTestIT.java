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
public class WSDLFromURLDirectTestIT extends TestNGCitrusTestRunner {
	
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Validates a WSDL-File can be taken from a URL using the direct instruction.");
    		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/direct-url-wsdl-${apiNumber}");
		variable("apiName", "Direct-URL-WSDL from URL-${apiNumber}");
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time from URL #######");
		createVariable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
		createVariable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/wsdl/wsdl-minimal-config.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Re-Import API from URL without a change #######");
		createVariable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
		createVariable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/wsdl/wsdl-minimal-config.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
		
		echo("####### Setting the status to Published #######");
		createVariable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
		createVariable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/wsdl/wsdl-minimal-config.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published"));
		
		echo("####### Now performing a change, which required to Re-Create the API #######");
		createVariable(ImportTestAction.API_DEFINITION, "http://www.mnb.hu/arfolyamok.asmx?WSDL");
		createVariable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/wsdl/wsdl-minimal-config-with-tags.json");
		createVariable("status", "published");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

	}
}
