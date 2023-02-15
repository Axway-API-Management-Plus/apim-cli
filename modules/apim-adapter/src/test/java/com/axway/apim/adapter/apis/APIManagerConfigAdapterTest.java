package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.IOException;

public class APIManagerConfigAdapterTest {
	
	@Test
	public void testCorrectFieldsAreIgnored() {
		APIManagerAdapter.apiManagerVersion = "7.7.20200930";
		String[] ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
		// No field should be ignored for the Sept-Release
		Assert.assertNotNull(ignoreFields);
		Assert.assertEquals(ignoreFields.length, 0);

		APIManagerAdapter.apiManagerVersion = "7.7.20200730";
		ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
		// Same for July, as fields have been added in January release
		Assert.assertNotNull(ignoreFields);
		Assert.assertEquals(ignoreFields.length, 0);

		APIManagerAdapter.apiManagerVersion = "7.7.20200331";
		ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
		// March release doesn't support new parameters introduced with the May release
		Assert.assertNotNull(ignoreFields);
		Assert.assertEquals(ignoreFields.length, 4);

		APIManagerAdapter.apiManagerVersion = "7.7.0";
		ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
		// 7.7.0 plain already supports some new settings
		Assert.assertNotNull(ignoreFields);
		Assert.assertEquals(ignoreFields.length, 4); // Only brand new fields are returned to be ignored

	}
}
