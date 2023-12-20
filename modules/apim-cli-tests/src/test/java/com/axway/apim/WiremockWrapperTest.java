package com.axway.apim;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.*;

public class WiremockWrapperTest {

    @Test
    public void testInitWiremock() {
        WiremockWrapper wiremockWrapper = new WiremockWrapper();
        wiremockWrapper.initWiremock();
        try {
            new Socket("localhost", 8075);
        } catch (UnknownHostException e) {
            Assert.fail("Unable to connect to mock server", e);
        } catch (IOException e) {
            Assert.fail("Unable to connect to mock server", e);
        }
    }

    @Test(expectedExceptions = {ConnectException.class, UnknownHostException.class})
    public void testClose() throws IOException {
        WiremockWrapper wiremockWrapper = new WiremockWrapper();
        wiremockWrapper.initWiremock();
        wiremockWrapper.close();

        new Socket("localhost", 8075);

    }
}
