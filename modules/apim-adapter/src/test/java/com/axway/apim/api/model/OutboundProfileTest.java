package com.axway.apim.api.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;

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
}
