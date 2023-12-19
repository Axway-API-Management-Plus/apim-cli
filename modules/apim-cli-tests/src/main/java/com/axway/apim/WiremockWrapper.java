package com.axway.apim;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WiremockWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(WiremockWrapper.class);

    WireMockServer wireMockServer;


    public void initWiremock() {

        wireMockServer = new WireMockServer(options().httpsPort(8075).jettyIdleTimeout(30000L).jettyStopTimeout(10000L).httpDisabled(true)
            .templatingEnabled(false)
            .notifier(new ConsoleNotifier(true))
            .usingFilesUnderClasspath("wiremock_apim"));
        System.setProperty("http.keepAlive", "false");
        wireMockServer.start();
        LOG.info("Wiremock server started");
    }

    public void close() {
        wireMockServer.stop();
        LOG.info("Wiremock server stopped");
    }
}
