package com.axway.apim.api.export.impl;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.lib.utils.TestIndicator;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WiremockTest extends WiremockWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(WiremockTest.class);

    WireMockServer wireMockServer;

    @BeforeClass
    public void initWiremock() {
//        TestIndicator.getInstance().setTestRunning(true);
//        wireMockServer = new WireMockServer(options().httpsPort(8075).usingFilesUnderDirectory(this.getClass().getResource("/").getPath()));
//        wireMockServer.start();
//        LOG.info("Wiremock server started");
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
        //wireMockServer.stop();
    }
}
