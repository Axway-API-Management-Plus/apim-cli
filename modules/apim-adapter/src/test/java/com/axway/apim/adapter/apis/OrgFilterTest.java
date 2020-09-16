package com.axway.apim.adapter.apis;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;

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
	
	@Test
	public void filterOrgWithName() {
		OrgFilter filter = new OrgFilter.Builder()
				.hasName("My orgname")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "name");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "My orgname");
	}
	
	@Test
	public void filterOrgEnabledTrue() {
		OrgFilter filter = new OrgFilter.Builder()
				.isEnabled(true)
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "enabled");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "enabled");
	}
	
	@Test
	public void testFilterOrgDevelopment() throws AppException {
		OrgFilter orgFilter = new OrgFilter.Builder()
				.hasDevelopment("true")
				.build();
		Organization testOrg = new Organization();
		testOrg.setDevelopment(false);
		Assert.assertTrue(orgFilter.filter(testOrg), "This organization should be filtered as it's a non dev org");

		// Switch organization to be a development organization
		testOrg.setDevelopment(true);
		Assert.assertFalse(orgFilter.filter(testOrg), "A development organization should stay.");
	}
	
	@Test
	public void filterOrgId() {
		OrgFilter filter = new OrgFilter.Builder()
				.hasId("123456789")
				.build();
		Assert.assertEquals(filter.getId(), "123456789");
	}
	
	@Test
	public void filterOrgWithDescription() {
		OrgFilter filter = new OrgFilter.Builder()
				.hasDescription("my description I want")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "description");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "like");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "my description I want");
	}
	
	@Test
	public void filterOrgEqualTest() {
		OrgFilter filter1 = new OrgFilter.Builder()
				.hasId("12345")
				.build();
		
		OrgFilter filter2 = new OrgFilter.Builder()
				.hasId("12345")
				.build();
		
		Assert.assertEquals(filter1, filter2, "Both filters should be equal");
	}
}
