package com.axway.apim.test.methodLevel;

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
public class OutboundMethodLevelTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Validate Outbound Method level settings are applied");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/basic-outbound-method-level-api-${apiNumber}");
		variable("apiName", "Basic Outbound Method-Level-API-${apiNumber}");
		
		echo("####### Try to replicate an API having Outbound Method-Level settings declared #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-outboundbound-api-key.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		createVariable("outboundProfileName", "HTTP Basic outbound Test ${apiNumber}");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the FE-API has been configured with outbound HTTP-Basic on method level #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.validate("$.[?(@.path=='/basic-outbound-method-level-api-930')].authenticationProfiles[?(@.name=='HTTP Basic outbound Test 930')].type", "http_basic")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}/operations").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.extractFromPayload("$.[?(@.name=='getOrderById')].id", "apiMethodId"));
		
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].outboundProfiles.${apiMethodId}.authenticationProfile", "${outboundProfileName}"));
		
		echo("####### Perform a No-Change #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-outboundbound-api-key.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "10");
		createVariable("outboundProfileName", "HTTP Basic outbound Test ${apiNumber}");
		swaggerImport.doExecute(context);
	}
}
