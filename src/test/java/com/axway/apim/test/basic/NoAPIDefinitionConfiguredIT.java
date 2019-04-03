package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.axway.apim.lib.ErrorCode;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test(testName="NoAPIDefinitionConfiguredIT")
public class NoAPIDefinitionConfiguredIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "NoAPIDefinitionConfiguredIT")
	public void run() {
		description("If no api-definition is passed as argument and no apiDefinition attribute is found in contract file, the tool must fail with a dedicated return code.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-no-api-def-${apiNumber}");
		variable("apiName", "No-API-DEF-CONFIGURED-${apiNumber}");

		echo("####### Calling the tool with a Non-Admin-User. #######");
		createVariable(ImportTestAction.API_DEFINITION,  "");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/minimal-config.json");
		createVariable("status", "unpublished");
		createVariable("expectedReturnCode", String.valueOf(ErrorCode.CANT_READ_CONFIG_FILE.getCode()));
		action(swaggerImport);
	}

}
