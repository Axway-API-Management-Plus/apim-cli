package com.axway.apim.appimport.adapter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictiontype;
import com.axway.apim.api.model.apps.APIKey;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;

public class JSONClientAppAdapterTest extends APIManagerMockBase {

	private static final String testPackage = "/com/axway/apim/appimport/adapter";
	
	@BeforeClass
	private void initTestIndicator() throws AppException, IOException {
		new CommandParameters(new HashMap<String, String>());
		setupMockData();
	}

	@Test
	public void testSingleAppAsArray() throws AppException {
		String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/SingleClientAppAsArray.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		ClientAppAdapter adapter = ClientAppAdapter.create(testFile);
		assertTrue(adapter instanceof JSONConfigClientAppAdapter, "Adapter is not a JSONConfigClientAppAdapter");
		List<ClientApplication> apps = adapter.getApplications();
		assertEquals(apps.size(), 1, "Expected 1 app returned from the Adapter");
	}
	
	@Test
	public void testSingleApp() throws AppException {
		String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/OneSingleClientApp.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		ClientAppAdapter adapter = ClientAppAdapter.create(testFile);
		assertTrue(adapter instanceof JSONConfigClientAppAdapter, "Adapter is not a JSONConfigClientAppAdapter");
		List<ClientApplication> apps = adapter.getApplications();
		assertEquals(apps.size(), 1, "Expected 1 app returned from the Adapter");
	}
	
	@Test
	public void testMultipleApps() throws AppException {
		String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/MulitpleTestApplications.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		ClientAppAdapter adapter = ClientAppAdapter.create(testFile);
		assertTrue(adapter instanceof JSONConfigClientAppAdapter, "Adapter is not a JSONConfigClientAppAdapter");
		List<ClientApplication> apps = adapter.getApplications();
		assertEquals(apps.size(), 2, "Expected 2 app returned from the Adapter");
	}
	
	@Test
	public void testCompleteApp() throws AppException {
		String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/CompleteApplication.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		ClientAppAdapter adapter = ClientAppAdapter.create(testFile);
		assertTrue(adapter instanceof JSONConfigClientAppAdapter, "Adapter is not a JSONConfigClientAppAdapter");
		List<ClientApplication> apps = adapter.getApplications();
		assertEquals(apps.size(), 1, "Expected 1 app returned from the Adapter");
		ClientApplication app = apps.get(0);
		assertEquals(app.getName(), "Complete application");
		assertEquals(app.getDescription(), "Sample Client Application, registered for use in the Client Demo");
		assertEquals(app.getImageUrl(), "app-image.jpg");
		assertTrue(app.getImage() instanceof Image);
		assertNotNull(app.getImage().getImageContent(), "No image content");
		assertEquals(app.isEnabled(), true);
		assertEquals(app.getEmail(), "sample@sampleapp.com");
		assertEquals(app.getPhone(), "012345678");
		assertNotNull(app.getCredentials(), "getCredentials is null");
		assertEquals(app.getCredentials().size(), 2, "Expected 2 credentials");
		
		ClientAppCredential oauthCred = app.getCredentials().get(0);
		assertTrue(oauthCred instanceof OAuth, "Expected OAuth credentials");
		assertEquals(oauthCred.getId(), "ClientConfidentialApp");
		assertTrue(((OAuth)oauthCred).getCert().startsWith("-----BEGIN CERTIFICATE-----"), "Expecte OAuth-Cert: '"+((OAuth)oauthCred).getCert()+"' to start with -----BEGIN CERTIFICATE-----");
		
		ClientAppCredential apikeyCred = app.getCredentials().get(1);
		assertTrue(apikeyCred instanceof APIKey, "Expected APIKey credentials");
		assertEquals(apikeyCred.getId(), "6cd55c27-675a-444a-9bc7-ae9a7869184d");
		
		APIQuota appQuota = app.getAppQuota();
		assertNotNull(appQuota, "appQuota is null");
		assertNotNull(appQuota.getRestrictions(), "appQuota restrictions are null");
		assertEquals(appQuota.getRestrictions().size(), 1, "Expected one restriction");
		QuotaRestriction restr = appQuota.getRestrictions().get(0);
		assertEquals(restr.getApi(), "*");
		assertEquals(restr.getMethod(), "*");
		assertEquals(restr.getType(), QuotaRestrictiontype.throttle);
		assertEquals(restr.getConfig().get("messages"), "9999");
		assertEquals(restr.getConfig().get("period"), "week");
		assertEquals(restr.getConfig().get("per"), "1");
	}
}
