package com.axway.lib.utils.rest;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import org.apache.http.Header;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class APIMHttpClientTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void splitStringToHttpHeaders() throws AppException {
        StandardImportParams coreParameters = new StandardImportParams();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("apiadmin");
        coreParameters.setPassword(" ");
        String customHeader = "cf-access-token:xyz133";
        APIMHttpClient apimHttpClient = APIMHttpClient.getInstance();
        List<Header> headers = apimHttpClient.splitStringToHttpHeaders(customHeader);
        Assert.assertEquals(headers.size(), 1);
        Assert.assertEquals(headers.get(0).getName(), "cf-access-token");
        Assert.assertEquals(headers.get(0).getValue(), "xyz133");

    }
}
