package com.axway.apim.organization.adapter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.testng.annotations.Test;

import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.adapter.JSONOrgAdapter;

public class JSONOrganizationAdapterTest {
	
	private static final String testPackage = "/com/axway/apim/organization/adapter";
	
	@Test
	public void readSingleOrgTest() throws AppException {
		String testFile = JSONOrganizationAdapterTest.class.getResource(testPackage + "/SingleOrganization.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		JSONOrgAdapter adapter = new JSONOrgAdapter();
		adapter.readConfig(testFile);
		List<Organization> orgs = adapter.getOrganizations();
		assertEquals(orgs.size(), 1, "Expected 1 app returned from the Adapter");
	}
	
	@Test
	public void readManyOrgsTest() throws AppException {
		String testFile = JSONOrganizationAdapterTest.class.getResource(testPackage + "/OrganizationArray.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		JSONOrgAdapter adapter = new JSONOrgAdapter();
		adapter.readConfig(testFile);
		List<Organization> orgs = adapter.getOrganizations();
		assertEquals(orgs.size(), 2, "Expected 2 apps returned from the Adapter");
	}
}