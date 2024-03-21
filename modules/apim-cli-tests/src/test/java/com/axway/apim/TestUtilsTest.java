package com.axway.apim;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.citrusframework.context.TestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

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

    @Test
    public void createTestConfigReplaceVariables() throws IOException {
        TestContext testContext = new TestContext();
        testContext.setVariable("apiName", "abc");
        testContext.setVariable("apiPath", "/api");
        testContext.setVariable("state", "published");
        String replacedConfig = TestUtils.createTestConfig("api-config.json", testContext, "apis", true);
        DocumentContext documentContext = JsonPath.parse(new File(replacedConfig));
        Assert.assertEquals(documentContext.read("$.name", String.class), "abc");
        Assert.assertEquals(documentContext.read("$.path"), "/api");
        Assert.assertEquals(documentContext.read("$.state"), "published");
    }

    @Test
    public void createTestConfigReplace() throws IOException {
        TestContext testContext = new TestContext();
        testContext.setVariable("apiName", "abc");
        testContext.setVariable("apiPath", "/api");
        testContext.setVariable("state", "published");
        String replacedConfig = TestUtils.createTestConfig("api-config.json", testContext, "apis", false);
        DocumentContext documentContext = JsonPath.parse(new File(replacedConfig));
        Assert.assertEquals(documentContext.read("$.name", String.class), "${apiName}");

    }

    @Test
    public void copyTestAssets(){
        TestContext testContext = new TestContext();
        File file = TestUtils.createTestDirectory("appstest");
        String replacedConfig = TestUtils.createTestConfig("api-config.json", testContext, "apis", false);
        TestUtils.copyTestAssets(new File(replacedConfig).getParentFile(), file);
        Optional<File> optionalFile = Arrays.stream(file.listFiles()).filter(file1 -> file1.getName().equals("axway.png")).findAny();
        Assert.assertTrue(optionalFile.isPresent());
    }
}
