package com.axway.apim;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class TestUtilsTest {

    @Test
    public void createTestDirectory(){
        File file = TestUtils.createTestDirectory("apps");
        Assert.assertNotNull(file);
    }

    @Test
    public void createTestExistingDirectory(){
        File file = TestUtils.createTestDirectory("apps");
        Assert.assertNotNull(file);
    }
}
