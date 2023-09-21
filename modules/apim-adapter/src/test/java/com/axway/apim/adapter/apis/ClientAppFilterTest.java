package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.client.apps.ClientAppFilter;
import com.axway.apim.adapter.jackson.AppCredentialsDeserializer;
import com.axway.apim.api.model.apps.APIKey;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ClientAppFilterTest extends WiremockWrapper {

	@BeforeClass
	public void initWiremock() {
		super.initWiremock();
	}

	@AfterClass
	public void close() {
		super.close();
	}

	@Test
	public void hasFullWildCardName() throws AppException {
		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasName("*")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 0);
	}

	@Test
	public void credentialANDRedirectURLFilterTest() throws IOException {
		ClientApplication testApp = getTestApp("client-app-with-two-redirectUrls.json");

		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasCredential("6cd55c27-675a-444a-9bc7-ae9a7869184d")
				.build();
		assertFalse(filter.filter(testApp), "App must match with API-Key: 6cd55c27-675a-444a-9bc7-ae9a7869184d");

		filter = new ClientAppFilter.Builder()
				.hasCredential("*675a*")
				.build();
		assertFalse(filter.filter(testApp), "App must match with wildcard search for API-Key: 6cd55c27-675a-444a-9bc7-ae9a7869184d");

		filter = new ClientAppFilter.Builder()
				.hasCredential("*XXXXX*")
				.build();
		assertTrue(filter.filter(testApp), "App SHOULD NOT match with wildcard search *XXXXX*");

		filter = new ClientAppFilter.Builder()
				.hasCredential("*XXXXX*")
				.hasRedirectUrl("*ZZZZZ*")
				.build();
		assertTrue(filter.filter(testApp), "App SHOULD NOT match");

		filter = new ClientAppFilter.Builder()
				.hasCredential("*XXXXX*")
				.hasRedirectUrl("*oauthclient:8088*")
				.build();
		assertTrue(filter.filter(testApp), "App SHOULD NOT match as a wrong credential is given");

		filter = new ClientAppFilter.Builder()
				.hasCredential("ClientConfidentialApp")
				.hasRedirectUrl("*oauthclient:8088*")
				.build();
		assertFalse(filter.filter(testApp), "App SHOULD match with correct credential and redirect url");

		filter = new ClientAppFilter.Builder()
				.hasRedirectUrl("*oauthclient:8088*")
				.build();
		assertFalse(filter.filter(testApp), "App SHOULD match with correct wildcard redirect url");

		filter = new ClientAppFilter.Builder()
				.hasRedirectUrl("https://oauthclient:8088/client/apigateway/callback")
				.build();
		assertFalse(filter.filter(testApp), "App SHOULD match with correct redirect url");
	}

	@Test
	public void appWithoutCredentialTest() throws IOException {
		ClientApplication testApp = getTestApp("client-app-with-two-redirectUrls.json");
		testApp.setCredentials(null);

		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasCredential("6cd55c27-675a-444a-9bc7-ae9a7869184d")
				.build();
		assertTrue(filter.filter(testApp), "App SHOULD NOT match as there are no credentials");

		filter = new ClientAppFilter.Builder()
				.hasRedirectUrl("*anything*")
				.build();
		assertTrue(filter.filter(testApp), "App SHOULD NOT match as there are no credentials");
	}

	@Test
	public void testAppHavingAPIKeyButNoClientID() throws IOException {
   		ClientApplication testApp = getTestApp("client-app-with-two-api-key-only.json");
		((APIKey)testApp.getCredentials().get(0)).setApiKey(null);

		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasCredential("Does-not-exists")
				.build();
		assertTrue(filter.filter(testApp), "App SHOULD NOT match as there are no credentials");
	}

	@Test
	public void testAppHavingAccessToAPI() throws IOException {
		ClientApplication testApp = getTestApp("client-app-with-apis.json");

		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasApiName("This API does not exists")
				.build();
		assertTrue(filter.filter(testApp), "App SHOULD NOT match as the given API doesn't exists.");

		filter = new ClientAppFilter.Builder()
				.hasApiName("*HIPAA*")
				.build();
		assertFalse(filter.filter(testApp), "App SHOULD match as the given API exists.");

		filter = new ClientAppFilter.Builder()
				.hasApiName("EMR-HealthCatalog")
				.build();
		assertFalse(filter.filter(testApp), "App SHOULD match as the given API exists.");
	}

	@Test
	public void testFilterAppCreatedByAndOrganization()  throws IOException {
		CoreParameters coreParameters = new CoreParameters();
		coreParameters.setHostname("localhost");
		coreParameters.setUsername("test");
		coreParameters.setPassword(Utils.getEncryptedPassword());
		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasCreatedByLoginName("usera")
				.hasOrganizationName("orga")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 6);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "orgid");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "987b2afc-b027-41fd-a920-bef182eb4a94");
		Assert.assertEquals(filter.getFilters().get(3).getValue(), "userid");
		Assert.assertEquals(filter.getFilters().get(4).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(5).getValue(), "2f126140-db10-4ccb-be9d-e430d9fe9c45");
	}

	private ClientApplication getTestApp(String appConfig) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new SimpleModule().addDeserializer(ClientAppCredential.class, new AppCredentialsDeserializer()));
		String TEST_PACKAGE = "com/axway/apim/adapter/apimanager/testApps/";
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE +appConfig);
		return mapper.readValue(is, ClientApplication.class);
	}
}
