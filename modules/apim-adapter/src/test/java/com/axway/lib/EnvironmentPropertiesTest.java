package com.axway.lib;

import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.error.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class EnvironmentPropertiesTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentPropertiesTest.class);

    @Test
    public void testNoStage() throws AppException {
        EnvironmentProperties properties = new EnvironmentProperties("NOT_SET");
        Assert.assertEquals(properties.containsKey("doesnExists"), false);
        Assert.assertEquals(properties.containsKey("username"), true);
    }

    @Test
    public void testStage() throws AppException {
        EnvironmentProperties properties = new EnvironmentProperties("anyOtherStage");
        Assert.assertEquals(properties.containsKey("thisKeyExists"), true);
    }

    @Test
    public void testNoStageFromConfDir() throws URISyntaxException {
        // A given path should be used to load the Environment-Config file from
        String path = EnvironmentPropertiesTest.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        System.out.println(path);
        path += "envPropertiesTest/swaggerPromoteHome";
        EnvironmentProperties properties = new EnvironmentProperties("fromSwaggerPromoteHome", path);
        Assert.assertEquals(properties.containsKey("thisKeyExists"), true);
        Assert.assertEquals(properties.get("thisKeyExists"), "stageKeyFromSwaggerPromoteHome");
    }

    @Test
    public void testStageFromConfDir() throws URISyntaxException {
        // A given path should be used to load the Environment-Config file from
        String path = EnvironmentPropertiesTest.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        path += "envPropertiesTest/swaggerPromoteHome";
        EnvironmentProperties properties = new EnvironmentProperties("fromSwaggerPromoteHome", path);
        Assert.assertEquals(properties.containsKey("thisKeyExists"), true);
        Assert.assertEquals(properties.get("thisKeyExists"), "stageKeyFromSwaggerPromoteHome");
    }

    @Test
    public void testEnvironmentWithOSEnvVariables() throws IOException {
        // For this test to run, the system must provide the environment properties CI & JAVA_HOME
        EnvironmentProperties properties = new EnvironmentProperties("NOT_SET");
        Assert.assertNotEquals(properties.get("variableFromOSEnvironmentVariable"), "${JAVA_HOME}");
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null) {
            LOG.warn("JAVA_HOME is not set and test is 'testEnvironmentWithOSEnvVariables' is ignored");
            return;
        }
        String CI = System.getenv("CI");
        if (CI == null) {
            LOG.warn("CI is not set and test is 'variablePartiallyFromOSEnvironmentVariable' is ignored");
            return;
        }
        Assert.assertEquals(properties.get("variablePartiallyFromOSEnvironmentVariable"), "Fixed value and true some dynamic parts");
    }

    @Test
    public void test$EnvFromConfigFile() {
        Assert.assertEquals("http://${env.HOSTNAME}:${env.PORT.TRAFFIC}", EnvironmentProperties.resolveValueWithEnvVars("http://${env.HOSTNAME}:${env.PORT.TRAFFIC}"));
        Assert.assertNotNull(EnvironmentProperties.resolveValueWithEnvVars("${PATH}"));
    }
}
