package com.axway.apim.api.model;

import java.io.IOException;
import java.util.List;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.type.TypeReference;

public class RemoteHostTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    private ObjectMapper mapper = new ObjectMapper();


    private String remoteHostResponse = "[\n" +
            "  {\n" +
            "    \"id\": \"69d89087-7bf7-4e05-916c-9a61a2af0925\",\n" +
            "    \"alias\": \"Host-ABC\",\n" +
            "    \"name\": \"host.abc.com\",\n" +
            "    \"port\": 8081,\n" +
            "    \"createdOn\": 1603792803423,\n" +
            "    \"createdBy\": \"f60e3e05-cdf3-4b70-affc-4cb61a10f4bb\",\n" +
            "    \"organizationId\": \"d9ea6280-8811-4baf-8b5b-011a97142840\",\n" +
            "    \"maxConnections\": -1,\n" +
            "    \"allowHTTP11\": false,\n" +
            "    \"includeContentLengthRequest\": false,\n" +
            "    \"includeContentLengthResponse\": false,\n" +
            "    \"offerTLSServerName\": false,\n" +
            "    \"verifyServerHostname\": true,\n" +
            "    \"connectionTimeout\": 30000,\n" +
            "    \"activeTimeout\": 30000,\n" +
            "    \"transactionTimeout\": 240000,\n" +
            "    \"idleTimeout\": 15000,\n" +
            "    \"maxReceiveBytes\": 20971520,\n" +
            "    \"maxSendBytes\": 20971520,\n" +
            "    \"inputBufferSize\": 8192,\n" +
            "    \"outputBufferSize\": 8192,\n" +
            "    \"addressCacheTimeout\": 300000,\n" +
            "    \"sslSessionCacheSize\": 32,\n" +
            "    \"inputEncodings\": [\n" +
            "      \".inherit\"\n" +
            "    ],\n" +
            "    \"outputEncodings\": [\n" +
            "      \".inherit\"\n" +
            "    ],\n" +
            "    \"exportCorrelationId\": true,\n" +
            "    \"loadBalancing\": \"responseTime\",\n" +
            "    \"responseTimeDecay\": 60000,\n" +
            "    \"addressing\": {\n" +
            "      \"highestPriority\": [\n" +
            "        \"host123.com\",\n" +
            "        \"host456.com\"\n" +
            "      ],\n" +
            "      \"highPriority\": [\n" +
            "        \n" +
            "      ],\n" +
            "      \"mediumPriority\": [\n" +
            "        \n" +
            "      ],\n" +
            "      \"lowestPriority\": [\n" +
            "        \n" +
            "      ]\n" +
            "    },\n" +
            "    \"watchdog\": {\n" +
            "      \"responseCodes\": [\n" +
            "        {\n" +
            "          \"start\": 300,\n" +
            "          \"end\": 999\n" +
            "        },\n" +
            "        {\n" +
            "          \"start\": 100,\n" +
            "          \"end\": 200\n" +
            "        }\n" +
            "      ],\n" +
            "      \"httpRequest\": {\n" +
            "        \"method\": \"OPTIONS\",\n" +
            "        \"uri\": \"*\"\n" +
            "      },\n" +
            "      \"pollTimeout\": 60000,\n" +
            "      \"pollIfUp\": false\n" +
            "    }\n" +
            "  }\n" +
            "]";


    @Test
    public void createRemoteHost() throws IOException {
        APIManagerAdapter.deleteInstance();
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());
        APIManagerAdapter.getInstance();
        List<RemoteHost> remoteHosts = mapper.readValue(remoteHostResponse, new TypeReference<List<RemoteHost>>() {
        });
        Assert.assertEquals(remoteHosts.size(), 1, "Expected one remote host");
    }
}