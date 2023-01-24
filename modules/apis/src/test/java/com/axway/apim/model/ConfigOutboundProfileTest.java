package com.axway.apim.model;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class ConfigOutboundProfileTest extends WiremockWrapper {

	@BeforeClass
	public void initWiremock() {
		super.initWiremock();
	}

	@AfterClass
	public void close() {
		super.close();
	}
	
	private static final String testPackage = "com/axway/apim/model/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testProfilesEquality() throws IOException {
		APIManagerAdapter.deleteInstance();
		CoreParameters coreParameters = new CoreParameters();
		coreParameters.setHostname("localhost");
		coreParameters.setUsername("test");
		coreParameters.setPassword(Utils.getEncryptedPassword());
		OutboundProfile profile1 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ConfiguredOutboundProfile1.json"), OutboundProfile.class);
		OutboundProfile profile3 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ConfiguredOutboundProfile1.json"), OutboundProfile.class);
		OutboundProfile profile2 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ConfiguredOutboundProfile2.json"), OutboundProfile.class);
		Assert.assertFalse(profile1.equals(profile2), "Both profiles are different");
		Assert.assertTrue(profile1.equals(profile3), "Both profiles are the same");
	}
}
