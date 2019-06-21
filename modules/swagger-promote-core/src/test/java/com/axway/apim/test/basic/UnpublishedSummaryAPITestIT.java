package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="UnpublishedSummaryAPITest")
public class UnpublishedSummaryAPITestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "UnpublishedNewSummaryAPITest")
	public void run() {
		description("Make sure, the summary gets updated");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-summary-test-${apiNumber}");
		variable("apiName", "My-Summary-test-${apiNumber}");
		

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/3_4_unpublished-dynamic-summary-api.json");
		createVariable("expectedReturnCode", "0");
		createVariable("apiSummary", "My great summary 1!");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
		echo("####### Expected API-Summary: ${apiSummary} #######");
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
			.validate("$.[?(@.path=='${apiPath}')].summary", "${apiSummary}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Change the API-Summary, stay in the same status #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/3_4_unpublished-dynamic-summary-api.json");
		createVariable("expectedReturnCode", "0");
		createVariable("apiSummary", "Another great summary!!!");
		action(swaggerImport);
		
		echo("####### Validate the summary has been changed, which is not a breaking change #######");
		echo("####### API-Summary should now be changed to: ${apiSummary} #######");
		http().client("apiManager")
			.send()
			.get("/proxies/${apiId}")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "unpublished")
			.validate("$.[?(@.id=='${apiId}')].summary", "${apiSummary}");
		
		echo("####### Change the API back to a state without a summary #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/3_5_unpublished-no-summary-api.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate the summary has been REMOVED, which is not a breaking change #######");
		http().client("apiManager")
			.send()
			.get("/proxies/${apiId}")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "unpublished")
			.validate("$.[?(@.id=='${apiId}')].summary", null);
	}

}
