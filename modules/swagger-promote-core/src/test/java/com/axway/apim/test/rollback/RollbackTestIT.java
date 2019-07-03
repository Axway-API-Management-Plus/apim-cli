package com.axway.apim.test.rollback;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.test.ImportTestAction;
import com.axway.apim.test.lib.APIManagerConfig;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class RollbackTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Making sure, items are completely rolled back in case of an error.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/rollback-api-${apiNumber}");
		variable("apiName", "Rollback-API-${apiNumber}");
		
		echo("####### Try to replicate APIs, that will fail #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/rollback/invalid-organization.json");
		createVariable("status", "published");
		createVariable("expectedReturnCode", "57"); // Must fail!
		swaggerImport.doExecute(context);
		
		echo("####### Validate the temp. FE-API has been rolled back #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.path", "@assertThat(not(containsString(${apiPath})))@"));
		
		echo("####### Validate the temp. BE-API has been rolled back #######");
		http(builder -> builder.client("apiManager").send().get("/apirepo").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.*.name", "@assertThat(not(containsString(${apiName})))@"));
	}
}
