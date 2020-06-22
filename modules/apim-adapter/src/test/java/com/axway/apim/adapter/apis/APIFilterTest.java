package com.axway.apim.adapter.apis;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.api.API;
import com.axway.apim.api.model.ServiceProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;

public class APIFilterTest {
	
	@BeforeClass
	public void setupTestIndicator() {
		TestIndicator.getInstance().setTestRunning(true);
	}
	
	@Test
	public void testStandardActualAPI() {
		APIFilter filter = new APIFilter.Builder(APIType.ACTUAL_API).build();
		Assert.assertEquals(filter.isIncludeClientApplications(), true);
		Assert.assertEquals(filter.isIncludeClientOrganizations(), true);
		Assert.assertEquals(filter.isIncludeQuotas(), true);
		Assert.assertEquals(filter.isIncludeOriginalAPIDefinition(), true);
	}
	
	@Test
	public void testCustomActualAPI() {
		APIFilter filter = new APIFilter.Builder(APIType.ACTUAL_API)
				.includeClientApplications(false)
				.includeClientOrganizations(false)
				.includeQuotas(false)
				.build();
		Assert.assertEquals(filter.isIncludeClientApplications(), false);
		Assert.assertEquals(filter.isIncludeClientOrganizations(), false);
		Assert.assertEquals(filter.isIncludeQuotas(), false);
		Assert.assertEquals(filter.isIncludeOriginalAPIDefinition(), true);
	}
	
	@Test
	public void filterWithId() {
		APIFilter filter = new APIFilter.Builder()
				.hasId("9878973123")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 0);
		Assert.assertEquals(filter.getId(), "9878973123");
	}
	
	@Test
	public void filterWithPath() throws IOException, AppException {
		// For this test, we must simulate API-Manager version >7.7
		APIManagerAdapter.apiManagerVersion = null;
		APIManagerAdapter.getInstance().configAdapter.setAPIManagerTestResponse("{ \"productVersion\": \"7.7.20200130\" }", false);
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/v1/api")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "path");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "/v1/api");
	}
	
	@Test
	public void filterWithPathOn762() throws IOException, AppException {
		// For this test, we must simulate API-Manager version >7.7
		APIManagerAdapter.apiManagerVersion = null;
		APIManagerAdapter.getInstance().configAdapter.setAPIManagerTestResponse("{ \"productVersion\": \"7.6.2 SP4\" }", false);
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/v1/api")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 0);
		Assert.assertEquals(filter.getApiPath(), "/v1/api");
	}
	
	@Test
	public void filterWithName() {
		APIFilter filter = new APIFilter.Builder()
				.hasName("The name I want")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "name");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "The name I want");
	}
	
	@Test
	public void hasFullWildCardName() {
		APIFilter filter = new APIFilter.Builder()
				.hasName("*")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 0);
	}
	
	@Test
	public void filterWithBackendApiID() {
		APIFilter filter = new APIFilter.Builder()
				.hasApiId("7868768768")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "apiid");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "7868768768");
	}
	
	@Test
	public void filterWithDeprecated() {
		APIFilter filter = new APIFilter.Builder()
				.isDeprecated(true)
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "deprecated");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "true");
	}
	
	@Test
	public void filterWithRetired() {
		APIFilter filter = new APIFilter.Builder()
				.isRetired(true)
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "retired");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "true");
	}
	
	@Test
	public void filterWithState() {
		APIFilter filter = new APIFilter.Builder()
				.hasState("unpublished")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "state");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "unpublished");
	}
	
	@Test
	public void apiFilterEqualTest() {
		APIFilter filter1 = new APIFilter.Builder()
				.hasId("12345")
				.build();
		
		APIFilter filter2 = new APIFilter.Builder()
				.hasId("12345")
				.build();
		
		Assert.assertEquals(filter1, filter2, "Both filters should be equal");
	}
	
	@Test
	public void testBackendBasepathFilter() throws AppException {
		APIFilter filter = new APIFilter.Builder()
				.hasBackendBasepath("*emr-system*")
				.build();
		API testAPI = getAPIWithBackendBasepath("http://emr-system:8081");
		assertTrue(filter.filter(testAPI), "API with base-path: http://emr-system:8081 should match to filter: *emr-system*");
		testAPI = getAPIWithBackendBasepath("http://sec.hipaa:8086");
		assertTrue(! filter.filter(testAPI), "API with base-path: http://sec.hipaa:8086 should NOT match to filter: *emr-system*");
		
		filter = new APIFilter.Builder()
				.hasBackendBasepath("http://emr-system:8081")
				.build();
		
		testAPI = getAPIWithBackendBasepath("http://emr-system:8081");
		assertTrue(filter.filter(testAPI), "API with base-path: http://emr-system:8081 should match to filter: http://emr-system:8081");
		
		testAPI = getAPIWithBackendBasepath("http://fhir3.healthintersections.com.au");
		assertTrue(! filter.filter(testAPI), "API with base-path: http://fhir3.healthintersections.com.au should NOT match to filter: http://emr-system:8081");
		
	}
	
	private API getAPIWithBackendBasepath(String basePath) {
		API api = new API();
		ServiceProfile serviceProfile = new ServiceProfile();
		serviceProfile.setBasePath(basePath);
		Map<String, ServiceProfile> serviceProfiles = new HashMap<String, ServiceProfile>();
		serviceProfiles.put("_default", serviceProfile);
		api.setServiceProfiles(serviceProfiles);
		return api;
	}
}

