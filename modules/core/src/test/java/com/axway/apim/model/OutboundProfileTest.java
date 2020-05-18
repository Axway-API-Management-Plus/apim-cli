package com.axway.apim.model;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.api.API;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OutboundProfileTest {
	
	private static final String testPackage = "com/axway/apim/adapter/model/";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testProfilesEquality() throws JsonParseException, JsonMappingException, IOException {
		OutboundProfile profile1 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "APIManagerOutboundProfile1.json"), OutboundProfile.class);
		OutboundProfile profile3 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "APIManagerOutboundProfile1.json"), OutboundProfile.class);
		
		OutboundProfile profile2 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "APIManagerOutboundProfile2.json"), OutboundProfile.class);
		
		Assert.assertFalse(profile1.equals(profile2), "Both profiles are different");
		Assert.assertTrue(profile1.equals(profile3), "Both profiles are the same");
	}
	
	@Test(enabled = false, description = "Validates the internal methodIds of the outbound profiles are translated to external names by the Custom-Deserializer")
	public void testProfileWithMethod() throws JsonParseException, JsonMappingException, IOException, AppException {
		InjectableValues iv = new InjectableValues.Std();
		((InjectableValues.Std) iv).addValue("apiId", "72745ed9-f75b-428c-959c-b483eea497a1");
		mapper.setInjectableValues(iv);
		API api = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "OutboundProfilesWithMethodOverride.json"), new TypeReference<API>(){});
		
		Assert.assertNull(api.getOutboundProfiles().get("getOrderById"), "Profile with method name should have been replaced by method id.");
		// 9a9dc4ff-1c24-42f0-b94c-556ae03ae84d
		System.out.println();
	}
}
