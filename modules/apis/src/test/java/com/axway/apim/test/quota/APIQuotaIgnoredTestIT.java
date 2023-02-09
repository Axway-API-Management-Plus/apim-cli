package com.axway.apim.test.quota;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

@Test(testName="APIQuotaIgnoredTestIT")
public class APIQuotaIgnoredTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "APIQuotaIgnoredTestIT")
	public void run() {
		description("Import an API containing a quota definition, but it should be ignored-");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/ignored-quota-api-${apiNumber}");
		variable("apiName", "Ignored-Quota-API-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/quota/1_api-with-quota.json");
		createVariable("state", "unpublished");
		createVariable("expectedReturnCode", "0");
		createVariable("applicationPeriod", "hour");
		createVariable("applicationMb", "111111");
		createVariable("systemPeriod", "day");
		createVariable("systemMessages", "2222");
		createVariable("ignoreQuotas", "true");
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
		
		echo("####### Check that the API-ID doesn't exists in the System-Quotas #######");
		http().client("apiManager").send().get("/quotas/"+ APIManagerAdapter.SYSTEM_API_QUOTA).header("Content-Type", "application/json");
		
		http().client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@");
		
		echo("####### Check that the API-ID doesn't exists in the Application-Quotas #######");
		http().client("apiManager").send().get("/quotas/"+ APIManagerAdapter.APPLICATION_DEFAULT_QUOTA).header("Content-Type", "application/json");

		http().client("apiManager")	.receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.restrictions[*].api", "@assertThat(not(containsString(${apiId})))@");
	}
}
