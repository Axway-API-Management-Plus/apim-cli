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
import com.axway.apim.api.IAPI;
import com.axway.apim.api.model.Organization;
import com.axway.apim.apiimport.ActualAPI;
import com.axway.apim.apiimport.state.APIChangeState;
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

		
		importOrgs.add(new Organization().setName("orgA"));
		importOrgs.add(new Organization().setName("orgB"));
		importOrgs.add(new Organization().setName("orgC"));

		managerOrgs.add(new Organization().setName("orgC"));
		managerOrgs.add(new Organization().setName("orgB"));
		managerOrgs.add(new Organization().setName("orgA"));

		importAPI.setClientOrganizations(importOrgs);
		importAPI.setOrganization(new Organization().setName("123"));

		managerAPI.setClientOrganizations(managerOrgs);
		managerAPI.setOrganization(new Organization().setName("123"));

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

	private static API getTestAPI() throws AppException {
		API testAPI = new ActualAPI();
		testAPI.setOrganization(new Organization().setName("123"));
		testAPI.setState(IAPI.STATE_PUBLISHED);
		return testAPI;
	}

}
