package com.axway.apim.appimport.it.customProperties;

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
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Test
public class ImportExportWithCustomPropsTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private static String PACKAGE = "/com/axway/apim/appimport/apps/customProperties/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import application with Custom-Properties into API-Manager");
		
		ImportAppTestAction importApp = new ImportAppTestAction(context);
		ExportAppTestAction exportApp = new ExportAppTestAction(context);
		
		variable("appName", "My-Custom-Prop-App-"+importApp.getRandomNum());

		echo("####### Import application: '${appName}' #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "AppWithCustomProperties.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' has been imported with Custom-Properties #######");
		http(builder -> builder.client("apiManager").send().get("/applications?field=name&op=eq&value=${appName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
			.validate("$.[?(@.name=='${appName}')].appCustomProperty1", "Custom value 1")
			.validate("$.[?(@.name=='${appName}')].appCustomProperty2", "2")
			.validate("$.[?(@.name=='${appName}')].appCustomProperty3", "true")
			.extractFromPayload("$.[?(@.id=='${appName}')].id", "appId"));
		
		echo("####### Re-Import same application - Should be a No-Change #######");
		createVariable(PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);
		
		echo("####### Export the application: '${appName}' - To validate custom properties are exported #######");
		createVariable(PARAM_TARGET, exportApp.getTestDirectory().getPath());
		createVariable(PARAM_EXPECTED_RC, "0");
		createVariable(PARAM_OUTPUT_FORMAT, "json");
		createVariable(PARAM_NAME, "${appName}");
		exportApp.doExecute(context);
		
		Assert.assertEquals(exportApp.getLastResult().getExportedFiles().size(), 1, "Expected to have one application exported");
		String exportedConfig = exportApp.getLastResult().getExportedFiles().get(0);
		
		ClientApplication exportedApp = mapper.readValue(new File(exportedConfig), ClientApplication.class);
		
		Assert.assertNotNull(exportedApp.getCustomProperties(), "Exported client application must have custom properties");
		Assert.assertEquals(exportedApp.getCustomProperties().size(), 3, "Exported client application must have 3 custom properties");
		Assert.assertEquals(exportedApp.getCustomProperties().get("appCustomProperty1"), "Custom value 1");
		Assert.assertEquals(exportedApp.getCustomProperties().get("appCustomProperty2"), "2");
		Assert.assertEquals(exportedApp.getCustomProperties().get("appCustomProperty3"), "true");
		
		echo("####### And Re-Import the exported application - Which should be a again a No-Change #######");
		createVariable(PARAM_CONFIGFILE,  exportedConfig);
		createVariable("expectedReturnCode", "10");
		importApp.doExecute(context);
	}
}
