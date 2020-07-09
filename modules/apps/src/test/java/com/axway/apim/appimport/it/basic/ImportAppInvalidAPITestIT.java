package com.axway.apim.appimport.it.basic;

import java.io.IOException;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.appimport.ApplicationImportTestAction;
import com.axway.apim.lib.errorHandling.AppException;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;

@Test
public class ImportAppInvalidAPITestIT extends TestNGCitrusTestRunner {

	private ApplicationImportTestAction appImport = new ApplicationImportTestAction();
	
	private static String PACKAGE = "/com/axway/apim/appimport/apps/basic/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void importApplicationBasicTest(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Trying to import an application that requests access to an unknown API");
		
		variable("appNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("appName", "Complete-App-${appNumber}");
		variable("apiName1", "This-API-is-unkown");

		echo("####### Import application: '${appName}' with access to ONE UNKNOWN API #######");
		createVariable(ApplicationImportTestAction.CONFIG,  PACKAGE + "AppWithAPIAccess.json");
		createVariable("expectedReturnCode", "56");
		appImport.doExecute(context);
	}
}
