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


    @Test
    public void compareEmptyAPIs() {
        APIComparator comp = new APIComparator();
        int rc = comp.compare(null, null);
        assertEquals(rc, 0);
    }

    @Test
    public void compareAPIsWithEmptyName() {
        APIComparator comp = new APIComparator();
        API api1 = new API();
        API api2 = new API();
        int rc = comp.compare(api1, api2);
        assertEquals(rc, 0);
    }

    @Test
    public void compareAPIsWithEmptyVersion() {
        APIComparator comp = new APIComparator();
        API api1 = new API();
        api1.setName("abc");
        api1.setVersion("1.0.0");
        API api2 = new API();
        api2.setName("abc");
        api2.setVersion("2.0.0");
        int rc = comp.compare(api1, api2);
        assertEquals(rc, -1);
    }


    @Test
    public void compareAPIsWithNameAndVersion() {
        APIComparator comp = new APIComparator();
        API api1 = new API();
        api1.setName("abc");
        API api2 = new API();
        api2.setName("abc");
        int rc = comp.compare(api1, api2);
        assertEquals(rc, 0);
    }

}
