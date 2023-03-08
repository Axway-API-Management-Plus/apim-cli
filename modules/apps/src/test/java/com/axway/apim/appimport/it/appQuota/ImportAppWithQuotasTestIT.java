package com.axway.apim.appimport.it.appQuota;

import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer.DeserializeMode;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appimport.it.ExportAppTestAction;
import com.axway.apim.appimport.it.ImportAppTestAction;
import com.axway.apim.test.ImportTestAction;
import com.axway.apim.test.actions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

@Test
public class ImportAppWithQuotasTestIT extends TestNGCitrusTestRunner  {

	private static final String PACKAGE = "/com/axway/apim/appimport/apps/appQuota/";

	@CitrusTest
	@Test
	@Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
		description("Import application into API-Manager");

		ImportAppTestAction importApp = new ImportAppTestAction(context);
		ExportAppTestAction exportApp = new ExportAppTestAction(context);
		ImportTestAction apiImport = new ImportTestAction();
		ObjectMapper mapper = new ObjectMapper();

		int randomId = importApp.getRandomNum();
		variable("useApiAdmin", "true"); // Use apiadmin account
		variable("appName", "My-App-"+randomId);
		variable("apiName", "Test-API-"+randomId);
		variable("apiPath", "/test/api/"+randomId);

		echo("####### Importing Test API 1 : '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/appimport/apps/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/appimport/apps/basic/test-api-config.json");
		createVariable("expectedReturnCode", "0");
		apiImport.doExecute(context);

		echo("####### Import application: '${appName}' incl. quotas #######");
		createVariable(TestParams.PARAM_CONFIGFILE,  PACKAGE + "AppWithQuotas.json");
		createVariable(TestParams.PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);

		echo("####### Validate application: '${appName}' incl. quotas has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications?field=name&op=eq&value=${appName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
			.extractFromPayload("$.[?(@.id=='${appName}')].id", "appId"));

		echo("####### Re-Import same application - Should be a No-Change #######");
		createVariable(TestParams.PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);

		echo("####### Export the application: '${appName}' - To validate quotas are correctly exported #######");
		createVariable(TestParams.PARAM_TARGET, exportApp.getTestDirectory().getPath());
		createVariable(TestParams.PARAM_EXPECTED_RC, "0");
		createVariable(TestParams.PARAM_OUTPUT_FORMAT, "json");
		createVariable(TestParams.PARAM_NAME, "${appName}");
		exportApp.doExecute(context);

		Assert.assertEquals(exportApp.getLastResult().getExportedFiles().size(), 1, "Expected to have one application exported");
		String exportedConfig = exportApp.getLastResult().getExportedFiles().get(0);

		mapper.registerModule(new SimpleModule().addDeserializer(QuotaRestriction.class, new QuotaRestrictionDeserializer(DeserializeMode.configFile, true)));
		ClientApplication exportedApp = mapper.readValue(new File(exportedConfig), ClientApplication.class);

		Assert.assertNotNull(exportedApp.getAppQuota(), "Exported client application must have application quota");

		APIQuota appQuota = exportedApp.getAppQuota();
		Assert.assertEquals(appQuota.getRestrictions().size(), 3, "Two restrictions are expected.");
		QuotaRestriction allAPIsRestri = null;
		QuotaRestriction APIRestri = null;
		QuotaRestriction APIMethodRestri = null;
		for(QuotaRestriction restr: appQuota.getRestrictions()) {
			if(restr.getConfig().get("messages").equals("1000")) {
				allAPIsRestri = restr;
			} else if(restr.getConfig().get("messages").equals("2000")) {
				APIRestri = restr;
			} else if(restr.getConfig().get("messages").equals("3000")) {
				APIMethodRestri = restr;
			}
		}
		Assert.assertNotNull(allAPIsRestri, "Expected a restriction for all APIs.");
		Assert.assertNotNull(APIRestri, "Expected a restriction for a specific APIs");

		Assert.assertEquals(allAPIsRestri.getApiId(), "*");
		Assert.assertEquals(allAPIsRestri.getMethod(), "*");

		Assert.assertEquals(APIRestri.getRestrictedAPI().getName(), context.getVariable("apiName"));
		Assert.assertEquals(APIRestri.getRestrictedAPI().getPath(), context.getVariable("apiPath"));
		Assert.assertEquals(APIRestri.getMethod(), "*");

		Assert.assertNotEquals(APIMethodRestri.getMethod(), "*");
	}
}
