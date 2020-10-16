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
public class NoChangeAPITestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Import an API and re-import it without any change. It must be detected, that no change happened.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-no-change-${apiNumber}");
		variable("apiName", "No-Change-${apiNumber}");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with an unknown RemoteHost #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/1_no-change-config.json");
		createVariable("expectedReturnCode", "63"); // Must fail, as the RemoteHost is unknown
		createVariable("remoteHostName", "my.host-${apiNumber}.com");
		createVariable("remoteHostPort", "8786");
		swaggerImport.doExecute(context);
		
		echo("####### Creating remote host ${remoteHostName}:${remoteHostPort} #######");
		http(builder -> builder.client("apiManager").send().post("/remotehosts").header("Content-Type", "application/json")
				.payload("{\"name\":\"${remoteHostName}\",\"port\":\"${remoteHostPort}\",\"organizationId\":\"${orgId}\"}"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON));
		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' incl. a RemoteHost #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/1_no-change-config.json");
		createVariable("expectedReturnCode", "0"); // Must fail, as the RemoteHost is unknown
		swaggerImport.doExecute(context);

		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));

		echo("####### RE-Importing same API: '${apiName}' on path: '${apiPath}' without changes. Expecting failure with RC 99. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/1_no-change-config.json");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
		
		echo("####### Make sure, the API-ID hasn't changed #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		// Check the API is still exposed on the same path
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].id", "${apiId}")); // Must be the same API-ID as before!
	}

}
