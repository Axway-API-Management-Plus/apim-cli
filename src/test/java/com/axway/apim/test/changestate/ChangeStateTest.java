package com.axway.apim.test.changestate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.api.APIManagerAPI;
import com.axway.apim.swagger.api.IAPIDefinition;

public class ChangeStateTest {

  @Test
  public void testOrderMakesNoChange() throws AppException, IOException {
	  IAPIDefinition importAPI = getTestAPI();
	  IAPIDefinition managerAPI = getTestAPI();

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
	  IAPIDefinition importAPI = getTestAPI();
	  IAPIDefinition managerAPI = getTestAPI();
	  
	  ((APIManagerAPI)importAPI).setVhost("abc.xyz.com");
	  ((APIManagerAPI)managerAPI).setVhost("123.xyz.com");
	  
	  APIChangeState changeState = new APIChangeState(managerAPI, importAPI);
	  Assert.assertEquals(changeState.isBreaking(), true);
  }
  
  private static IAPIDefinition getTestAPI() throws AppException {
	  IAPIDefinition testAPI = new APIManagerAPI();
	  testAPI.setValid(true);
	  testAPI.setState(IAPIDefinition.STATE_PUBLISHED);
	  return testAPI;
  }
  
}
