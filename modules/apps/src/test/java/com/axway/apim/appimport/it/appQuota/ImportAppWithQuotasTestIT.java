package com.axway.apim.appimport.it.appQuota;

import java.io.File;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appimport.it.ExportAppTestAction;
import com.axway.apim.appimport.it.ImportAppTestAction;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.test.ImportTestAction;
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Test
public class ImportAppWithQuotasTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private static String PACKAGE = "/com/axway/apim/appimport/apps/appQuotas/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import application into API-Manager");
		
		ImportAppTestAction importApp = new ImportAppTestAction(context);
		ExportAppTestAction exportApp = new ExportAppTestAction(context);
		ImportTestAction apiImport = new ImportTestAction();
		ObjectMapper mapper = new ObjectMapper();
		
		variable("appName", "My-App-"+importApp.getRandomNum());
		
		echo("####### Importing Test API 1 : '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  PACKAGE + "petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  PACKAGE + "../basic/test-api-config.json");
		createVariable("expectedReturnCode", "0");
		apiImport.doExecute(context);

		echo("####### Import application: '${appName}' incl. quotas #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "AppWithQuotas.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' incl. quotas has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications?field=name&op=eq&value=${appName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
			.extractFromPayload("$.[?(@.id=='${appName}')].id", "appId"));
		
		echo("####### Re-Import same application - Should be a No-Change #######");
		createVariable(PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);
		
		echo("####### Export the application: '${appName}' - To validate quotas are correctly exported #######");
		createVariable(PARAM_TARGET, exportApp.getTestDirectory().getPath());
		createVariable(PARAM_EXPECTED_RC, "0");
		createVariable(PARAM_OUTPUT_FORMAT, "json");
		createVariable(PARAM_NAME, "${appName}");
		exportApp.doExecute(context);
		
		Assert.assertEquals(exportApp.getLastResult().getExportedFiles().size(), 1, "Expected to have one application exported");
		String exportedConfig = exportApp.getLastResult().getExportedFiles().get(0);
		
		ClientApplication exportedApp = mapper.readValue(new File(exportedConfig), ClientApplication.class);
		
		Assert.assertNotNull(exportedApp.getPermissions(), "Exported client application must have permissions");
		Assert.assertEquals(exportedApp.getPermissions().size(), 4, "Exported client application must have 4 permissions");
	}
}
