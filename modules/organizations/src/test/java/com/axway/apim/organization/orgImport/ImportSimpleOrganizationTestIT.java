package com.axway.apim.organization.orgImport;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.organization.OrganizationExportTestAction;
import com.axway.apim.organization.OrganizationImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class ImportSimpleOrganizationTestIT extends TestNGCitrusTestRunner {

	private OrganizationImportTestAction orgImport = new OrganizationImportTestAction();
	private OrganizationExportTestAction orgExport = new OrganizationExportTestAction();
	
	private static String PACKAGE = "/com/axway/apim/organization/orgImport/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		description("Import organization into API-Manager");
		
		variable("orgNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("orgName", "My-Org-${orgNumber}");
		variable("orgDescription", "A description for my org");
		// This test must be executed with an Admin-Account as we need to create a new organization
		variable("oadminUsername1", "apiadmin"); 
		variable("oadminPassword1", "changeme");

		echo("####### Import organization: '${orgName}' #######");		
		createVariable(OrganizationImportTestAction.CONFIG,  PACKAGE + "SingleOrganization.json");
		createVariable("expectedReturnCode", "0");
		orgImport.doExecute(context);
		
		echo("####### Validate organization: '${orgName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/organizations?field=name&op=eq&value=${orgName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${orgName}')].name", "@assertThat(hasSize(1))@")
			.extractFromPayload("$.[?(@.id=='${orgName}')].id", "orgId"));
		
		echo("####### Re-Import same organization - Should be a No-Change #######");
		createVariable("expectedReturnCode", "10");
		orgImport.doExecute(context);
		
		echo("####### Change the description and import it again #######");
		variable("orgDescription", "My changed org description");
		createVariable("expectedReturnCode", "0");
		orgImport.doExecute(context);
		
		echo("####### Export the organization #######");
		ErrorState.deleteInstance();
		variable("targetFolder", "citrus:systemProperty('java.io.tmpdir')");
		createVariable("expectedReturnCode", "0");
		orgExport.doExecute(context);
		
		echo("####### Re-Import EXPORTED organization - Should be a No-Change #######");
		createVariable(OrganizationImportTestAction.CONFIG,  "${targetFolder}/${orgName}/org-config.json");
		createVariable("expectedReturnCode", "10");
		orgImport.doExecute(context);
	}
}
