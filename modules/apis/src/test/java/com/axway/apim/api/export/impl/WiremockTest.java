package com.axway.apim.api.export.impl;

import com.axway.apim.WiremockWrapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class WiremockTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }
}
