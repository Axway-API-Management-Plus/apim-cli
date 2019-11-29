package com.axway.apim.test.basic;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIManagerAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetExistingAPITest {
	
	APIManagerAdapter apiManager;
	ObjectMapper mapper = new ObjectMapper();
	
	@BeforeSuite
	public void init() throws AppException, IOException {
		this.apiManager = APIManagerAdapter.getInstance(true);
	}
	
	@Test
	public void duplicateVHost() throws AppException, IOException {
		JsonNode jsonResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("restapi/proxies/ProxiesWithVHostDuplicates.json"));
		assertNotNull(jsonResponse);
		List<JsonNode> apis = apiManager.getExistingAPIs("/api/test/DifferentVHostExportTestIT-531", jsonResponse, null, null, APIManagerAdapter.TYPE_FRONT_END, true);
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
		JsonNode jsonResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("restapi/proxies/ProxiesWithVHostDuplicates.json"));
		assertNotNull(jsonResponse);
		List<JsonNode> apis = apiManager.getExistingAPIs("/api/test/DifferentVHostExportTestIT-531", jsonResponse, null, "vhost2.customer.com", APIManagerAdapter.TYPE_FRONT_END, true);
		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(apis.size(), 1, "Expected 1 APIs with requested V-Host");
		Assert.assertEquals(apis.get(0).get("path").asText(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(0).get("name").asText(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(0).get("vhost").asText(), "vhost2.customer.com", "First API must be exposed on vhost2.customer.com");
	}
	
	@Test
	public void nonExistingAPI() throws AppException, IOException {
		JsonNode jsonResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("restapi/proxies/ProxiesWithVHostDuplicates.json"));
		assertNotNull(jsonResponse);
		List<JsonNode> apis = apiManager.getExistingAPIs("/api/test/Not-ExistingAPI", jsonResponse, null, null, APIManagerAdapter.TYPE_FRONT_END, true);
		Assert.assertEquals(apis.size(), 0, "It was not expected to find an API on path /api/test/Not-ExistingAPI");
	}
	
	@Test
	public void findUniqueAPIBasedOnID() throws AppException, IOException {
		JsonNode jsonResponse = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream("restapi/proxies/incompleteProxyAPI.json"));
		assertNotNull(jsonResponse);
		List<JsonNode> apis = apiManager.getExistingAPIs(null, jsonResponse, null, null, APIManagerAdapter.TYPE_FRONT_END, true);
		Assert.assertEquals(apis.size(), 1, "We expect one API to get back.");
	}	
}
