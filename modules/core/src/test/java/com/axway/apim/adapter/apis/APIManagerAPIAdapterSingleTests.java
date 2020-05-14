package com.axway.apim.adapter.apis;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerAPIAdapterSingleTests {
	
	private static final String testPackage = "com/axway/apim/adapter/apimanager/singleTests/";
	
	ObjectMapper mapper = new ObjectMapper();
	APIManagerAPIAdapter apiAdapter;
	
	@BeforeClass
	private void initTestIndicator() throws AppException, IOException {
		APIManagerAdapter.configAdapter.setAPIManagerTestResponse(mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/apis/config/configAsAdmin.json")), true);
		APIManagerAdapter.configAdapter.setAPIManagerTestResponse(mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/apis/config/configAsOrgAdmin.json")), false);
		TestIndicator.getInstance().setTestRunning(true);
		APIManagerAdapter.getInstance();
		apiAdapter = (APIManagerAPIAdapter)APIAdapter.create(APIManagerAdapter.getInstance());
	}
	
	@Test
	public void duplicateVHost() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
			.hasApiPath("/api/test/DifferentVHostExportTestIT-531").build();

		List<API> apis = apiAdapter.getAPIs(filter, true);
		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(apis.size(), 2, "Expected 2 APIs exposed on the same path with a different V-Host");
		
		Assert.assertEquals(apis.get(0).getPath(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(0).getName(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(0).getVhost(), "vhost2.customer.com", "First API must be exposed on vhost2.customer.com");
		
		Assert.assertEquals(apis.get(1).getPath(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(1).getName(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(1).getVhost(), "vhost1.customer.com", "First API must be exposed on vhost1.customer.com");
	}
	
	@Test
	public void restrictedOnVHost() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/api/test/DifferentVHostExportTestIT-531")
				.hasVHost("vhost2.customer.com")
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);
		
		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(apis.size(), 1, "Expected 1 APIs with requested V-Host");
		Assert.assertEquals(apis.get(0).getPath(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(0).getName(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(0).getVhost(), "vhost2.customer.com", "First API must be exposed on vhost2.customer.com");
	}
	
	@Test
	public void nonExistingAPI() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/api/test/Not-ExistingAPI")
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);

		Assert.assertEquals(apis.size(), 0, "It was not expected to find an API on path /api/test/Not-ExistingAPI");
	}
	
	@Test
	public void nonExistingUniqueAPI() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/api/test/Not-ExistingAPI")
				.build();
		API api = apiAdapter.getAPI(filter, true);

		Assert.assertNull(api);
	}
	
	@Test(expectedExceptions = AppException.class)
	public void resultMustBeUniqueButIsNot() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/api/test/DifferentVHostExportTestIT-531")
				.build();
		
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void reponseContainsOneAPIOnly() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "incompleteProxyAPI.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);

		Assert.assertEquals(apis.size(), 1, "We expect one API to get back.");
	}	
	
	@Test
	public void nothingGivenToFilterTest() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "allProxies.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);
		
		int numberOfAPIs = mapper.readTree(apiManagerResponse).size();

		Assert.assertEquals(apis.size(), numberOfAPIs, "We expect all APIs to get back in the list.");
	}
	
	@Test(expectedExceptions = AppException.class)
	public void getUniqueWithRoutingKeyNotOkay() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("1.0")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyOK() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("1.0")
				.hasApiPath("/api/emr/catalog")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyOK2() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("1.1")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyVHostOK() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
			
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("1.1")
				.hasVHost("api.customer.com")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyVHostOK2() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("2.0")
				.hasVHost("api2.customer.com")
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);

		Assert.assertEquals(apis.size(), 2);
	}
}
