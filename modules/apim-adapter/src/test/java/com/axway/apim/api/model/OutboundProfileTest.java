package com.axway.apim.api.model;

import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.error.AppException;

import java.util.List;

public class OutboundProfileTest {
	@Test
	public void testWithCustomRequestPolicy() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setRequestPolicy(null);
		desiredProfile.setRequestPolicy(new Policy("My custom request policy"));
		Assert.assertFalse(actualProfile.equals(desiredProfile), "Outbound profiles must be different");
	}

	@Test
	public void testWithCustomResponsePolicy() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setResponsePolicy(null);
		desiredProfile.setResponsePolicy(new Policy("My custom response policy"));
		Assert.assertFalse(actualProfile.equals(desiredProfile), "Outbound profiles must be different");
	}

	@Test
	public void testWithCustomSameResponsePolicy() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setResponsePolicy(new Policy("My custom response policy"));
		desiredProfile.setResponsePolicy(new Policy("My custom response policy"));
		Assert.assertTrue(actualProfile.equals(desiredProfile), "Outbound profiles must be equal");
	}

	@Test
	public void testWithCustomRoutePolicy() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setRoutePolicy(null);
		desiredProfile.setRoutePolicy(new Policy("My custom routing policy"));
		Assert.assertFalse(actualProfile.equals(desiredProfile), "Outbound profiles must be different");
	}

	@Test
	public void testWithCustomSameRoutingPolicy() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setRoutePolicy(new Policy("My custom routing policy"));
		desiredProfile.setRoutePolicy(new Policy("My custom routing policy"));
		Assert.assertTrue(actualProfile.equals(desiredProfile), "Outbound profiles must be equal");
	}

	@Test
	public void testWithCustomFaultHandlerPolicy() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setFaultHandlerPolicy(null);
		desiredProfile.setFaultHandlerPolicy(new Policy("My custom fault handler policy"));
		Assert.assertFalse(actualProfile.equals(desiredProfile), "Outbound profiles must be different");
	}

	@Test
	public void testWithCustomSameFaultHandlerPolicy() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setFaultHandlerPolicy(new Policy("My custom fault handler policy"));
		desiredProfile.setFaultHandlerPolicy(new Policy("My custom fault handler policy"));
		Assert.assertTrue(actualProfile.equals(desiredProfile), "Outbound profiles must be equal");
	}

	@Test
	public void testWithDifferentAuthNProfile() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setAuthenticationProfile("Passthrough");
		desiredProfile.setAuthenticationProfile("Another profile");
		Assert.assertFalse(actualProfile.equals(desiredProfile),
				"The AuthN-Profile of the outbound profiles are different.");
	}

	@Test
	public void testDefaultValueAuthNProfile() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setAuthenticationProfile(null);
		desiredProfile.setAuthenticationProfile("_default");
		Assert.assertTrue(actualProfile.equals(desiredProfile), "The AuthN-Profile of the outbound profiles are different.");
		actualProfile.setAuthenticationProfile("_default");
		desiredProfile.setAuthenticationProfile("_default");
		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The AuthN-Profile of the outbound profiles are different.");
		actualProfile.setAuthenticationProfile("_default");
		desiredProfile.setAuthenticationProfile(null);
		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The AuthN-Profile of the outbound profiles are different.");
		actualProfile.setAuthenticationProfile(null);
		desiredProfile.setAuthenticationProfile(null);
		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The AuthN-Profile of the outbound profiles are different.");
	}

	@Test
	public void testWithDifferentARouteType() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setRouteType("proxy");
		desiredProfile.setRouteType("policy");
		Assert.assertFalse(actualProfile.equals(desiredProfile), "The route type of the outbound profiles are equals.");
	}

	@Test
	public void testDefaultValueRoute() throws AppException {
		OutboundProfile actualProfile = new OutboundProfile();
		OutboundProfile desiredProfile = new OutboundProfile();
		actualProfile.setRouteType(null);
		desiredProfile.setRouteType("proxy");
		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The route type of the outbound profiles are different.");
		actualProfile.setRouteType("proxy");
		desiredProfile.setRouteType("proxy");
		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The route type of the outbound profiles are different.");
		actualProfile.setRouteType("proxy");
		desiredProfile.setRouteType(null);
		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The Aroute type of the outbound profiles are different.");
		actualProfile.setRouteType(null);
		desiredProfile.setRouteType(null);
		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The route type of the outbound profiles are different.");
		actualProfile.setRouteType("");
		desiredProfile.setRouteType("");
		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The route type of the outbound profiles are different.");
		actualProfile.setRouteType("policy");
		desiredProfile.setRouteType("policy");
		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The route type of the outbound profiles are different.");
		actualProfile.setRouteType("policy");
		desiredProfile.setRouteType("proxy");
		Assert.assertFalse(actualProfile.equals(desiredProfile), "The route type of the outbound profiles are equals.");
		actualProfile.setRoutePolicy(new Policy("My custom routing policy"));
		Assert.assertEquals(actualProfile.getRouteType(), "policy", "The route type is policy");
		actualProfile.setRouteType(null);
		actualProfile.setRoutePolicy(null);
		Assert.assertEquals(actualProfile.getRouteType(), "proxy", "The route type is proxy");
	}

    @Test
    public void equalOutboundProfileWithMethod() throws JsonProcessingException {
        String  source = "{\n" +
            "        \"7024b732-4c36-4583-a122-4f2da87d5ff3\": {\n" +
            "            \"apiId\": \"fb7ff6b2-406d-4063-ab0a-9e06d1480ec3\",\n" +
            "            \"apiMethodId\": \"a03014c4-de43-4b9c-be05-10f09e5b33ff\",\n" +
            "            \"authenticationProfile\": \"HTTP Basic outbound Test 193\",\n" +
            "            \"parameters\": [\n" +
            "                {\n" +
            "                    \"additional\": true,\n" +
            "                    \"exclude\": false,\n" +
            "                    \"name\": \"additionalOutboundParam\",\n" +
            "                    \"paramType\": \"header\",\n" +
            "                    \"required\": false,\n" +
            "                    \"type\": \"string\",\n" +
            "                    \"value\": \"Test-Value\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"routeType\": \"proxy\"\n" +
            "        }\n" +
            "    }";

        ObjectMapper objectMapper = new ObjectMapper();
        OutboundProfile outboundProfiles = objectMapper.readValue(source, OutboundProfile.class);
        Assert.assertTrue(Utils.compareValues(outboundProfiles, outboundProfiles));
    }
}
