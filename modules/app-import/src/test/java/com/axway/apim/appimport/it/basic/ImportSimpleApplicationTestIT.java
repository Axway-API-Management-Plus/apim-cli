package com.axway.apim.appimport.it.basic;

import java.io.IOException;

import org.springframework.http.HttpStatus;
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
import com.consol.citrus.message.MessageType;

@Test
public class ImportSimpleApplicationTestIT extends TestNGCitrusTestRunner {

	private ApplicationImportTestAction appImport = new ApplicationImportTestAction();
	
	private static String PACKAGE = "/com/axway/apim/appimport/apps/basic/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import application into API-Manager");
		
		variable("appNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("appName", "My-App-${appNumber}");

		echo("####### Import application: '${appName}' #######");		
		createVariable(ApplicationImportTestAction.CONFIG,  PACKAGE + "SimpleTestApplication.json");
		createVariable("expectedReturnCode", "0");
		appImport.doExecute(context);
		
		echo("####### Validate application: '${appName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications?field=name&op=eq&value=${appName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
			.extractFromPayload("$.[?(@.id=='${appName}')].id", "appId"));
		
		echo("####### Re-Import same application - Should be a No-Change #######");
		createVariable("expectedReturnCode", "10");
		appImport.doExecute(context);		
	}
}
