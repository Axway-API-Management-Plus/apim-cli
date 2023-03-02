package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class APIManagerAPIMethodAdapterTest extends WiremockWrapper {

	private APIManagerAPIMethodAdapter methodAdapter;

	@BeforeClass
	public void init() {
		try {
			super.initWiremock();
			CoreParameters coreParameters = new CoreParameters();
			coreParameters.setHostname("localhost");
			coreParameters.setUsername("apiadmin");
			coreParameters.setPassword(Utils.getEncryptedPassword());
			APIManagerAdapter.deleteInstance();
			methodAdapter = APIManagerAdapter.getInstance().methodAdapter;
		} catch (AppException e) {
			throw new RuntimeException(e);
		}
	}

	@AfterClass
	public void close() {
		super.close();
	}
	


	
	@Test
	public void testGetAllAPIMethods() throws IOException {
		List<APIMethod> methods = methodAdapter.getAllMethodsForAPI("e4ded8c8-0a40-4b50-bc13-552fb7209150");

		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(methods.size(), 19, "Expected 19 APIMethods");
		APIMethod method = methods.get(0);
		
		Assert.assertEquals(method.getName(), "logoutUser");
		Assert.assertEquals(method.getSummary(), "Logs out current logged in user session");
	}
	
	@Test
	public void testGetMethodForName() throws IOException {
		APIMethod method = methodAdapter.getMethodForName("e4ded8c8-0a40-4b50-bc13-552fb7209150", "deletePet");
		Assert.assertEquals(method.getName(), "deletePet");
	}

	@Test
	public void updateApiMethod(){
		try {
			APIMethod method = methodAdapter.getMethodForName("e4ded8c8-0a40-4b50-bc13-552fb7209150", "deletePet");
			Assert.assertEquals(method.getName(), "deletePet");
			methodAdapter.updateApiMethod(method);
		} catch (AppException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
