package com.axway.apim.test.quota;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="APIBasicQuotaTest")
public class APIBasicQuotaTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "APIBasicQuotaTest")
	public void run() {
		description("Import an API containing a quota definition");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/quota-api-${apiNumber}");
		variable("apiName", "Quota-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		createVariable("applicationPeriod", "hour");
		createVariable("systemPeriod", "days");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has a been imported #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Check System-Quotas have been setup as configured #######");
		http().client("apiManager")
			.send()
			.get("/quotas/00000000-0000-0000-0000-000000000000")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "666")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "days")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "2");
		
		echo("####### Check Application-Quotas have been setup as configured #######");
		http().client("apiManager")
			.send()
			.get("/quotas/00000000-0000-0000-0000-000000000001")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.mb", "555")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "hour")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "1");
		
		echo("####### Executing a Quota-No-Change import #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("applicationPeriod", "hour");
		createVariable("systemPeriod", "days");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
		
		echo("####### Perform a change in System-Default-Quota #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("applicationPeriod", "hour"); // This one stays!
		createVariable("systemPeriod", "weeks");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Check System-Quotas have been updated #######");
		http().client("apiManager")
			.send()
			.get("/quotas/00000000-0000-0000-0000-000000000000")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "666")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "weeks")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "2");
		
		echo("####### Perform a change in Application-Default-Quota #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "published");
		createVariable("applicationPeriod", "seconds"); 
		createVariable("systemPeriod", "weeks");// Now, this one stays!
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Check Application-Quotas have been updated #######");
		http().client("apiManager")
			.send()
			.get("/quotas/00000000-0000-0000-0000-000000000001")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.mb", "555")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "seconds")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "1");
		
		echo("####### Make sure, the System-Quota stays unchanged with the last update #######");
		http().client("apiManager")
			.send()
			.get("/quotas/00000000-0000-0000-0000-000000000000")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${apiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${apiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.messages", "666")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.period", "weeks")
			.validate("$.restrictions.[?(@.api=='${apiId}')].config.per", "2");
		
		echo("####### Perform a breaking change, making sure, that defined Quotas persist #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "published");
		createVariable("applicationPeriod", "seconds"); 
		createVariable("enforce", "true");
		createVariable("systemPeriod", "weeks");// Now, this one stays!
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been re-imported with a new API-ID #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "newApiId");
		
		echo("####### Check System-Quotas have been setup as configured for the new API #######");
		http().client("apiManager")
			.send()
			.get("/quotas/00000000-0000-0000-0000-000000000000")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${newApiId}')].type", "throttle")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.messages", "666")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "weeks")
			.validate("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.per", "2");
		
		echo("####### Check Application-Quotas have been setup as configured for the new API  #######");
		http().client("apiManager")
			.send()
			.get("/quotas/00000000-0000-0000-0000-000000000001")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.restrictions.[?(@.api=='${newApiId}')].type", "throttlemb")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].method", "*")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.mb", "555")
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.period", "seconds")
			.validate("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@") // Make sure, the old API-ID has been removed
			.validate("$.restrictions.[?(@.api=='${newApiId}')].config.per", "1");
		
	}
}
