package com.axway.apim.test.changestate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.api.IAPI;
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

		IAPI importAPI = getTestAPI();
		IAPI managerAPI = getTestAPI();

		List<String> importOrgs = new ArrayList<String>();
		List<String> managerOrgs = new ArrayList<String>();

		importOrgs.add("orgA");
		importOrgs.add("orgB");
		importOrgs.add("orgC");

		managerOrgs.add("orgC");
		managerOrgs.add("orgB");
		managerOrgs.add("orgA");

		importAPI.setClientOrganizations(importOrgs);
		importAPI.setOrganizationId("123");

		managerAPI.setClientOrganizations(managerOrgs);
		managerAPI.setOrganizationId("123");

		APIChangeState changeState = new APIChangeState(managerAPI, importAPI);
		Assert.assertEquals(changeState.hasAnyChanges(), false);
	}

	@Test
	public void isVhostBreaking() throws Exception {
		IAPI importAPI = getTestAPI();
		IAPI managerAPI = getTestAPI();

		((ActualAPI)importAPI).setVhost("abc.xyz.com");
		((ActualAPI)managerAPI).setVhost("123.xyz.com");

		APIChangeState changeState = new APIChangeState(managerAPI, importAPI);
		Assert.assertEquals(changeState.isBreaking(), true);
	}

	private static IAPI getTestAPI() throws AppException {
		IAPI testAPI = new ActualAPI();
		testAPI.setOrganizationId("123");
		testAPI.setValid(true);
		testAPI.setState(IAPI.STATE_PUBLISHED);
		return testAPI;
	}

}
