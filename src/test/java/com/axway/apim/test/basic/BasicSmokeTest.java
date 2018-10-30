package com.axway.apim.test.basic;

import org.testng.annotations.Test;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;

@Test
public class BasicSmokeTest extends AbstractTestNGCitrusTest {

    @CitrusXmlTest(name = "BasicSmokeTest")
    public void myFirstTest() {
    }
}