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

		APIManagerAdapter.apiManagerVersion = "7.6.2";
		ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
		Assert.assertNotNull(ignoreFields);
		// 7.6.2 doesn't support all of the new fields
		Assert.assertEquals(ignoreFields.length, 13);
	}
	
	@Test
	public void testAPIManager77March2021Config() throws IOException {
		APIManagerConfigAdapter configAdapter = new APIManagerConfigAdapter();
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("apimanager/config/apimanager-config-77-2021-March.json"));
		configAdapter.setAPIManagerTestResponse(apiManagerResponse, true);
		configAdapter.getConfig(true);
	}
}
