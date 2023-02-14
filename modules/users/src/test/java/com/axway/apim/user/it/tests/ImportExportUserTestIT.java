package com.axway.apim.user.it.tests;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.testActions.TestParams;
import com.axway.apim.user.it.ExportUserTestAction;
import com.axway.apim.user.it.ImportUserTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

@Test
public class ImportExportUserTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private static final String PACKAGE = "/com/axway/apim/users/userImport/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
		description("Import user into API-Manager incl. custom properties");
		ImportUserTestAction importApp = new ImportUserTestAction(context);
		ExportUserTestAction exportApp = new ExportUserTestAction(context);

		variable("loginName", "My-User-"+importApp.getRandomNum());
		variable("password", "changeme");
		variable("phone", "+006856778789");
		variable("mobile", "+534534534435");
		variable("userCustomProperty1", "User custom value 1");
		variable("userCustomProperty2", "2");
		variable("userCustomProperty3", "true");

		echo("####### Import user: '${loginName}' having custom properties and a password #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "SingleUser.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate user: '${loginName}' has been imported incl. custom properties and the given password #######");
		http(builder -> builder.client("apiManager").send().get("/users?field=loginName&op=eq&value=${loginName}").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.loginName=='${loginName}')].loginName", "@assertThat(hasSize(1))@")
			.validate("$.[?(@.loginName=='${loginName}')].userCustomProperty1", "User custom value 1")
			.validate("$.[?(@.loginName=='${loginName}')].userCustomProperty2", "2")
			.validate("$.[?(@.loginName=='${loginName}')].userCustomProperty3", "true")
			.validate("$.[?(@.loginName=='${loginName}')].phone", "+006856778789")
			.validate("$.[?(@.loginName=='${loginName}')].mobile", "+534534534435")
			.extractFromPayload("$.[?(@.loginName=='${loginName}')].id", "userId"));
		
		echo("####### Try to login with created user #######");
		http(builder -> builder.client("apiManager").send().post("/login").payload("username=${loginName}&password=${password}&success=/title").header("Content-Type", "application/x-www-form-urlencoded"));

		/*
		 * The following tests are only executed on newer API Manager versions. On older versions, updating a user fails with the error message: 
		 * {"errors":[{"code":500, "message": "Internal server error"}]}.
		 * The API Manager reports this in the trace log: 
		 * java.lang.IllegalArgumentException: Cannot refine property with name: orgs2Role, as this property is not defined in the type: API Portal_PortalUsers
		 * The reason is that the FED file: swagger-promote-7.7-20200130.fed for building the test images of earlier versions did not yet 
		 * include the update of the KPS table PortalUsers.
		 * So the conclusion is that the CLI code works and there is no problem at this point with the CLI. The effort to update the test images 
		 * is in no relation to the benefit.
		 */
		if(APIManagerAdapter.hasAPIManagerVersion("7.7.20200930")) {
			echo("####### Change some user details #######");
			variable("phone", "+1111111111111");
			variable("mobile", "+2222222222222");
			variable("userCustomProperty1", "Changed custom value 1");
			variable("userCustomProperty2", "3");
			variable("userCustomProperty3", "false");
			importApp.doExecute(context);
			
			echo("####### Validate details of user: '${loginName}' (ID: ${userId}) have changed #######");
			http(builder -> builder.client("apiManager").send().get("/users/${userId}").header("Content-Type", "application/json"));
			http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.loginName=='${loginName}')].userCustomProperty1", "Changed custom value 1")
				.validate("$.[?(@.loginName=='${loginName}')].userCustomProperty2", "3")
				.validate("$.[?(@.loginName=='${loginName}')].userCustomProperty3", "false")
				.validate("$.[?(@.loginName=='${loginName}')].phone", "+1111111111111")
				.validate("$.[?(@.loginName=='${loginName}')].mobile", "+2222222222222"));
			
			echo("####### Re-Import same user - Should be a No-Change #######");
			createVariable(PARAM_EXPECTED_RC, "10");
			importApp.doExecute(context);
			
			echo("####### Export the user #######");
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
			Assert.assertEquals(exportedUser.getCustomProperties().get("userCustomProperty1"), "Changed custom value 1");
			Assert.assertEquals(exportedUser.getCustomProperties().get("userCustomProperty2"), "3");
			Assert.assertEquals(exportedUser.getCustomProperties().get("userCustomProperty3"), "false");
			
			echo("####### Re-Import EXPORTED user - Should be a No-Change #######");
			createVariable(PARAM_CONFIGFILE,  exportedConfig);
			createVariable("expectedReturnCode", "10");
			importApp.doExecute(context);
		}
	}
}
