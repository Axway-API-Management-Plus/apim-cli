package com.axway.apim.test.changestate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.api.API;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.apiimport.ActualAPI;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;

public class ChangeStateTest {
	
	@BeforeClass
	public void prepareTest() {
		TestIndicator.getInstance().setTestRunning(true);
	}
	
	@AfterClass
	public void afterTest() {
		TestIndicator.getInstance().setTestRunning(false);
	}	

	@Test
	public void testOrderMakesNoChange() throws AppException, IOException, ParseException {

		API importAPI = getTestAPI();
		API managerAPI = getTestAPI();

		List<Organization> importOrgs = new ArrayList<Organization>();
		List<Organization> managerOrgs = new ArrayList<Organization>();

		
		importOrgs.add(new Organization.Builder().hasName("orgA").hasId("123").build());
		importOrgs.add(new Organization.Builder().hasName("orgB").hasId("456").build());
		importOrgs.add(new Organization.Builder().hasName("orgC").hasId("789").build());

		managerOrgs.add(new Organization.Builder().hasName("orgC").hasId("123").build());
		managerOrgs.add(new Organization.Builder().hasName("orgB").hasId("456").build());
		managerOrgs.add(new Organization.Builder().hasName("orgA").hasId("789").build());

		importAPI.setClientOrganizations(importOrgs);
		importAPI.setOrganization(new Organization.Builder().hasName("123").hasId("789").build());

		managerAPI.setClientOrganizations(managerOrgs);
		managerAPI.setOrganization(new Organization.Builder().hasName("123").hasId("789").build());

		APIChangeState changeState = new APIChangeState(managerAPI, importAPI);
		Assert.assertEquals(changeState.hasAnyChanges(), false);
	}

	@Test
	public void isVhostBreaking() throws Exception {
		API importAPI = getTestAPI();
		API managerAPI = getTestAPI();

		((ActualAPI)importAPI).setVhost("abc.xyz.com");
		((ActualAPI)managerAPI).setVhost("123.xyz.com");

		APIChangeState changeState = new APIChangeState(managerAPI, importAPI);
		Assert.assertEquals(changeState.isBreaking(), true);
	}
	
	@Test
	public void isDesiredStateDeleted() throws Exception {
		API importAPI = getTestAPI();
		API managerAPI = getTestAPI();

		((ActualAPI)importAPI).setState(API.STATE_DELETED);
		((ActualAPI)importAPI).setDescriptionType("ANY-TYPE");
		
		
		((ActualAPI)managerAPI).setState(API.STATE_PUBLISHED);
		((ActualAPI)importAPI).setDescriptionType("ANY-OTHER-TYPE");

		APIChangeState changeState = new APIChangeState(managerAPI, importAPI);
		// As the API should be deleted anyway, desiredChanges should be ignored - The API should just be deleted
		Assert.assertEquals(changeState.getAllChanges().size(), 1, "The state should be included");
		Assert.assertEquals(changeState.getAllChanges().get(0), "state", "The state should be included");
	}

	private static API getTestAPI() throws AppException {
		API testAPI = new ActualAPI();
		testAPI.setOrganization(new Organization.Builder().hasName("123").hasId("123").build());
		testAPI.setState(API.STATE_PUBLISHED);
		return testAPI;
	}

}
