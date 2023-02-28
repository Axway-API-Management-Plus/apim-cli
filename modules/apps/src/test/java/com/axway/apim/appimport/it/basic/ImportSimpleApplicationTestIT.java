package com.axway.apim.appimport.it.basic;

import java.io.IOException;

import com.axway.apim.testActions.TestParams;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.appimport.it.ImportAppTestAction;
import com.axway.apim.lib.error.AppException;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class ImportSimpleApplicationTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private static String PACKAGE = "/com/axway/apim/appimport/apps/basic/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import application into API-Manager");
		
		ImportAppTestAction importApp = new ImportAppTestAction(context);
		
		variable("appName", "My-App-"+importApp.getRandomNum());

		echo("####### Import application: '${appName}' #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "SimpleTestApplication.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications?field=name&op=eq&value=${appName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
			.extractFromPayload("$.[?(@.id=='${appName}')].id", "appId"));
		
		echo("####### Re-Import same application - Should be a No-Change #######");
		createVariable(PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);		
	}
	
	@CitrusTest
	@Test @Parameters("context")
	public void importAsAdmin(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import application into API-Manager using an admin account");
		
		ImportAppTestAction importApp = new ImportAppTestAction(context);
		
		variable("appName", "My-Admin-App-"+importApp.getRandomNum());
		// Directly use an admin-account, otherwise the OrgAdmin organization is used by default
		createVariable(PARAM_OADMIN_USERNAME, "apiadmin"); 
		createVariable(PARAM_OADMIN_PASSWORD, "changeme");

		echo("####### Import application: '${appName}' #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "SimpleTestApplication.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications?field=name&op=eq&value=${appName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
			.extractFromPayload("$.[?(@.id=='${appName}')].id", "appId"));
		
		echo("####### Re-Import same application - Should be a No-Change #######");
		createVariable(PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);		
	}
	
	@CitrusTest
	@Test @Parameters("context")
	public void importDisabledApplication(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import application into API-Manager which is disabled");
		
		ImportAppTestAction importApp = new ImportAppTestAction(context);
		
		variable("appNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("appName", "Disabled-App-"+importApp.getRandomNum());

		echo("####### Import application: '${appName}' #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "DisabledApplication.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate disabled application: '${appName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications?field=name&op=eq&value=${appName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
			.validate("$.[?(@.name=='${appName}')].enabled", "false")
			.extractFromPayload("$.[?(@.id=='${appName}')].id", "appId"));
		
		echo("####### Re-Import same application - Should be a No-Change #######");
		createVariable(PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);
	}
}
