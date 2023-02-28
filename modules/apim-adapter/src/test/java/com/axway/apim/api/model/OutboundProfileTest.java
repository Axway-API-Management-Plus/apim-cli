package com.axway.apim.api.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.error.AppException;

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

		Assert.assertTrue(actualProfile.equals(desiredProfile),
				"The AuthN-Profile of the outbound profiles are different.");

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

}