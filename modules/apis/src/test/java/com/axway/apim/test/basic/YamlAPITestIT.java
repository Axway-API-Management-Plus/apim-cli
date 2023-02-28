package com.axway.apim.test.basic;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class YamlAPITestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Import a YAML API and re-import it without any change. It must be detected, that no change happened.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/yaml-my-no-change-${apiNumber}");
		variable("apiName", "YAML No-Change-${apiNumber}");
		
		if(APIManagerAdapter.hasAPIManagerVersion("7.7")) {

			echo("####### Importing YAML API: '${apiName}' on path: '${apiPath}' for the first time #######");
			createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/yaml-petstore.yaml");
			createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config.json");
			createVariable("state", "unpublished");
			createVariable("expectedReturnCode", "0");
			swaggerImport.doExecute(context);
	
			echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
			http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
	
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
	
			echo("####### RE-Importing same API: '${apiName}' on path: '${apiPath}' without changes. Expecting failure with RC 99. #######");
			createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/yaml-petstore.yaml");
			createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config.json");
			createVariable("state", "unpublished");
			createVariable("expectedReturnCode", "10");
			swaggerImport.doExecute(context);
			
			echo("####### Make sure, the API-ID hasn't changed #######");
			http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
	
			// Check the API is still exposed on the same path
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
				.validate("$.[?(@.path=='${apiPath}')].id", "${apiId}")); // Must be the same API-ID as before!

		} else {
			echo("####### Importing YAML API: '${apiName}' on path: '${apiPath}' for the first time #######");
			createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/yaml-petstore.yaml");
			createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config.json");
			createVariable("state", "unpublished");
			createVariable("expectedReturnCode", "75"); // Not supported in previous versions
			swaggerImport.doExecute(context);
		}
	}

}
