package com.axway.apim.test.quota;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class QuotaModeReplaceTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException, InterruptedException {
		swaggerImport = new ImportTestAction();
		description("If the Quota-Mode is set to replace, evtl. existing quotas should be replaced.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/quota-replace-api-${apiNumber}");
		variable("apiName", "Quota-${apiNumber}-Replace-API");
		variable("quotaMode", "replace");
		
		
		echo("####### Import a very basic API without any quota #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		echo("####### API: '${apiName}' on path: '${apiPath}' with ID: '${apiId}' imported #######");
		
		echo("####### Get the operations/methods for the created API #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}/operations").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.extractFromPayload("$.[?(@.name=='updatePetWithForm')].id", "testMethodId1")
				.extractFromPayload("$.[?(@.name=='findPetsByStatus')].id", "testMethodId2")
				.extractFromPayload("$.[?(@.name=='getPetById')].id", "testMethodId3")
				.extractFromPayload("$.[?(@.name=='updateUser')].id", "testMethodId4"));
		
		echo("####### Define a manual application- and system-quota for the API: ${apiId} #######"); 
		http(builder -> builder.client("apiManager").send().put("/quotas/"+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).header("Content-Type", "application/json")
		.payload("{\"id\":\""+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA+"\", \"type\":\"APPLICATION\",\"name\":\"Application Default\","
				+ "\"description\":\"Maximum message rates per application. Applied to each application unless an Application-Specific quota is configured\","
				+ "\"restrictions\":["
					+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId1}\",\"type\":\"throttlemb\",\"config\":{\"period\":\"hour\",\"per\":1,\"mb\":700}}, "
					+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId2}\",\"type\":\"throttle\",\"config\":{\"period\":\"day\",\"per\":2,\"messages\":100000}} "
				+ "],"
				+ "\"system\":true}"));
		
		http(builder -> builder.client("apiManager").send().put("/quotas/"+APIManagerAdapter.SYSTEM_API_QUOTA).header("Content-Type", "application/json")
		.payload("{\"id\":\""+APIManagerAdapter.SYSTEM_API_QUOTA+"\", \"type\":\"API\",\"name\":\"System\","
				+ "\"description\":\"Maximum message rates aggregated across all client applications\","
				+ "\"restrictions\":["
					+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId3}\",\"type\":\"throttle\",\"config\":{\"period\":\"hour\",\"per\":3,\"messages\":1003}}, "
					+ "{\"api\":\"${apiId}\",\"method\":\"${testMethodId4}\",\"type\":\"throttlemb\",\"config\":{\"period\":\"day\",\"per\":4,\"mb\":500}} "
				+ "],"
				+ "\"system\":true}"));

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with Quotas configured that must replace existing quotas #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		createVariable("systemPeriod", "day");
		createVariable("systemMessages", "666");
		createVariable("applicationPeriod", "hour");
		createVariable("applicationMb", "555");
		swaggerImport.doExecute(context);
		
		echo("####### Check Application-Quotas have been setup as configured #######");
		if(APIManagerAdapter.hasAPIManagerVersion("7.7.20200130")) {
			echo("####### ############ Sleep 5 seconds ##################### #######");
			Thread.sleep(5000); // Starting with this version, we need to wait a few milliseconds, otherwise the REST-API doesn't return the complete set of quotas
		}
		http(builder -> builder.client("apiManager").send().get("/quotas/"+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			// The method specific quotas must be have been removed
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].type", "@assertThat(empty())@")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].type", "@assertThat(empty())@")
			
			// These quota settings are inserted based on configuration by Swagger-Promote 
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.mb", "555")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.per", "1"));
		
		echo("####### Check that only the configured quota remains and previouls configured are removed #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/"+APIManagerAdapter.SYSTEM_API_QUOTA).header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			// The method specific quotas must be have been removed
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].type", "@assertThat(empty())@")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].type", "@assertThat(empty())@")

			// These quota settings are inserted by Swagger-Promote based on configuration
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.messages", "666")
			//.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.per", "2"));
		
		echo("####### Executing a Quota-No-Change import #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("applicationMb", "555");
		createVariable("systemMessages", "666");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
		
		echo("####### Perform a change in System-Default-Quota #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("applicationMb", "555");
		createVariable("systemMessages", "888");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Check System-Quotas have been updated #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/00000000-0000-0000-0000-000000000000").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "888")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "2"));
		
		echo("####### Perform a change in Application-Default-Quota #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "published");
		createVariable("applicationMb", "777");
		createVariable("systemMessages", "888");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Check Application-Quotas have been updated #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/"+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.mb", "777")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "1"));
		
		echo("####### Make sure, the System-Quota stays unchanged with the last update #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/"+APIManagerAdapter.SYSTEM_API_QUOTA).header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "888")
			//.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "2"));
		
		echo("####### Perform a breaking change, making sure, that defined Quotas persist #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "published"); 
		createVariable("enforce", "true");
		createVariable("systemMessages", "999");
		createVariable("applicationMb", "1888");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' has been re-imported with a new API-ID #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId"));
		
		echo("####### Check System-Quotas have been setup as configured for the new API #######");
		echo("####### ############ Sleep 5 seconds ##################### #######");
		Thread.sleep(5000);
		http(builder -> builder.client("apiManager").send().get("/quotas/"+APIManagerAdapter.SYSTEM_API_QUOTA).header("Content-Type", "application/json"));
		Thread.sleep(5000);
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "999")
			//.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "day")
			.validate("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.per", "2"));
		
		echo("####### Check Application-Quotas have been setup as configured for the new API  #######");
		echo("####### ############ Sleep 5 seconds ##################### #######");
		Thread.sleep(5000);
		http(builder -> builder.client("apiManager").send().get("/quotas/"+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).header("Content-Type", "application/json"));
		Thread.sleep(5000);
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${newApiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.mb", "1888")
			//.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "hour")
			.validate("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.per", "1"));
		
	}
}
