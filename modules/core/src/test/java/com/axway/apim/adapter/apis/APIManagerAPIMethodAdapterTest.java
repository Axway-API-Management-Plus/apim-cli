package com.axway.apim.adapter.apis;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import com.axway.apim.api.model.APIMethod;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;

public class APIManagerAPIMethodAdapterTest {
	
	private static final String testPackage = "com/axway/apim/adapter/apis/";
	
	@BeforeClass
	private void initTestIndicator() {
		TestIndicator.getInstance().setTestRunning(true);
	}
	
	@Test
	public void testGetAllAPIMethods() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiMethods.json"));
		assertNotNull(apiManagerResponse);
		APIManagerAPIMethodAdapter methodAdapter = new APIManagerAPIMethodAdapter();
		methodAdapter.setAPIManagerTestResponse("1234567890", apiManagerResponse);
		
		List<APIMethod> methods = methodAdapter.getAllMethodsForAPI("1234567890");

		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(methods.size(), 20, "Expected 20 APIMethods");
		APIMethod method = methods.get(0);
		
		Assert.assertEquals(method.getName(), "updatePet");
		Assert.assertEquals(method.getSummary(), "Update an existing pet");
	}
	
	@Test
	public void testGetMethodIdPerName() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiMethods.json"));
		assertNotNull(apiManagerResponse);
		APIManagerAPIMethodAdapter methodAdapter = new APIManagerAPIMethodAdapter();
		methodAdapter.setAPIManagerTestResponse("1234567890", apiManagerResponse);
		
		String methodId = methodAdapter.getMethodIdPerName("1234567890", "deletePet");

		Assert.assertEquals(methodId, "132712e4-c132-4298-bfa0-f6f5c811ad65");
	}
}
