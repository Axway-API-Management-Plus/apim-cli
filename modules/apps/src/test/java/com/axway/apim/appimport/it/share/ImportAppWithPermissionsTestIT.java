package com.axway.apim.appimport.it.share;

import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appimport.it.ExportAppTestAction;
import com.axway.apim.appimport.it.ImportAppTestAction;
import com.axway.apim.testActions.TestParams;
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
public class ImportAppWithPermissionsTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private static final String PACKAGE = "/com/axway/apim/appimport/apps/appPermissions/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
		description("Import application incl. shares into API-Manager");
		
		ImportAppTestAction importApp = new ImportAppTestAction(context);
		int no = importApp.getRandomNum();
		createVariable("username1", "User-A-"+no);
		createVariable("username2", "User-B-"+no);
		createVariable("username3", "User-C-"+no);
		http(builder -> builder.client("apiManager").send().post("/users").header("Content-Type", "application/json")
				.payload("{\"loginName\":\"${username1}\",\"name\":\"${username1}\",\"email\":\"${username1}@company.com\",\"role\":\"oadmin\",\"organizationId\":\"${orgId}\"}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.extractFromPayload("$.id", "userId-1"));
		echo("####### Created Test-User 1 to share with: '${username1}' (${userId-1}) #######");
		
		http(builder -> builder.client("apiManager").send().post("/users").header("Content-Type", "application/json")
				.payload("{\"loginName\":\"${username2}\",\"name\":\"${username2}\",\"email\":\"${username2}@company.com\",\"role\":\"oadmin\",\"organizationId\":\"${orgId}\"}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.extractFromPayload("$.id", "userId-2"));
		echo("####### Created Test-User 2 to share with: '${username2}' (${userId-2}) #######");
		
		http(builder -> builder.client("apiManager").send().post("/users").header("Content-Type", "application/json")
				.payload("{\"loginName\":\"${username3}\",\"name\":\"${username3}\",\"email\":\"${username3}@company.com\",\"role\":\"oadmin\",\"organizationId\":\"${orgId}\"}"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
				.extractFromPayload("$.id", "userId-3"));
		echo("####### Created Test-User 3 to share with: '${username3}' (${userId-3}) #######");
		
		variable("appName", "Shared-App-"+no);

		echo("####### Import application: '${appName}' #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "AppWith2Permissions.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications?field=name&op=eq&value=${appName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
			.extractFromPayload("$.[?(@.name=='${appName}')].id", "appId"));
		
		echo("####### Validate application: '${appName}' (${appId}) has defined permissions #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/permissions").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.*.id", "@assertThat(hasSize(3))@")   // Must be three, as the application is created by an OrgAdmin
				.validate("$.[?(@.userId=='${userId-1}')].permission", "manage")
				.validate("$.[?(@.userId=='${userId-2}')].permission", "view")); 
		
		echo("####### Re-Import same application - Should be a No-Change #######");
		createVariable(PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);
		
		echo("####### Reduce number of permissions #######");
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "AppWith1Permission1Invalid.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' (${appId}) has reduced permissions #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/permissions").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.*.id", "@assertThat(hasSize(2))@")   // Must be three, as the application is created by an OrgAdmin
				.validate("$.[?(@.userId=='${userId-2}')].permission", "view"));
		
		echo("####### Replicate with ALL permissions #######");
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "AppWithALLPermissions.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' (${appId}) has permissions for ALL users #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/permissions").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.*.id", "@assertThat(hasSize(4))@")   // Must be four, as the application is created by an OrgAdmin
				.validate("$.[?(@.userId=='${userId-1}')].permission", "view")
				.validate("$.[?(@.userId=='${userId-2}')].permission", "view")
				.validate("$.[?(@.userId=='${userId-3}')].permission", "view"));
		
		echo("####### Replicate with ALL permissions and ONE Manage override for user: ${username2} #######");
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "AppWithALLPermOneOverride.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' (${appId}) has permissions for ALL users and ONE manage #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/permissions").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.*.id", "@assertThat(hasSize(4))@")   // Must be four, as the application is created by an OrgAdmin
				.validate("$.[?(@.userId=='${userId-1}')].permission", "view")
				.validate("$.[?(@.userId=='${userId-2}')].permission", "manage")
				.validate("$.[?(@.userId=='${userId-3}')].permission", "view"));
		
		ExportAppTestAction exportApp = new ExportAppTestAction(context);
		
		echo("####### Export the application: '${appName}' - To validate permissions are exported #######");
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
