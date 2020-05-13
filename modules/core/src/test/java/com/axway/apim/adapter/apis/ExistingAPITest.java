package com.axway.apim.adapter.apis;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExistingAPITest {
	
	ObjectMapper mapper = new ObjectMapper();
	APIManagerAPIAdapter existingApis;
	
	@BeforeClass
	private void initTestIndicator() {
		TestIndicator.getInstance().setTestRunning(true);
	}
	
	@Test
	public void duplicateVHost() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
			.hasApiPath("/api/test/DifferentVHostExportTestIT-531").build();

		List<JsonNode> apis = apiAdapter.getAPIs(filter, true);
		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(apis.size(), 2, "Expected 2 APIs exposed on the same path with a different V-Host");
		Assert.assertEquals(apis.get(0).get("path").asText(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(0).get("name").asText(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(0).get("vhost").asText(), "vhost2.customer.com", "First API must be exposed on vhost2.customer.com");
		
		Assert.assertEquals(apis.get(1).get("path").asText(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(1).get("name").asText(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(1).get("vhost").asText(), "vhost1.customer.com", "First API must be exposed on vhost1.customer.com");
	}
	
	@Test
	public void restrictedOnVHost() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.hasApiPath("/api/test/DifferentVHostExportTestIT-531")
				.hasVHost("vhost2.customer.com")
				.build();
		List<JsonNode> apis = apiAdapter.getAPIs(filter, true);
		
		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(apis.size(), 1, "Expected 1 APIs with requested V-Host");
		Assert.assertEquals(apis.get(0).get("path").asText(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(0).get("name").asText(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(0).get("vhost").asText(), "vhost2.customer.com", "First API must be exposed on vhost2.customer.com");
	}
	
	@Test
	public void nonExistingAPI() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.hasApiPath("/api/test/Not-ExistingAPI")
				.build();
		List<JsonNode> apis = apiAdapter.getAPIs(filter, true);

		Assert.assertEquals(apis.size(), 0, "It was not expected to find an API on path /api/test/Not-ExistingAPI");
	}
	
	@Test
	public void nonExistingUniqueAPI() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.hasApiPath("/api/test/Not-ExistingAPI")
				.build();
		JsonNode api = apiAdapter.getAPI(filter, true);

		Assert.assertNull(api);
	}
	
	@Test(expectedExceptions = AppException.class)
	public void resultMustBeUniqueButIsNot() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.hasApiPath("/api/test/DifferentVHostExportTestIT-531")
				.build();
		
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void reponseContainsOneAPIOnly() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/incompleteProxyAPI.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.build();
		List<JsonNode> apis = apiAdapter.getAPIs(filter, true);

		Assert.assertEquals(apis.size(), 1, "We expect one API to get back.");
	}	
	
	@Test
	public void nothingGivenToFilterTest() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/allProxies.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.build();
		List<JsonNode> apis = apiAdapter.getAPIs(filter, true);
		
		int numberOfAPIs = apiManagerResponse.size();

		Assert.assertEquals(apis.size(), numberOfAPIs, "We expect all APIs to get back in the list.");
	}
	
	@Test(expectedExceptions = AppException.class)
	public void getUniqueWithRoutingKeyNotOkay() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.hasQueryStringVersion("1.0")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyOK() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.hasQueryStringVersion("1.0")
				.hasApiPath("/api/emr/catalog")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyOK2() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.hasQueryStringVersion("1.1")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyVHostOK() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
			
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.hasQueryStringVersion("1.1")
				.hasVHost("api.customer.com")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyVHostOK2() throws AppException, IOException {
		JsonNode apiManagerResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setTestAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder(APIManagerAdapter.TYPE_FRONT_END)
				.hasQueryStringVersion("2.0")
				.hasVHost("api2.customer.com")
				.build();
		List<JsonNode> apis = apiAdapter.getAPIs(filter, true);

		Assert.assertEquals(apis.size(), 2);
	}
}
