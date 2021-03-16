package com.axway.apim.api.export.lib;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.axway.apim.api.API;

public class APIComparatorTest {
	@Test
	public void testCompareAPIWithoutVersion() {
		APIComparator comp = new APIComparator();
		API api1 = new API();
		api1.setName("API 1");
		api1.setVersion("1.0.0");

		API api2 = new API();
		api2.setName("API 1");
		
		// Should not lead to a NPE!
		int rc = comp.compare(api1, api2);
		assertEquals(rc, 0);
	}
}
