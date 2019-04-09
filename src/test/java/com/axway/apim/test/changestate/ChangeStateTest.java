package com.axway.apim.test.changestate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.api.state.ActualAPI;
import com.axway.apim.swagger.api.state.IAPI;
import com.axway.apim.test.lib.TestIndicator;

public class ChangeStateTest {
	
	@BeforeClass
	public void prepareTest() {
		TestIndicator.getInstance().setTestRunning(true);
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

		managerAPI.setClientOrganizations(managerOrgs);

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
		testAPI.setValid(true);
		testAPI.setState(IAPI.STATE_PUBLISHED);
		return testAPI;
	}

}
