package com.axway.apim.test.basic;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ManagerVersionCheckTest extends WiremockWrapper {

	private APIManagerAdapter apiManagerAdapter;

	@BeforeClass
	public void init() {
		try {
			super.initWiremock();
			CoreParameters coreParameters = new CoreParameters();
			coreParameters.setHostname("localhost");
			coreParameters.setUsername("apiadmin");
			coreParameters.setPassword(Utils.getEncryptedPassword());
			APIManagerAdapter.deleteInstance();
			apiManagerAdapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			throw new RuntimeException(e);
		}
	}

	@AfterClass
	public void close() {
		super.close();
	}


	@Test
	public void isVersionWithAPIManager77() {
		apiManagerAdapter.setApiManagerVersion("7.7.0");
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.7"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8"));
		Assert.assertFalse(APIManagerAdapter.hasAPIManagerVersion("7.8 SP1"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.6.2"));
		Assert.assertTrue(APIManagerAdapter.hasAPIManagerVersion("7.5.3 SP10"));
	}

	
	@Test
	public void isVersionWithAPIManager7720200130() {
		apiManagerAdapter.setApiManagerVersion("7.7.20200130");
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
	public void isVersionWithAPIManager7720200331() {
		apiManagerAdapter.setApiManagerVersion("7.7.20200331");
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
}
