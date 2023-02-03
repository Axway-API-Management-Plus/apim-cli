package com.axway.apim.test.basic;

import java.lang.reflect.Field;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;

public class ManagerVersionCheckTest {
	
	@Test
	public void isVersionWithAPIManager77() throws Exception {
		setAPIManagerVersion("7.7.0");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8 SP1"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP10"));
	}

	
	@Test
	public void isVersionWithAPIManager7720200130() throws Exception {
		setAPIManagerVersion("7.7.20200130");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7"), "Failed with requested version 7.7");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1"), "Failed with requested version 7.7 SP1");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7 SP2"), "Failed with requested version 7.7 SP2");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7.20200130"), "Failed with requested version 7.7.20200130");
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7.20200330"), "Failed with requested version 7.7.20200330"); // Should fail, as the requested version is from March release
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3"), "Failed with requested version 7.5.3");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2"), "Failed with requested version 7.6.2");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP2"), "Failed with requested version 7.6.2 SP2");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3"), "Failed with requested version 7.6.2 SP3");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP10"), "Failed with requested version 7.5.3 SP10");
	}
	
	@Test
	public void isVersionWithAPIManager7720200331() throws Exception {
		setAPIManagerVersion("7.7.20200331");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7"), "Failed with requested version 7.7");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1"), "Failed with requested version 7.7 SP1");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7 SP2"), "Failed with requested version 7.7 SP2");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7.20200130"), "Failed with requested version 7.7.20200130");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7.20200331"), "Failed with requested version 7.7.20200331");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3"), "Failed with requested version 7.5.3");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2"), "Failed with requested version 7.6.2");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP2"), "Failed with requested version 7.6.2 SP2");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3"), "Failed with requested version 7.6.2 SP3");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP10"), "Failed with requested version 7.5.3 SP10");
	}
	
	
	private void setAPIManagerVersion(String managerVersion) throws Exception {
		Field field = APIManagerAdapter.class.getDeclaredField("apiManagerVersion");
		field.setAccessible(true);
		field.set(null, managerVersion);
	}
}
