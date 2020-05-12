package com.axway.apim.appimport.adapter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.testng.annotations.Test;

import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;

public class JSONClientAppAdapterTest {

	private static final String testPackage = "/com/axway/apim/appimport/adapter";

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
}
