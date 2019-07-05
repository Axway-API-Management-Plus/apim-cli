package com.axway.apim.test.basic;

import java.lang.reflect.Field;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.swagger.APIManagerAdapter;

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
	public void isVersionWithAPIManager762() throws Exception {
		setAPIManagerVersion("7.6.2");
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP2"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP10"));
		
		setAPIManagerVersion("7.6.2 SP3");
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP2"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP10"));
	}
	
	@Test
	public void isVersionWithAPIManager753() throws Exception {
		setAPIManagerVersion("7.5.3");
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.6.2"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP2"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP10"));
		
		setAPIManagerVersion("7.5.3 SP9");
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.6.2"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP2"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP10"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP9"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP8"));
	}
	
	private void setAPIManagerVersion(String managerVersion) throws Exception {
		Field field = APIManagerAdapter.class.getDeclaredField("apiManagerVersion");
		field.setAccessible(true);
		field.set(null, managerVersion);
	}
}
