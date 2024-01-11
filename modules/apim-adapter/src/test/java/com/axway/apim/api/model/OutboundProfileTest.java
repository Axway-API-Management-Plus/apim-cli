package com.axway.apim.api.model;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class OutboundProfileTest extends WiremockWrapper {

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
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());
        OutboundProfile profile1 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ConfiguredOutboundProfile1.json"), OutboundProfile.class);
        OutboundProfile profile3 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ConfiguredOutboundProfile1.json"), OutboundProfile.class);
        OutboundProfile profile2 = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "ConfiguredOutboundProfile2.json"), OutboundProfile.class);
        Assert.assertNotEquals(profile2, profile1, "Both profiles are different");
        Assert.assertEquals(profile3, profile1, "Both profiles are the same");
    }
    @Test
    public void testWithCustomRequestPolicy() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setRequestPolicy(null);
        desiredProfile.setRequestPolicy(new Policy("My custom request policy"));
        Assert.assertNotEquals(desiredProfile, actualProfile, "Outbound profiles must be different");
    }

    @Test
    public void testWithCustomResponsePolicy() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setResponsePolicy(null);
        desiredProfile.setResponsePolicy(new Policy("My custom response policy"));
        Assert.assertNotEquals(desiredProfile, actualProfile, "Outbound profiles must be different");
    }

    @Test
    public void testWithCustomSameResponsePolicy() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setResponsePolicy(new Policy("My custom response policy"));
        desiredProfile.setResponsePolicy(new Policy("My custom response policy"));
        Assert.assertEquals(desiredProfile, actualProfile, "Outbound profiles must be equal");
    }

    @Test
    public void testWithCustomRoutePolicy() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setRoutePolicy(null);
        desiredProfile.setRoutePolicy(new Policy("My custom routing policy"));
        Assert.assertNotEquals(desiredProfile, actualProfile, "Outbound profiles must be different");
    }

    @Test
    public void testWithCustomSameRoutingPolicy() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setRoutePolicy(new Policy("My custom routing policy"));
        desiredProfile.setRoutePolicy(new Policy("My custom routing policy"));
        Assert.assertEquals(desiredProfile, actualProfile, "Outbound profiles must be equal");
    }

    @Test
    public void testWithCustomFaultHandlerPolicy() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setFaultHandlerPolicy(null);
        desiredProfile.setFaultHandlerPolicy(new Policy("My custom fault handler policy"));
        Assert.assertNotEquals(desiredProfile, actualProfile, "Outbound profiles must be different");
    }

    @Test
    public void testWithCustomSameFaultHandlerPolicy() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setFaultHandlerPolicy(new Policy("My custom fault handler policy"));
        desiredProfile.setFaultHandlerPolicy(new Policy("My custom fault handler policy"));
        Assert.assertEquals(desiredProfile, actualProfile, "Outbound profiles must be equal");
    }

    @Test
    public void testWithDifferentAuthNProfile() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setAuthenticationProfile("Passthrough");
        desiredProfile.setAuthenticationProfile("Another profile");
        Assert.assertNotEquals(desiredProfile, actualProfile, "The AuthN-Profile of the outbound profiles are different.");
    }

    @Test
    public void testDefaultValueAuthNProfile() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setAuthenticationProfile(null);
        desiredProfile.setAuthenticationProfile("_default");
        Assert.assertEquals(desiredProfile, actualProfile, "The AuthN-Profile of the outbound profiles are different.");
        actualProfile.setAuthenticationProfile("_default");
        desiredProfile.setAuthenticationProfile("_default");
        Assert.assertEquals(desiredProfile, actualProfile, "The AuthN-Profile of the outbound profiles are different.");
        actualProfile.setAuthenticationProfile("_default");
        desiredProfile.setAuthenticationProfile(null);
        Assert.assertEquals(desiredProfile, actualProfile, "The AuthN-Profile of the outbound profiles are different.");
        actualProfile.setAuthenticationProfile(null);
        desiredProfile.setAuthenticationProfile(null);
        Assert.assertEquals(desiredProfile, actualProfile, "The AuthN-Profile of the outbound profiles are different.");
    }

    @Test
    public void testWithDifferentARouteType() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setRouteType("proxy");
        desiredProfile.setRouteType("policy");
        Assert.assertNotEquals(desiredProfile, actualProfile, "The route type of the outbound profiles are equals.");
    }

    @Test
    public void testDefaultValueRoute() {
        OutboundProfile actualProfile = new OutboundProfile();
        OutboundProfile desiredProfile = new OutboundProfile();
        actualProfile.setRouteType(null);
        desiredProfile.setRouteType("proxy");
        Assert.assertEquals(desiredProfile, actualProfile, "The route type of the outbound profiles are different.");
        actualProfile.setRouteType("proxy");
        desiredProfile.setRouteType("proxy");
        Assert.assertEquals(desiredProfile, actualProfile, "The route type of the outbound profiles are different.");
        actualProfile.setRouteType("proxy");
        desiredProfile.setRouteType(null);
        Assert.assertEquals(desiredProfile, actualProfile, "The Aroute type of the outbound profiles are different.");
        actualProfile.setRouteType(null);
        desiredProfile.setRouteType(null);
        Assert.assertEquals(desiredProfile, actualProfile, "The route type of the outbound profiles are different.");
        actualProfile.setRouteType("");
        desiredProfile.setRouteType("");
        Assert.assertEquals(desiredProfile, actualProfile, "The route type of the outbound profiles are different.");
        actualProfile.setRouteType("policy");
        desiredProfile.setRouteType("policy");
        Assert.assertEquals(desiredProfile, actualProfile, "The route type of the outbound profiles are different.");
        actualProfile.setRouteType("policy");
        desiredProfile.setRouteType("proxy");
        Assert.assertNotEquals(desiredProfile, actualProfile, "The route type of the outbound profiles are equals.");
        actualProfile.setRoutePolicy(new Policy("My custom routing policy"));
        Assert.assertEquals(actualProfile.getRouteType(), "policy", "The route type is policy");
        actualProfile.setRouteType(null);
        actualProfile.setRoutePolicy(null);
        Assert.assertEquals(actualProfile.getRouteType(), "proxy", "The route type is proxy");
    }
}
