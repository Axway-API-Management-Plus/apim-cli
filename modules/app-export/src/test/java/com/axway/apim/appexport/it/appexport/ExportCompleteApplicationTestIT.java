package com.axway.apim.appexport.it.appexport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.appexport.ApplicationExportTestAction;
import com.axway.apim.appimport.ApplicationImportTestAction;
import com.axway.apim.appimport.adapter.jackson.AppCredentialsDeserializer;
import com.axway.apim.lib.errorHandling.AppException;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Test
public class ExportCompleteApplicationTestIT extends TestNGCitrusTestRunner {
	
	private ObjectMapper mapper = new ObjectMapper();

	private ApplicationImportTestAction appImport = new ApplicationImportTestAction();
	private ApplicationExportTestAction appExport = new ApplicationExportTestAction();
	
	private static String PACKAGE = "/com/axway/apim/appimport/apps/basic/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void exportComplteApplicationTest(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		variable("targetFolder", "citrus:systemProperty('java.io.tmpdir')");
		mapper.registerModule(new SimpleModule().addDeserializer(ClientAppCredential.class, new AppCredentialsDeserializer()));
		
		description("Export complete application from API-Manager tests");
		
		variable("appNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("appName", "Complete-App-for-export-${appNumber}");
		variable("phone", "123456789-${appNumber}");
		variable("description", "My App-Description ${appNumber}");
		variable("email", "email-${appNumber}@customer.com");
		variable("state", "approved");
		variable("appImage", "app-image.jpg");
		variable("quotaMessages", "9999");
		variable("quotaPeriod", "week");
		
		
		echo("####### Import application: '${appName}' to be exported afterwards #######");		
		createVariable(ApplicationImportTestAction.CONFIG,  PACKAGE + "CompleteApplication.json");
		createVariable("expectedReturnCode", "0");
		appImport.doExecute(context);
		
		echo("####### Export the application that has been imported before #######");
		createVariable("expectedReturnCode", "0");
		appExport.doExecute(context);
		
		String exportedAppConfigFile = context.getVariable("targetFolder")+"/"+context.getVariable("appName")+"/"+context.getVariable("appName")+".json";
		
		echo("####### Reading exported API-Config file: '"+exportedAppConfigFile+"' #######");
		JsonNode exportedAppConfig = mapper.readTree(new FileInputStream(new File(exportedAppConfigFile)));
		JsonNode importedAppConfig = mapper.readTree(new FileInputStream(context.getVariable("configFile")));
		
		assertEquals(exportedAppConfig.get("name").asText(), 				context.getVariable("appName"), "name not equal.");
		assertEquals(exportedAppConfig.get("description").asText(), 		context.getVariable("description"), "description not equal.");
		assertEquals(exportedAppConfig.get("state").asText(), 				context.getVariable("state"), "state not equal.");
		assertEquals(exportedAppConfig.get("image").asText(), 				"app-image.jpg", "image not equal.");
		assertEquals(exportedAppConfig.get("enabled").asText(), 			"true", "enabled is not true");
		assertEquals(exportedAppConfig.get("email").asText(), 				context.getVariable("email"), "email not equal.");
		assertEquals(exportedAppConfig.get("phone").asText(), 				context.getVariable("phone"), "phone not equal.");
		
		assertEquals(exportedAppConfig.get("organization").asText(), 		context.getVariable("${orgName}"), "Organization not equal.");
		
		assertTrue(new File(context.getVariable("targetFolder")+"/"+context.getVariable("appName")+"/app-image.jpg").exists(), "Application image is missing");
		
		List<ClientAppCredential> importedCredentials = mapper.convertValue(importedAppConfig.get("credentials"), new TypeReference<List<ClientAppCredential>>(){});
		List<ClientAppCredential> exportedCredentials = mapper.convertValue(importedAppConfig.get("credentials"), new TypeReference<List<ClientAppCredential>>(){});
		
		assertEquals(importedCredentials, exportedCredentials, "Application credentials are not equal.");
		
		APIQuota importedAppQuota = mapper.convertValue(importedAppConfig.get("appQuota"), APIQuota.class);
		APIQuota exportedAppQuota = mapper.convertValue(exportedAppConfig.get("appQuota"), APIQuota.class);
		
		assertEquals(importedAppQuota, exportedAppQuota, "Application quotas are not equal.");
	}
}
