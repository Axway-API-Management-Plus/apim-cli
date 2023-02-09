package com.axway.apim.appimport.it.basic;

import com.axway.apim.appimport.it.ImportAppTestAction;
import com.axway.lib.testActions.TestParams;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test
public class ImportCompleteApplicationTestIT extends TestNGCitrusTestRunner implements TestParams {
	
	private static final String PACKAGE = "/com/axway/apim/appimport/apps/basic/";
	
	@CitrusTest
	@Test @Parameters("context")
	public void importApplicationBasicTest(@Optional @CitrusResource TestContext context) throws InterruptedException {
		description("Import application into API-Manager");
		
		ImportAppTestAction importApp = new ImportAppTestAction(context);
		variable("useApiAdmin", "true"); // Use apiadmin account
		variable("appNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("appName", "Complete-App-${appNumber}");
		variable("phone", "123456789-${appNumber}");
		variable("description", "My App-Description ${appNumber}");
		variable("email", "email-${appNumber}@customer.com");
		variable("quotaMessages", "9999");
		variable("quotaPeriod", "week");
		variable("state", "approved");
		variable("appImage", "app-image.jpg");
		variable("oauthCorsOrigins","");
		
		variable("scopeName1","scope.READ");
		variable("scopeEnabled1",true);
		variable("scopeIsDefault1",false);
		
		variable("scopeName2","scope.WRITE");
		variable("scopeEnabled2",false);
		variable("scopeIsDefault2",false);

		echo("####### Import application: '${appName}' #######");		
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "CompleteApplication.json");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications?field=name&op=eq&value=${appName}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
			.validate("$.[?(@.name=='${appName}')].phone", "${phone}")
			.validate("$.[?(@.name=='${appName}')].description", "${description}")
			.validate("$.[?(@.name=='${appName}')].email", "${email}")
			.validate("$.[?(@.name=='${appName}')].state", "${state}")
			.validate("$.[?(@.name=='${appName}')].image", "@assertThat(containsString(/api/portal/v))@")
			.extractFromPayload("$.[?(@.name=='${appName}')].id", "appId"));
		
//		echo("####### Validate application: '${appName}' with id: ${appId} quota has been imported #######");
//		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/quota").header("Content-Type", "application/json"));
//		sleep(30000);
//		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
//				.validate("$.type", "APPLICATION")
//				.validate("$.restrictions[*].api", "@assertThat(hasSize(1))@")
//				.validate("$.restrictions[0].api", "*")
//				.validate("$.restrictions[0].method", "*")
//				.validate("$.restrictions[0].type", "throttle")
//				.validate("$.restrictions[0].config.messages", "${quotaMessages}")
//				.validate("$.restrictions[0].config.period", "${quotaPeriod}")
//				.validate("$.restrictions[0].config.per", "1"));
		
		echo("####### Validate application: '${appName}' with id: ${appId} OAuth-Credentials have been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/oauth").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$[0].id", "ClientConfidentialApp-${appNumber}")
				.validate("$[0].cert", "@assertThat(containsString(-----BEGIN CERTIFICATE-----))@")
				.validate("$[0].secret", "9cb76d80-1bc2-48d3-8d31-edeec0fddf6c")
				.validate("$[0].corsOrigins[0]", ""));
		
		echo("####### Validate application: '${appName}' with id: ${appId} API-Key has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/apikeys").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$[0].id", "6cd55c27-675a-444a-9bc7-ae9a7869184d-${appNumber}")
				.validate("$[0].secret", "34f2b2d6-0334-4dcc-8442-e0e7009b8950")
				.validate("$[0].corsOrigins[0]", ""));
		
		echo("####### Validate application: '${appName}' with id: ${appId} Ext client id has been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/extclients").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.clientId=='ClientConfidentialClientID-${appNumber}')].clientId", "ClientConfidentialClientID-${appNumber}")
				.validate("$.[?(@.clientId=='ClientConfidentialClientID-${appNumber}')].enabled", "true")
				.validate("$[0].corsOrigins[0]", ""));
		
		echo("####### Validate application: '${appName}' with id: ${appId} Application-Scopes have been imported #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/oauthresource").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.scope=='${scopeName1}')].isDefault", "${scopeIsDefault1}")
				.validate("$.[?(@.scope=='${scopeName2}')].isDefault", "${scopeIsDefault2}"));
		
		echo("####### Re-Import same application - Should be a No-Change #######");
		createVariable(PARAM_EXPECTED_RC, "10");
		importApp.doExecute(context);
		
		echo("####### Re-Import slightly modified application - Existing App should be updated #######");
		variable("email", "newemail-${appNumber}@customer.com");
		variable("quotaMessages", "1111");
		variable("quotaPeriod", "day");
		
		variable("scopeName2","scope.WRITE");
		variable("scopeEnabled2",true);
		variable("scopeIsDefault2",true);
		
		// First scope is removed - Second scope becomes the first (See the config file: CompleteApplicationOnlyOneScope.json)
		
		createVariable(PARAM_EXPECTED_RC, "0");
		createVariable(PARAM_CONFIGFILE,  PACKAGE + "CompleteApplicationOnlyOneScope.json");
		importApp.doExecute(context);
		
		echo("####### Validate application: '${appName}' (${appId}) has been updated #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.name=='${appName}')].name", "@assertThat(hasSize(1))@")
				.validate("$.[?(@.name=='${appName}')].phone", "${phone}")
				.validate("$.[?(@.name=='${appName}')].description", "${description}")
				.validate("$.[?(@.name=='${appName}')].email", "${email}") // This should be the new email
				.validate("$.[?(@.name=='${appName}')].state", "${state}")
				.validate("$.[?(@.name=='${appName}')].image", "@assertThat(containsString(/api/portal/v))@")
				.extractFromPayload("$.[?(@.name=='${appName}')].id", "appId"));
		
		echo("####### Validate modified quota for application: '${appName}' with id: ${appId} has been updated #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/quota").header("Content-Type", "application/json"));
		
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.type", "APPLICATION")
				.validate("$.restrictions[*].api", "@assertThat(hasSize(1))@")
				.validate("$.restrictions[0].api", "*")
				.validate("$.restrictions[0].method", "*")
				.validate("$.restrictions[0].type", "throttle")
				.validate("$.restrictions[0].config.messages", "${quotaMessages}")
				.validate("$.restrictions[0].config.period", "${quotaPeriod}")
				.validate("$.restrictions[0].config.per", "1"));
		
		echo("####### Validate modified scopes for application: '${appName}' with id: ${appId} #######");
		Thread.sleep(3000);
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/oauthresource").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.scope=='${scopeName2}')].enabled", "${scopeEnabled2}")
				.validate("$.[?(@.scope=='${scopeName2}')].isDefault", "${scopeIsDefault2}"));
		
		echo("####### Update the application image only #######");
		variable("appImage", "other-app-image.jpg");
		
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		
		echo("####### Re-Import change corsorigins (oauth) - Existing App should be updated #######");
		variable("oauthCorsOrigins","*");
		createVariable(PARAM_EXPECTED_RC, "0");
		importApp.doExecute(context);
		sleep(1000);

		echo("####### Validate application: '${appName}' with id: ${appId} OAuth has been changed #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/oauth").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$[0].id", "ClientConfidentialApp-${appNumber}")
				.validate("$[0].cert", "@assertThat(containsString(-----BEGIN CERTIFICATE-----))@")
				.validate("$[0].secret", "9cb76d80-1bc2-48d3-8d31-edeec0fddf6c")
				.validate("$[0].corsOrigins[0]", "*"));
		
		echo("####### Validate application: '${appName}' with id: ${appId} API-Key has been changed #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/apikeys").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$[0].id", "6cd55c27-675a-444a-9bc7-ae9a7869184d-${appNumber}")
				.validate("$[0].secret", "34f2b2d6-0334-4dcc-8442-e0e7009b8950")
				.validate("$[0].corsOrigins[0]", "*"));
		
		echo("####### Validate application: '${appName}' with id: ${appId} Ext client id has been changed #######");
		http(builder -> builder.client("apiManager").send().get("/applications/${appId}/extclients").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
				.validate("$.[?(@.clientId=='ClientConfidentialClientID-${appNumber}')].clientId", "ClientConfidentialClientID-${appNumber}")
				.validate("$.[?(@.clientId=='ClientConfidentialClientID-${appNumber}')].enabled", "true")
				.validate("$[0].corsOrigins[0]", "*"));
	}
}
