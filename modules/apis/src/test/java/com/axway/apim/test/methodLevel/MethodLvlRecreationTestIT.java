package com.axway.apim.test.methodLevel;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class MethodLvlRecreationTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Make sure, Method-Level config is working on API-Recreation. See issue: #119");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/method-lvl-recreation-${apiNumber}");
		variable("apiName", "Method-Level-Recreation-API-${apiNumber}");
		
		echo("####### Try to replicate an API having Method-Level settings declared #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-inbound-api-key.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		createVariable("securityProfileName", "APIKeyBased${apiNumber}");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the FE-API has been configured with API-Key on method level #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles.[?(@.name=='${securityProfileName}')].devices[0].type", "apiKey")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}/operations").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.extractFromPayload("$.[?(@.name=='findPetsByStatus')].id", "apiMethodId"));
		
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].inboundProfiles.${apiMethodId}.securityProfile", "${securityProfileName}"));
		
		echo("####### Force a Re-Creation of the API with the same Method-Level-Settings #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-inbound-api-key.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		createVariable("securityProfileName", "APIKeyBased${apiNumber}");
		swaggerImport.doExecute(context);
		
		echo("####### Make sure, the Method-Level settings are still in place #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles.[?(@.name=='${securityProfileName}')].devices[0].type", "apiKey")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}/operations").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.extractFromPayload("$.[?(@.name=='findPetsByStatus')].id", "apiMethodId"));
		
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].inboundProfiles.${apiMethodId}.securityProfile", "${securityProfileName}"));
		/*
		
		echo("####### Execute a No-Change test #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-inbound-api-key-and-cors.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		createVariable("securityProfileName", "APIKeyBased${apiNumber}");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the FE-API has been configured with API-Key on method level #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "${state}")
			.validate("$.[?(@.id=='${apiId}')].securityProfiles.[?(@.name=='${securityProfileName}')].devices[0].type", "apiKey")
			.validate("$.[?(@.id=='${apiId}')].corsProfiles.[0].name", "New CORS Profile")
			.validate("$.[?(@.id=='${apiId}')].inboundProfiles.${apiMethodId}.securityProfile", "${securityProfileName}")
			.validate("$.[?(@.id=='${apiId}')].inboundProfiles.${apiMethodId}.corsProfile", "New CORS Profile"));*/
	}
}
