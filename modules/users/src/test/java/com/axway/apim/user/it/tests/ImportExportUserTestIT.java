package com.axway.apim.user.it.tests;

import java.io.File;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.user.it.ExportUserTestAction;
import com.axway.apim.user.it.ImportUserTestAction;
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Test
public class ImportExportUserTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private static String PACKAGE = "/com/axway/apim/users/userImport/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import user into API-Manager incl. custom properties");
		ImportUserTestAction importApp = new ImportUserTestAction(context);
		ExportUserTestAction exportApp = new ExportUserTestAction(context);
		
		variable("loginName", "My-User-"+importApp.getRandomNum());

		echo("####### Import user: '${loginName}' having custom properties #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "SingleUser.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate user: '${loginName}' has been imported incl. custom properties #######");
		http(builder -> builder.client("apiManager").send().get("/users?field=loginName&op=eq&value=${loginName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.loginName=='${loginName}')].loginName", "@assertThat(hasSize(1))@")
			.validate("$.[?(@.loginName=='${loginName}')].userCustomProperty1", "User custom value 1")
			.validate("$.[?(@.loginName=='${loginName}')].userCustomProperty2", "2")
			.validate("$.[?(@.loginName=='${loginName}')].userCustomProperty3", "true")
			.extractFromPayload("$.[?(@.id=='${loginName}')].id", "userId"));
		
		echo("####### Re-Import same user - Should be a No-Change #######");
		createVariable(PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);
		
		echo("####### Export the user #######");
		ErrorState.deleteInstance();
		variable("targetFolder", "citrus:systemProperty('java.io.tmpdir')");
		createVariable(PARAM_TARGET, exportApp.getTestDirectory().getPath());
		createVariable(PARAM_LOGINNAME, "${loginName}");
		createVariable(PARAM_OUTPUT_FORMAT, "json");
		createVariable(PARAM_EXPECTED_RC, "0");
		exportApp.doExecute(context);
		
		Assert.assertEquals(exportApp.getLastResult().getExportedFiles().size(), 1, "One exported user is expected.");
		String exportedConfig = exportApp.getLastResult().getExportedFiles().get(0);
		
		ClientApplication exportedUser = mapper.readValue(new File(exportedConfig), ClientApplication.class);
		
		Assert.assertNotNull(exportedUser.getCustomProperties(), "Exported user must have custom properties");
		Assert.assertEquals(exportedUser.getCustomProperties().size(), 3, "Exported user must have 3 custom properties");
		Assert.assertEquals(exportedUser.getCustomProperties().get("userCustomProperty1"), "User custom value 1");
		Assert.assertEquals(exportedUser.getCustomProperties().get("userCustomProperty2"), "2");
		Assert.assertEquals(exportedUser.getCustomProperties().get("userCustomProperty3"), "true");
		
		echo("####### Re-Import EXPORTED user - Should be a No-Change #######");
		createVariable(PARAM_CONFIGFILE,  exportedConfig);
		createVariable("expectedReturnCode", "10");
		importApp.doExecute(context);
	}
}
