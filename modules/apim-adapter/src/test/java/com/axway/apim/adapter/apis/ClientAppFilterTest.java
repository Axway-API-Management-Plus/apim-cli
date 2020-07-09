package com.axway.apim.adapter.apis;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientAppFilterTest {
	
	private static String TEST_PACKAGE = "com/axway/apim/adapter/apimanager/testApps/";
	
	@BeforeClass
	public void setupTestIndicator() {
		TestIndicator.getInstance().setTestRunning(true);
	}
	
	@Test
	public void hasFullWildCardName() throws AppException {
		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasName("*")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 0);
	}
	
	@Test
	public void credentialANDRedirectURLFilterTest() throws AppException, JsonParseException, JsonMappingException, IOException {
		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasCredential("6cd55c27-675a-444a-9bc7-ae9a7869184d")
				.build();
		ClientApplication testApp = getTestApp("client-app-with-two-redirectUrls.json");

		assertTrue(filter.filter(testApp), "App must match with API-Key: 6cd55c27-675a-444a-9bc7-ae9a7869184d");

	}
	
	private ClientApplication getTestApp(String appConfig) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE+appConfig);
		ClientApplication app = mapper.readValue(is, ClientApplication.class);
		return app;
	}
}
