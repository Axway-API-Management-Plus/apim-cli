package com.axway.apim.organization.it.tests;

import com.axway.apim.organization.it.ExportOrganizationTestAction;
import com.axway.apim.organization.it.ImportOrganizationTestAction;
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test
public class ImportSimpleOrganizationTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private static final String PACKAGE = "/com/axway/apim/organization/orgImport/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) {
		description("Import organization into API-Manager");
		ExportOrganizationTestAction exportApp = new ExportOrganizationTestAction(context);
		ImportOrganizationTestAction importApp = new ImportOrganizationTestAction(context);

		variable("orgName", "My-Org-"+importApp.getRandomNum());
		variable("orgDescription", "A description for my org");
		// This test must be executed with an Admin-Account as we need to create a new organization
		//createVariable(PARAM_IGNORE_ADMIN_ACC, "fals");

		echo("####### Import organization: '${orgName}' #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "SingleOrganization.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate organization: '${orgName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/organizations?field=name&op=eq&value=${orgName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${orgName}')].name", "@assertThat(hasSize(1))@")
			.extractFromPayload("$.[?(@.id=='${orgName}')].id", "orgId"));
		
		echo("####### Re-Import same organization - Should be a No-Change #######");
		createVariable(PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);
		
		echo("####### Change the description and import it again #######");
		createVariable("orgDescription", "My changed org description");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Export the organization #######");
		createVariable(PARAM_TARGET, exportApp.getTestDirectory().getPath());
		createVariable(PARAM_EXPECTED_RC, "0");
		createVariable(PARAM_OUTPUT_FORMAT, "json");
		createVariable(PARAM_NAME, "${orgName}");
		exportApp.doExecute(context);
		
		Assert.assertEquals(exportApp.getLastResult().getExportedFiles().size(), 1, "Expected to have one organization exported");
		String exportedConfig = exportApp.getLastResult().getExportedFiles().get(0);
		
		echo("####### Re-Import EXPORTED organization - Should be a No-Change #######");
		createVariable(PARAM_CONFIGFILE,  exportedConfig);
		createVariable("expectedReturnCode", "10");
		importApp.doExecute(context);
	}
}
