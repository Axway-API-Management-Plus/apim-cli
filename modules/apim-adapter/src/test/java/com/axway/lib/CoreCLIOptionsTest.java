package com.axway.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.lib.utils.SampleCLIOptions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class CoreCLIOptionsTest {

    private String apimCliHome;
    @BeforeClass
    private void initCommandParameters() {
        apimCliHome = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + "apimcli";
    }
    @Test
    public void testCoreParameters() throws AppException {
        String[] args = {"-h", "api-env", "-u", "apiadmin", "-p", "changeme", "-port", "8888 ", "-apimCLIHome", "My-home-is-my-castle", "-clearCache", "ALL", "-returnCodeMapping", "10:0", "-rollback", "false", "-force", "-ignoreCache", "-retryDelay", "10000"};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();

        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://api-env:8888");
        Assert.assertEquals(params.getUsername(), "apiadmin");
        Assert.assertEquals(params.getPassword(), "changeme");
        Assert.assertEquals(params.getApimCLIHome(), "My-home-is-my-castle");
        Assert.assertEquals(params.getClearCache(), "ALL");
        Assert.assertEquals(params.getReturnCodeMapping(), "10:0");
        Assert.assertTrue(params.isForce());
        Assert.assertFalse(params.isRollback());
        Assert.assertTrue(params.isIgnoreCache());
        Assert.assertEquals(params.getRetryDelay(), 10000);
    }

    @Test
    public void testManagerURLParameter() throws AppException {
        String[] args = {"-apimanagerUrl", "https://manager.k8s.com/some/path", "-u", "apiadmin", "-p", "changeme"};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://manager.k8s.com/some/path");
    }

    @Test
    public void testOldForceParameter() throws AppException {
        String[] args = {"-s", "api-env", "-f", "true"};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();
        Assert.assertEquals(params.getRetryDelay(), 1000, "Should default be 1 second");
        Assert.assertTrue(params.isForce());
    }

    @Test
    public void testStagePropertyFiles() throws IOException {
        String[] args = {"-s", "yetAnotherStage", "-apimCLIHome", apimCliHome};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();
        Assert.assertEquals(params.getProperties().get("yetAnotherProperty"), "HellImHere"); // from env.yetAnotherStage.properties
        Assert.assertEquals(params.getProperties().get("myTestVariable"), "resolvedToSomething"); // from env.properties
    }

    @Test
    public void testPropertyFileWithoutStage() throws IOException {
        String[] args = {"-apimCLIHome", apimCliHome};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();
        Assert.assertEquals(params.getAPIManagerURL().toString(), "https://localhost:8075");
        Assert.assertEquals(params.getProperties().get("myTestVariable"), "resolvedToSomething"); // from env.properties
    }

    @Test
    public void testProxyParametersFromStage() throws IOException {
        String[] args = {"-s", "stageWithProxy", "-apimCLIHome", apimCliHome};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();
        Assert.assertEquals(params.getProxyHost(), "proxyHost");
        Assert.assertTrue(params.getProxyPort() == 8987);
        Assert.assertEquals(params.getProxyUsername(), "proxyUser");
        Assert.assertEquals(params.getProxyPassword(), "proxyPassword");
    }

    @Test
    public void testProxyParams() throws AppException {
        String[] args = {"-httpProxyHost", "myProxyHost", "-httpProxyPort", "6767", "-httpProxyUsername", "myProxyUser", "-httpProxyPassword", "myProxyPW"};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();

        Assert.assertEquals(params.getProxyHost(), "myProxyHost");
        Assert.assertTrue(params.getProxyPort() == 6767);
        Assert.assertEquals(params.getProxyUsername(), "myProxyUser");
        Assert.assertEquals(params.getProxyPassword(), "myProxyPW");

        // Test if port is not given
        String[] args2 = {"-httpProxyHost", "myProxyHost", "-httpProxyUsername", "myProxyUser", "-httpProxyPassword", "myProxyPW"};
        options = SampleCLIOptions.create(args2);
        params = (CoreParameters) options.getParams();
        Assert.assertTrue(params.getProxyPort() == -1);

        // Test if Username  / Password is not given
        String[] args3 = {"-httpProxyHost", "myProxyHost3", "-httpProxyPort", "1234"};
        options = SampleCLIOptions.create(args3);
        params = (CoreParameters) options.getParams();
        Assert.assertEquals(params.getProxyHost(), "myProxyHost3");
        Assert.assertTrue(params.getProxyPort() == 1234);
        Assert.assertNull(params.getProxyUsername());
        Assert.assertNull(params.getProxyPassword());
    }

    @Test
    public void testAPIBasePathParam() throws AppException {
        String[] args = {"-h", "my-manager", "-apiBasepath", "/fr/apim/v13/portal"};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();

        Assert.assertEquals(params.getApiBasepath(), "/fr/apim/v13/portal");
    }

    @Test
    public void testDisableCompression() throws AppException {
        String[] args = {"-disableCompression"};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();
        Assert.assertEquals(params.isDisableCompression(), true);

    }

    @Test
    public void testDisableCompressionNegative() throws AppException {
        String[] args = {""};
        CLIOptions options = SampleCLIOptions.create(args);
        CoreParameters params = (CoreParameters) options.getParams();
        Assert.assertEquals(params.isDisableCompression(), false);

    }
}
