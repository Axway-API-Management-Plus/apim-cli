package com.axway.apim.config;

import com.axway.apim.api.API;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;

public class GenerateTemplateTest {

    @Test
    public void testDownloadCertificates() throws IOException, CertificateEncodingException, NoSuchAlgorithmException, KeyManagementException {
        GenerateTemplate generateTemplate = new GenerateTemplate();
        API api = new API();
        generateTemplate.downloadCertificates(api, "config.json", "https://petstore3.swagger.io/api/v3/openapi.json");
        Assert.assertEquals(3, api.getCaCerts().size());
    }
}
