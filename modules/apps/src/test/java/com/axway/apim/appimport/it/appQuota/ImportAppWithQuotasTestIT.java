package com.axway.apim.appimport.it.appQuota;

import com.axway.apim.APIImportApp;
import com.axway.apim.EndpointConfig;
import com.axway.apim.TestUtils;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer;
import com.axway.apim.adapter.jackson.QuotaRestrictionDeserializer.DeserializeMode;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.appexport.ApplicationExportApp;
import com.axway.apim.appimport.ClientApplicationImportApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.JsonPathSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})
public class ImportAppWithQuotasTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient apiManager;


	@CitrusTest
	@Test
	public void run(@Optional @CitrusResource TestContext context) throws IOException {
		description("Import application into API-Manager");
		ObjectMapper mapper = new ObjectMapper();
		String randomId = RandomNumberFunction.getRandomNumber(4, true);
		variable("useApiAdmin", "true"); // Use apiadmin account
		variable("appName", "My-App-"+randomId);
		variable("apiName", "Test-API-"+randomId);
		variable("apiPath", "/test/api/"+randomId);

		$(echo("####### Importing Test API 1 : '${apiName}' on path: '${apiPath}' #######"));

        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/test-api-config.json",
            context, "apps", true);
        String specFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/petstore.json",
            context, "apps", false);
        $(testContext -> {
            String[] args = {"api", "import", "-c", updatedConfigFile, "-a", specFile, "-h", testContext.getVariable("apiManagerHost"),
                "-u", testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass")};
            int returnCode = APIImportApp.importAPI(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });


        $(echo("####### Import application: '${appName}' without quotas #######"));
        String updatedConfigFile2 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/AppWithNoQuotas.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile2, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });


        $(echo("####### Import Same application: '${appName}' incl. quotas #######"));
        String updatedConfigFile3 = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/AppWithQuotas.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile3, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });

        $(echo("####### Validate application: '${appName}' incl. quotas has been imported #######"));
        $(http().client(apiManager).send().get("/applications?field=name&op=eq&value=${appName}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(JsonPathSupport.jsonPath()
			.expression("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")).extract(fromBody()
			.expression("$.[?(@.id=='${appName}')].id", "appId")));
        $(sleep().seconds(3));
        $(echo("####### Re-Import same application - Should be a No-Change #######"));
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile3, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"),
                "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 10)
                throw new ValidationException("Expected RC was: 10 but got: " + returnCode);
        });

        $(echo("####### Export the application: '${appName}' - To validate quotas are correctly exported #######"));

        String tmpDirPath = TestUtils.createTestDirectory("apps").getPath();
        String appName = context.replaceDynamicContentInString("${appName}");

        $(testContext -> {
            String[] args = {"org", "get", "-n", appName, "-t", tmpDirPath, "-deleteTarget", "-h", testContext.getVariable("apiManagerHost"), "-u",
                testContext.getVariable("apiManagerUser"), "-p", testContext.getVariable("apiManagerPass"), "-o", "json"};
            int returnCode = ApplicationExportApp.export(args);
            if (returnCode != 0)
                throw new ValidationException("Expected RC was: 0 but got: " + returnCode);
        });
        Assert.assertEquals(new File(tmpDirPath, appName).listFiles().length, 1, "Expected to have one application exported");
        String exportedConfig = new File(tmpDirPath, appName).listFiles()[0].getPath();
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
