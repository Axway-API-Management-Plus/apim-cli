package com.axway.apim.test.orgadmin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="OrgAdminOAuthExternalTestIT")
public class OrgAdminOAuthExternalTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "OrgAdminOAuthExternalTestIT")
	public void run() {
		description("Org-Admin only account tests for API-OAuth (External) Security configuration");
		createVariable("useApiAdmin", "true");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/oadmin-oauth-test-${apiNumber}");
		variable("apiName", "OAdmin OAuth-External Test ${apiNumber}");
		variable("status", "unpublished");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable("tokenInfoPolicy", "Tokeninfo policy 1");
		createVariable("accessTokenLocation", "HEADER");
		createVariable("scopes", "resource.WRITE, resource.READ, resource.ADMIN");
		createVariable("removeCredentialsOnSuccess", "false");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/4_api-oauth_external.json");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with correct settings #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].type", "oauthExternal")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.tokenStore", "@assertThat(containsString(${tokenInfoPolicy}))@")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.scopes", "${scopes}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.useClientRegistry", "true")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.['oauth.token.client_id']", "${//oauth.token.client_id//}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.['oauth.token.scopes']", "${//oauth.token.scopes//}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.['oauth.token.valid']", "${//oauth.token.valid//}")
			.validate("$.[?(@.path=='${apiPath}')].securityProfiles[0].devices[0].properties.accessTokenLocation", "${accessTokenLocation}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");

		echo("####### Simulate re-import with no-change #######");
		createVariable("tokenInfoPolicy", "Tokeninfo policy 1");
		createVariable("accessTokenLocation", "HEADER");
		createVariable("scopes", "resource.WRITE, resource.READ, resource.ADMIN");
		createVariable("removeCredentialsOnSuccess", "false");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/security/4_api-oauth_external.json");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
	}
}
