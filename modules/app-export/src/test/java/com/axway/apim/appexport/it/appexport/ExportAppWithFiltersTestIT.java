package com.axway.apim.appexport.it.appexport;

import java.io.IOException;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.appexport.ApplicationExportTestAction;
import com.axway.apim.appimport.ApplicationImportTestAction;
import com.axway.apim.appimport.adapter.jackson.AppCredentialsDeserializer;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Test
public class ExportAppWithFiltersTestIT extends TestNGCitrusTestRunner {
	
	private ObjectMapper mapper = new ObjectMapper();

	private ApplicationImportTestAction appImport = new ApplicationImportTestAction();
	private ApplicationExportTestAction appExport = new ApplicationExportTestAction();
	
	private static String PACKAGE = "/com/axway/apim/appexport/apps/basic/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void tryToExportWithInvalidOrgName(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Trying to export an application belonging to an invalid organization");
		
		variable("targetFolder", "citrus:systemProperty('java.io.tmpdir')");
		mapper.registerModule(new SimpleModule().addDeserializer(ClientAppCredential.class, new AppCredentialsDeserializer()));
		
		echo("####### Try to export an application with an invalid orgName #######");
		createVariable("expectedReturnCode", ""+ErrorCode.UNKNOWN_ORGANIZATION.getCode());
		createVariable("orgNameFilter", "ThisOrgDoesnExists");
		createVariable("appName", "NotRelevantForThisTest");
		
		appExport.doExecute(context);
	}
	
	@CitrusTest
	@Test @Parameters("context")
	public void pendingApplicationTest(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Export complete application from API-Manager tests");
		
		variable("targetFolder", "citrus:systemProperty('java.io.tmpdir')");
		mapper.registerModule(new SimpleModule().addDeserializer(ClientAppCredential.class, new AppCredentialsDeserializer()));
		
		variable("appNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("appName", "Pending-App-for-export-${appNumber}");
		variable("appStatus", "pending");
		
		echo("####### Import pending application: '${appName}' to be exported afterwards #######");		
		createVariable(ApplicationImportTestAction.CONFIG,  PACKAGE + "BasicAppWithStatus.json");
		createVariable("expectedReturnCode", "0");
		appImport.doExecute(context);
		
		echo("####### Export the pending application that has been imported before #######");
		createVariable("expectedReturnCode", "0");
		appExport.doExecute(context);
	}
}
