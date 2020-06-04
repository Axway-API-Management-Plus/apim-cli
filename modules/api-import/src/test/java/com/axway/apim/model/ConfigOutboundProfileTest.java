package com.axway.apim.model;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigOutboundProfileTest extends APIManagerMockBase {
	
	private static final String testPackage = "com/axway/apim/model/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	private void initTestIndicator() throws AppException, IOException {
		setupMockData();
	}
	
	@Test
	public void testProfilesEquality() throws JsonParseException, JsonMappingException, IOException {
		OutboundProfile profile1 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ConfiguredOutboundProfile1.json"), OutboundProfile.class);
		OutboundProfile profile3 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ConfiguredOutboundProfile1.json"), OutboundProfile.class);
		
		OutboundProfile profile2 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ConfiguredOutboundProfile2.json"), OutboundProfile.class);
		
		Assert.assertFalse(profile1.equals(profile2), "Both profiles are different");
		Assert.assertTrue(profile1.equals(profile3), "Both profiles are the same");
	}
}
