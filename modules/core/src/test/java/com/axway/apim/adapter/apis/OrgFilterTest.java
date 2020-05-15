package com.axway.apim.adapter.apis;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class OrgFilterTest {
	@Test
	public void emptyOrgFilter() {
		OrgFilter filter1 = new OrgFilter.Builder().build();
		OrgFilter filter2 = new OrgFilter.Builder().build();
		Map<OrgFilter, String> testMap = new HashMap<OrgFilter, String>();
		testMap.put(filter1, "Something");
		testMap.put(filter2, "Something else");
		
		Assert.assertEquals(testMap.size(), 1, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter1), "Something else", "Which filter instance to use should not make a difference");
	}
	
	@Test
	public void orgFilterWithDifferentName() {
		OrgFilter filter1 = new OrgFilter.Builder().hasName("ABC").build();
		OrgFilter filter2 = new OrgFilter.Builder().hasName("XYZ").build();
		Map<OrgFilter, String> testMap = new HashMap<OrgFilter, String>();
		testMap.put(filter1, "Name for ABC");
		testMap.put(filter2, "Name for XYZ");
		
		Assert.assertEquals(testMap.size(), 2, "We expect two entries as the filter is different");
		Assert.assertEquals(testMap.get(filter1), "Name for ABC");
		Assert.assertEquals(testMap.get(filter2), "Name for XYZ");
	}
	
	@Test
	public void orgFilterWithSameName() {
		OrgFilter filter1 = new OrgFilter.Builder().hasName("ABC").build();
		OrgFilter filter2 = new OrgFilter.Builder().hasName("ABC").build();
		Map<OrgFilter, String> testMap = new HashMap<OrgFilter, String>();
		testMap.put(filter1, "Name for ABC");
		testMap.put(filter2, "Name for XYZ");
		
		Assert.assertEquals(testMap.size(), 1, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter2), "Name for XYZ", "XYZ must be returned as it was put last");
	}
	
	@Test
	public void orgFilterWithDifferentId() {
		OrgFilter filter1 = new OrgFilter.Builder().hasId("1234567890").build();
		OrgFilter filter2 = new OrgFilter.Builder().hasId("0987654321").build();
		Map<OrgFilter, String> testMap = new HashMap<OrgFilter, String>();
		testMap.put(filter1, "Name for 1234567890");
		testMap.put(filter2, "Name for 0987654321");
		
		Assert.assertEquals(testMap.size(), 2, "We expect two entries as the filter is different");
		Assert.assertEquals(testMap.get(filter1), "Name for 1234567890");
		Assert.assertEquals(testMap.get(filter2), "Name for 0987654321");
	}
	
	@Test
	public void orgFilterWithSameId() {
		OrgFilter filter1 = new OrgFilter.Builder().hasId("1234567890").build();
		OrgFilter filter2 = new OrgFilter.Builder().hasId("1234567890").build();
		Map<OrgFilter, String> testMap = new HashMap<OrgFilter, String>();
		testMap.put(filter1, "Name for 1234567890");
		testMap.put(filter2, "Name for 0987654321");
		
		Assert.assertEquals(testMap.size(), 1, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter2), "Name for 0987654321", "0987654321 must be returned as it was put last");
	}
	
	@Test
	public void orgFilterWithSameIdAndName() {
		OrgFilter filter1 = new OrgFilter.Builder().hasId("1234567890").hasName("ABC").build();
		OrgFilter filter2 = new OrgFilter.Builder().hasId("1234567890").hasName("ABC").build();
		Map<OrgFilter, String> testMap = new HashMap<OrgFilter, String>();
		testMap.put(filter1, "Name for 1234567890");
		testMap.put(filter2, "Name for 0987654321");
		
		Assert.assertEquals(testMap.size(), 1, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter2), "Name for 0987654321", "0987654321 must be returned as it was put last");
	}
	
	@Test
	public void orgFilterWithDifferentIdAndName() {
		OrgFilter filter1 = new OrgFilter.Builder().hasId("1234567890").hasName("ABC").build();
		OrgFilter filter2 = new OrgFilter.Builder().hasId("0987654321").hasName("XYZ").build();
		Map<OrgFilter, String> testMap = new HashMap<OrgFilter, String>();
		testMap.put(filter1, "Name for 1234567890");
		testMap.put(filter2, "Name for 0987654321");
		
		Assert.assertEquals(testMap.size(), 2, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter1), "Name for 1234567890");
		Assert.assertEquals(testMap.get(filter2), "Name for 0987654321");
	}
	
	@Test
	public void orgFilterWithDifferentIdSameName() {
		OrgFilter filter1 = new OrgFilter.Builder().hasId("1234567890").hasName("ABC").build();
		OrgFilter filter2 = new OrgFilter.Builder().hasId("0987654321").hasName("ABC").build();
		Map<OrgFilter, String> testMap = new HashMap<OrgFilter, String>();
		testMap.put(filter1, "Name for 1234567890");
		testMap.put(filter2, "Name for 0987654321");
		
		Assert.assertEquals(testMap.size(), 2, "We expect only as the filter is the same");
		Assert.assertEquals(testMap.get(filter1), "Name for 1234567890");
		Assert.assertEquals(testMap.get(filter2), "Name for 0987654321");
	}

}
