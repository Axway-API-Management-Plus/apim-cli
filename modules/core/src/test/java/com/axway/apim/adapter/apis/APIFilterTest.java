package com.axway.apim.adapter.apis;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter.Builder.Type;

public class APIFilterTest {
	@Test
	public void testStandardActualAPI() {
		APIFilter filter = new APIFilter.Builder(Type.ACTUAL_API).build();
		Assert.assertEquals(filter.isIncludeClientApplications(), true);
		Assert.assertEquals(filter.isIncludeClientOrganizations(), true);
		Assert.assertEquals(filter.isIncludeQuotas(), true);
		Assert.assertEquals(filter.isIncludeOriginalAPIDefinition(), true);
	}
	
	@Test
	public void testCustomActualAPI() {
		APIFilter filter = new APIFilter.Builder(Type.ACTUAL_API)
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
	public void filterWithPath() throws IOException {
		// For this test, we must simulate API-Manager version >7.7
		APIManagerAdapter.apiManagerVersion = null;
		APIManagerAdapter.configAdapter.setAPIManagerTestResponse("{ \"productVersion\": \"7.7.20200130\" }", false);
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/v1/api")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "path");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "/v1/api");
	}
	
	@Test
	public void filterWithPathOn762() throws IOException {
		// For this test, we must simulate API-Manager version >7.7
		APIManagerAdapter.apiManagerVersion = null;
		APIManagerAdapter.configAdapter.setAPIManagerTestResponse("{ \"productVersion\": \"7.6.2 SP4\" }", false);
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
}
