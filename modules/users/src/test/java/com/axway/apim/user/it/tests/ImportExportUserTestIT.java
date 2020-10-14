package com.axway.apim.user.it.tests;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.ExportResult;
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

@Test
public class ImportExportUserTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private static String PACKAGE = "/com/axway/apim/users/userImport/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import user into API-Manager");
		ImportUserTestAction importApp = new ImportUserTestAction(context);
		ExportUserTestAction exportApp = new ExportUserTestAction(context);
		
		variable("loginName", "My-User-"+importApp.getRandomNum());

		echo("####### Import user: '${loginName}' #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "SingleUser.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate user: '${loginName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/users?field=loginName&op=eq&value=${loginName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.loginName=='${loginName}')].loginName", "@assertThat(hasSize(1))@")
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
		
		ExportResult result = exportApp.getLastResult();
		
		Assert.assertEquals(result.getExportedFiles().size(), 1, "One exported user is expected.");
		
		echo("####### Re-Import EXPORTED user - Should be a No-Change #######");
		createVariable(PARAM_CONFIGFILE,  result.getExportedFiles().get(0));
		createVariable("expectedReturnCode", "10");
		importApp.doExecute(context);
	}
}
