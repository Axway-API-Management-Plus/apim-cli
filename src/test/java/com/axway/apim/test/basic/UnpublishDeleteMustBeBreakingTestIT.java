package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test(testName="UnpublishDeleteMustBeBreakingTestIT")
public class UnpublishDeleteMustBeBreakingTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "UnpublishDeleteMustBeBreakingTestIT")
	public void setupDevOrgTest() {

		echo("####### This test makes sure, once an API is published, unpublishing or deleting it requires a force #######");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/check-is-breaking-${apiNumber}");
		variable("apiName", "Check-is-Breaking-${apiNumber}");

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' as Published #######");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("status", "published");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate unpublishing it, will fail, with the need to enforce it #######");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("status", "unpublished");
		createVariable("expectedReturnCode", "15");
		action(swaggerImport);
		
		echo("####### Validate deleting it, will fail, with the need to enforce it #######");
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("status", "deleted");
		createVariable("expectedReturnCode", "15");
		action(swaggerImport);
	}
}
