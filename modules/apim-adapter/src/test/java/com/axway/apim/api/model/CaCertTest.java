package com.axway.apim.api.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CaCertTest {

    private Logger logger = LoggerFactory.getLogger(CaCertTest.class);

    @Test
    public void testCertCommonNameWithClash(){
        CaCert caCert = new CaCert();
        caCert.setAlias("CN=Autorité de Certification Racine ANTS/A V3 ou encore CN=IGC/A AC racine Etat Français");
        String fileName = caCert.getCertFile();
        logger.info("Filename : {}", fileName);
        Assert.assertTrue(!fileName.contains("/"));
    }


}
