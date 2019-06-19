package com.axway.apim.test.quota;

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
public class ReCreateAPIQuotaStaysTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Validate the use-case described in issue #86 works as expected.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/recreate-with-app-quota-${apiNumber}");
		variable("apiName", "Recreate-with-App-Quota-${apiNumber}");

		echo("####### Create an API as given in the issue #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/issue-86-api-with-app-quota.json");
		createVariable("state", "published");
		createVariable("image", "/com/axway/apim/test/files/basic/API-Logo.jpg");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").name("api").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		createVariable("appName", "Recreate-with-App-Quota ${apiNumber}");
		echo("####### Create an application: '${appName}', used to subscribe to that API #######");
		http(builder -> builder.client("apiManager").send().post("/applications").header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "testAppId")
			.extractFromPayload("$.name", "testAppName"));
		
		echo("####### Grant access to org2 for this API  #######");
		http(builder -> builder.client("apiManager").send().post("/proxies/grantaccess").header("Content-Type", "application/x-www-form-urlencoded")
				.payload("action=orgs&apiId=${apiId}&grantOrgId=${orgId2}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.NO_CONTENT));
		
		echo("####### Subscribe App to the API #######");
		http(builder -> builder.client("apiManager").send().post("/applications/${testAppId}/apis").header("Content-Type", "application/json")
				.payload("{\"apiId\":\"${apiId}\",\"enabled\":true}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED));
		
		echo("####### Configure an Application specfic quota override for this APP, which must be taken over when re-creating this API #######");
		http(builder -> builder.client("apiManager").send().post("/applications/${testAppId}/quota").header("Content-Type", "application/json")
				.payload("{\"type\":\"APPLICATION\",\"name\":\"Recreate-with-App-Quota 758 Quota\",\"restrictions\":[{\"api\":\"${apiId}\",\"method\":\"*\",\"type\":\"throttle\",\"config\":{\"period\":\"hours\",\"messages\":600,\"per\":4}}]}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED));
		
		echo("####### For a Re-Creation of the API which fails according to the issue 86 #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/issue-86-api-with-app-quota.json");
		createVariable("state", "published");
		createVariable("image", "/com/axway/apim/test/files/basic/API-Logo.jpg");
		createVariable("enforce", "true");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate the new APIs has been created #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"));

		echo("####### Validate the application DEFAULT quota is set for the API as before #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000001").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "25")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "seconds")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.per", "60"));
		
		echo("####### Validate the application SPECIFIC quota override is set for the API as before #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${testAppId}/quota").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "600")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "hours")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.per", "4"));
	}
}
