package com.axway.apim.adapter.apis;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;

public class APIManagerConfigAdapterTest {
	
	@Test
	public void testCorrectFieldsAreIgnored() {
		APIManagerAdapter.apiManagerVersion = "7.7.20200930";
		String[] ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
		// No field should be ignored for the Sept-Release
		Assert.assertNotNull(ignoreFields);
		Assert.assertEquals(ignoreFields.length, 0);
		
		APIManagerAdapter.apiManagerVersion = "7.6.2";
		ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
		Assert.assertNotNull(ignoreFields);
		// Five config fields, based on all known fields are not supported by an API-Manager 7.6.2
		Assert.assertEquals(ignoreFields.length, 5);
	}
}
