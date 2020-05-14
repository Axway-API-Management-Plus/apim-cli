package com.axway.apim.test.basic;

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
public class SpecialCharactersAPITestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Import an API having some special characters in the Swagger & API-Config-File.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/special-chars-${apiNumber}");
		variable("apiName", "Special-Chars-${apiNumber}");

		echo("####### Importing Special-Chars API: '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore-special-chars.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/special-chars-config.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			// TODO: Find a way to validate unicode characters as well
			//.validate("$.[?(@.path=='${apiPath}')].summary", "Ã�ï¿½Ã�Â´Ã�Â¿Ã�Â°Ã‘â€š Ã�Â¸Ã�Â»Ã�Â¸ Ã‘Æ’Ã�Â¼Ã‘â‚¬Ã�Â¸.")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));

		echo("####### RE-Importing same API: '${apiName}' on path: '${apiPath}' without changes. Expecting failure with RC 10. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore-special-chars.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/special-chars-config.json");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
	}

}
