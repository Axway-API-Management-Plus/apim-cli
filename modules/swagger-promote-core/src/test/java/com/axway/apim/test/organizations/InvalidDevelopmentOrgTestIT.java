package com.axway.apim.test.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class InvalidDevelopmentOrgTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	public void runUnknownOrg() {
		description("Validates correct handling of invalid Owning-Organizations.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/invalid-org-${apiNumber}");
		variable("apiName", "Invalid organization ${apiNumber}");
		// Directly use an admin-account, otherwise the OrgAdmin organization is used by default
		variable("oadminUsername1", "apiadmin"); 
		variable("oadminPassword1", "changeme");
		variable("testOrgName", "Invalid organization ${orgNumber}");
		
		variable("testOrgName", "Org without permission ${apiNumber}");
		
		echo("####### Try to import an API with an invalid organization - Must be handled with a proper error-code and message #######");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/dynamic-organization.json");
		createVariable("status", "published");
		createVariable("expectedReturnCode", "57");
		action(swaggerImport);
	}
	
	@CitrusTest
	public void runNonDevOrg() {
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/invalid-org-${apiNumber}");
		variable("apiName", "Invalid organization ${apiNumber}");
		// Directly use an admin-account, otherwise the OrgAdmin organization is used by default
		variable("oadminUsername1", "apiadmin"); 
		variable("oadminPassword1", "changeme");
		variable("testOrgName", "NonDevOrg ${apiNumber}");
		
		
		http().client("apiManager")
			.send()
			.post("/organizations")
			.name("anotherOrgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"NonDevOrg ${apiNumber}\", \"description\": \"Org without dev permission\", \"enabled\": true, \"development\": false }");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "${testOrgName}")
			.extractFromPayload("$.id", "noPermOrgId");
		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/organizations/dynamic-organization.json");
		createVariable("status", "published");
		
		createVariable("expectedReturnCode", "57");
		action(swaggerImport);
		
	}
	

}
