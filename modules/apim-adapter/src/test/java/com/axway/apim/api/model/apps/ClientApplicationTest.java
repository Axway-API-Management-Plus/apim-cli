package com.axway.apim.api.model.apps;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.api.model.Organization;

public class ClientApplicationTest {
	@Test
	public void appsAreNotEqualWithDiffOrg() {
		ClientApplication app1 = new ClientApplication();
		ClientApplication app2 = new ClientApplication();
		Organization org1 = new Organization();
		Organization org2 = new Organization();
		org1.setName("Org 1");
		org2.setName("Org 2");
		app1.setOrganization(org1);
		app2.setOrganization(org2);
		Assert.assertFalse(app1.equals(app2), "Apps are not equals as the organizations are different.");
	}
}
