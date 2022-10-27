package com.axway.apim.api.model;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CaCertTest {

    @Test
    public void testCertCommonNameWithClash(){
        CaCert caCert = new CaCert();
        caCert.setAlias("CN=AutoritédeCertificationRacineANTS/AV3.crt");
        String fileName = caCert.getCertFile();
        Assert.assertTrue(!fileName.contains("/"));

    }
}
