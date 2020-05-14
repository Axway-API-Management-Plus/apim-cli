package com.axway.apim.adapter.apis;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIFilter.Builder.Type;

public class APIFilterTest {
	@Test
	public void testStandardActualAPI() {
		APIFilter filter = new APIFilter.Builder(Type.ACTUAL_API).build();
		Assert.assertEquals(filter.includeClientApplications, true);
		Assert.assertEquals(filter.includeClientOrganizations, true);
		Assert.assertEquals(filter.includeQuotas, true);
	}
	
	@Test
	public void testCustomActualAPI() {
		APIFilter filter = new APIFilter.Builder(Type.ACTUAL_API)
				.includeClientApplications(false)
				.includeClientOrganizations(false)
				.includeQuotas(false)
				.build();
		Assert.assertEquals(filter.includeClientApplications, false);
		Assert.assertEquals(filter.includeClientOrganizations, false);
		Assert.assertEquals(filter.includeQuotas, false);
	}
}
