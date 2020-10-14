package com.axway.apim.setup.config.it.tests;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.setup.config.it.ImportConfigTestAction;
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;

@Test
public class ImportAndExportConfigTestIT extends TestNGCitrusTestRunner implements TestParams {

	private ImportConfigTestAction configImport = new ImportConfigTestAction();
	
	private static String PACKAGE = "/com/axway/apim/setup/config/it/tests/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void runConfigImportAndExport(@Optional @CitrusResource TestContext context) throws AppException {
		description("Import configuration into API-Manager");

		echo("####### Import configuration #######");		
		createVariable(TestParams.PARAM_CONFIGFILE,  PACKAGE + "apimanager-config.json");
		createVariable(TestParams.PARAM_EXPECTED_RC, "0");
		createVariable("portalName", "MY API-MANAGER NAME");
		configImport.doExecute(context);
		
		echo("####### Validate configuration has been applied #######");
		http(builder -> builder.client("apiManager").send().get("/config").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.portalName", "${portalName}"));
		
		echo("####### Import configuration #######");		
		createVariable(TestParams.PARAM_CONFIGFILE,  PACKAGE + "apimanager-config.json");
		createVariable(TestParams.PARAM_EXPECTED_RC, "17");
		createVariable(TestParams.PARAM_IGNORE_ADMIN_ACC, "true");
		configImport.doExecute(context);
	}
}
