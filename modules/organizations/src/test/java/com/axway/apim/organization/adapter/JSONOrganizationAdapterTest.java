package com.axway.apim.organization.adapter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.testng.annotations.Test;

import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.adapter.JSONOrgAdapter;
import com.axway.apim.organization.lib.OrgImportParams;

public class JSONOrganizationAdapterTest {
	
	private static final String testPackage = "/com/axway/apim/organization/adapter";
	
	@Test
	public void readSingleOrgTest() throws AppException {
		String testFile = JSONOrganizationAdapterTest.class.getResource(testPackage + "/SingleOrganization.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		OrgImportParams importParams = new OrgImportParams();
		importParams.setConfig(testFile);
		
		JSONOrgAdapter adapter = new JSONOrgAdapter(importParams);
		List<Organization> orgs = adapter.getOrganizations();
		assertEquals(orgs.size(), 1, "Expected 1 org returned from the Adapter");
		Organization org = orgs.get(0);
		assertNotNull(org.getImage(), "Organization should have an image attached");
	}
	
	@Test
	public void readManyOrgsTest() throws AppException {
		String testFile = JSONOrganizationAdapterTest.class.getResource(testPackage + "/OrganizationArray.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		OrgImportParams importParams = new OrgImportParams();
		importParams.setConfig(testFile);
		
		JSONOrgAdapter adapter = new JSONOrgAdapter(importParams);
		List<Organization> orgs = adapter.getOrganizations();
		assertEquals(orgs.size(), 2, "Expected 2 apps returned from the Adapter");
	}
	
	@Test(expectedExceptions = AppException.class)
	public void readManyOrgsWithStageTest() throws AppException {
		String testFile = JSONOrganizationAdapterTest.class.getResource(testPackage + "/OrganizationArray.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		OrgImportParams importParams = new OrgImportParams();
		importParams.setConfig(testFile);
		importParams.setStage("test-stage");
		
		JSONOrgAdapter adapter = new JSONOrgAdapter(importParams);
		// Stage for a list of organizations is not supported!
		adapter.getOrganizations();
	}
	
	@Test
	public void readOrgWithStageTest() throws AppException {
		String testFile = JSONOrganizationAdapterTest.class.getResource(testPackage + "/SingleOrganization.json").getPath();
		assertTrue(new File(testFile).exists(), "Test file doesn't exists");
		OrgImportParams importParams = new OrgImportParams();
		importParams.setConfig(testFile);
		importParams.setStage("test-stage");
		
		JSONOrgAdapter adapter = new JSONOrgAdapter(importParams);
		List<Organization> orgs = adapter.getOrganizations();
		assertEquals(orgs.size(), 1, "Expected 1 org returned from the Adapter");
		Organization org = orgs.get(0);
		assertNotNull(org.getName(), "API Development TEST-Stage");		
	}
}