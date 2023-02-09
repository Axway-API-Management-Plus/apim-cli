package com.axway.apim;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WiremockWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(WiremockWrapper.class);

    WireMockServer wireMockServer;


    public void initWiremock(){

        wireMockServer = new WireMockServer(options().httpsPort(8075).usingFilesUnderClasspath("wiremock_apim"));
        wireMockServer.start();
        LOG.info("Wiremock server started");
    }

    public void close() {
        wireMockServer.stop();
    }
}
