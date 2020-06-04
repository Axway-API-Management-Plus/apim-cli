package com.axway.apim.test.orgadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="OrgAdminUnpublishedTestIT")
public class OrgAdminUnpublishedTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "OrgAdminUnpublishedTestIT")
	public void run() {
		description("OrgAdmins should be able to register unpublished APIs and manage them.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/org-admin-${apiNumber}");
		variable("apiName", "OrgAdmin-Unpublished-${apiNumber}");
		variable("ignoreAdminAccount", "true"); // This tests simulate to use only an Org-Admin-Account

		echo("####### Calling the tool with a Non-Admin-User. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/1_no-change-config.json");
		createVariable("expectedReturnCode", "0");
		createVariable("apiManagerUser", "${oadminUsername1}"); // This is an org-admin user
		createVariable("apiManagerPass", "${oadminPassword1}");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported using OrgAdmin only #######");
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
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Re-Import another Swagger-File still unpublished. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore2.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/1_no-change-config.json");
		createVariable("expectedReturnCode", "0");
		createVariable("apiManagerUser", "${oadminUsername1}"); // This is an org-admin user
		createVariable("apiManagerPass", "${oadminPassword1}");
		action(swaggerImport);
	}

}
