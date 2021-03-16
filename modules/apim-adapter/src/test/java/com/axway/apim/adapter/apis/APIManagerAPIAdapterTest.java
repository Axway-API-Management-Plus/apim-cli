package com.axway.apim.adapter.apis;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;

public class APIManagerAPIAdapterTest {
	
	APIManagerAPIAdapter adapter;
	
	@BeforeClass
	public void setup() throws AppException {
		adapter = new APIManagerAPIAdapter();
	}
	
	@Test
	public void testUniqueAPIWithAPIPath() throws AppException {
		API noVHostAPI = createTestAPI("/api/v1/resource", null, null);
		API vhostAPI = createTestAPI("/api/v1/other", "api.customer.com", null);
		API anotherVHostAPI = createTestAPI("/api/v1/other", "otherapi.customer.com", null);
		List<API> testAPIs = new ArrayList<API>();
		testAPIs.add(noVHostAPI);
		testAPIs.add(vhostAPI);
		testAPIs.add(anotherVHostAPI);
		
		APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").build();

		// Must return the default API (testAPI1) as we don't filter specifically for the V-Host
		API uniqueAPI = adapter.getUniqueAPI(testAPIs, filter);
		Assert.assertEquals(uniqueAPI, noVHostAPI);
	}
	
	@Test
	public void testUniqueAPISamePathDiffQueryString() throws AppException {
		API queryVersionAPI = createTestAPI("/api/v1/resource", null, "1.0");
		API noQueryVersionAPI = createTestAPI("/api/v1/resource", null, null);
		API anotherQueryVersionAPI = createTestAPI("/api/v1/resource", null, "2.0");
		API nextTextAPI = createTestAPI("/api/v1/resource", "api.customer.com", "3.0");
		List<API> testAPIs = new ArrayList<API>();
		testAPIs.add(queryVersionAPI);
		testAPIs.add(noQueryVersionAPI);
		testAPIs.add(anotherQueryVersionAPI);
		testAPIs.add(nextTextAPI);
		
		APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").build();

		// Must return testAPI2 as the default, because no queryStringVersion is given in the filter
		API uniqueAPI = adapter.getUniqueAPI(testAPIs, filter);
		Assert.assertEquals(uniqueAPI, noQueryVersionAPI);
	}
	
	@Test
	public void testUniqueAPIVHostDefault() throws AppException {
		API queryVersionAPI = createTestAPI("/api/v1/resource", "api.customer.com", "1.0");
		API noQueryVersionAPI = createTestAPI("/api/v1/resource", "api.customer.com", null);
		API anotherQueryVersionAPI = createTestAPI("/api/v1/resource", null, "2.0");
		List<API> testAPIs = new ArrayList<API>();
		testAPIs.add(queryVersionAPI);
		testAPIs.add(noQueryVersionAPI);
		testAPIs.add(anotherQueryVersionAPI);
		
		APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasVHost("api.customer.com").build();

		// Must return testAPI2 which is the default for the requested VHost as this API has not QueryVersion
		API uniqueAPI = adapter.getUniqueAPI(testAPIs, filter);
		Assert.assertEquals(uniqueAPI, noQueryVersionAPI);
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "No unique API found.*")
	public void testNoUniqueFoundWithQueryVersion() throws AppException {
		API testAPI1 = createTestAPI("/api/v1/resource", null, "1.0");
		API testAPI2 = createTestAPI("/api/v1/resource", null, "1.0");
		List<API> testAPIs = new ArrayList<API>();
		testAPIs.add(testAPI1);
		testAPIs.add(testAPI2);
		
		APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasQueryStringVersion("1.0").build();

		// Must fail (throw an Exception) as the API is really not unique, even if we filter with the QueryVersion
		API uniqueAPI = adapter.getUniqueAPI(testAPIs, filter);
		Assert.assertEquals(uniqueAPI, testAPI2);
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "No unique API found.*")
	public void testNoUniqueFoundWithQueryVersionAndVHost() throws AppException {
		API testAPI1 = createTestAPI("/api/v1/resource", "host.customer.com", "1.0");
		API testAPI2 = createTestAPI("/api/v1/resource", "host.customer.com", "1.0");
		List<API> testAPIs = new ArrayList<API>();
		testAPIs.add(testAPI1);
		testAPIs.add(testAPI2);
		
		APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasQueryStringVersion("1.0").hasVHost("host.customer.com").build();

		// Must fail (throw an Exception) as the API is really not unique, even if we filter with the QueryVersion and VHost
		API uniqueAPI = adapter.getUniqueAPI(testAPIs, filter);
		Assert.assertEquals(uniqueAPI, testAPI2);
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "No unique API found.*")
	public void testNoUniqueFoundMixedVHost() throws AppException {
		API testAPI1 = createTestAPI("/api/v1/resource", "host.customer.com", "1.0");
		API testAPI2 = createTestAPI("/api/v1/resource", "other.customer.com", "1.0");
		List<API> testAPIs = new ArrayList<API>();
		testAPIs.add(testAPI1);
		testAPIs.add(testAPI2);
		
		APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasQueryStringVersion("1.0").build();

		// Must fail (throw an Exception) as the API is really not unique, if we filter with the QueryVersion only
		API uniqueAPI = adapter.getUniqueAPI(testAPIs, filter);
		Assert.assertEquals(uniqueAPI, testAPI2);
	}
	
	private static API createTestAPI(String apiPath, String vhost, String queryVersion) {
		API testAPI = new API();
		testAPI.setPath(apiPath);
		testAPI.setVhost(vhost);
		testAPI.setApiRoutingKey(queryVersion);
		return testAPI;
	}
}
