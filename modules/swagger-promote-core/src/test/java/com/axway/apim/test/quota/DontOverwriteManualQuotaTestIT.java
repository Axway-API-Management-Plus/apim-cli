package com.axway.apim.test.quota;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class DontOverwriteManualQuotaTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		
		description("Swagger-Promote Quota should only overwrite configured Quota-Information and leave manual Quota unchanged!");
		/*
		 * The Keys inside restrictions to identify an existing System- or Application-Default-Quota
		 * - the API the quota should be applied
		 * - the Method the quota should be applied
		 * - the type (throttle or throttlemb), as you may have for one API/API-Method two different types
		 * - the period, as you may want to configured a overall quota for a day and for one hour
		 * - the per - You may want to configured 1 quota per 1 hour and another for 8 hours
		 * 
		 * If all these keys are the same, the quota-setting is the same and should be overwritten!
		 */

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/dont-overwrite-quota-restriction-api-${apiNumber}");
		variable("apiName", "Quota-${apiNumber}-Multi-Restriction-API");
		
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
		
		echo("####### Replicate the same API with some Quotas configured, which are different to the one manually defined before #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("applicationPeriod", "day");
		createVariable("applicationMb", "555");
		createVariable("systemPeriod", "week");
		createVariable("systemMessages", "666");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate all APPLICATION quotas (manually configured & API-Config) do exists #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/"+APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].config.per", "1")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId1}')].config.mb", "700")
			
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].config.per", "2")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId2}')].config.messages", "100000")
			
			// These quota settings are inserted by Swagger-Promote based on configuration
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.mb", "555")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.period", "day")
			.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttlemb')].config.per", "1"));
		
		echo("####### Validate all SYSTEM quotas (manually configured & API-Config) do exists #######");
		http(builder -> builder.client("apiManager").send().get("/quotas/"+APIManagerAdapter.SYSTEM_API_QUOTA).header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].type", "throttle")
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].config.period", "hour")
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].config.per", "3")
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId3}')].config.messages", "1003")
				
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].type", "throttlemb")
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].config.period", "day")
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].config.per", "4")
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='${testMethodId4}')].config.mb", "500")
				
				// These quota settings are inserted based on configuration by Swagger-Promote 
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.messages", "666")
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.period", "week")
				.validate("$.restrictions.[?(@.api=='${apiId}' && @.method=='*'&& @.type=='throttle')].config.per", "2"));
	}
}
