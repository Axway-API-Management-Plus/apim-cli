package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * queryStringPassThrough only exists in the inboundProfiles from API-Manager 7.7.20220530 onwards, so it has to be
 * excluded from the proxy update on older versions. The check must cope with every version string the API-Manager
 * reports, in particular the dated builds carrying a service pack suffix.
 */
public class APIManagerVersionGatingTest extends WiremockWrapper {

    private static final String QUERY_STRING_PASS_THROUGH = "queryStringPassThrough";

    private APIManagerAPIAdapter apiAdapter;
    private String originalVersion;

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        try {
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("test");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiAdapter = APIManagerAdapter.getInstance().getApiAdapter();
            originalVersion = APIManagerAdapter.getInstance().getApiManagerVersion();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @AfterMethod
    public void restoreVersion() throws AppException {
        APIManagerAdapter.getInstance().setApiManagerVersion(originalVersion);
    }

    private boolean queryStringPassThroughIsSent(String version) throws AppException {
        APIManagerAdapter.getInstance().setApiManagerVersion(version);
        List<String> excluded = Arrays.asList(apiAdapter.getSerializeAllExcept());
        // When the field is NOT excluded from serialization, it is sent to the API-Manager.
        return !excluded.contains(QUERY_STRING_PASS_THROUGH);
    }

    @Test
    public void sentOnTheVersionThatIntroducedIt() throws AppException {
        Assert.assertTrue(queryStringPassThroughIsSent(APIManagerAPIAdapter.QUERY_STRING_PASS_THROUGH_MIN_VERSION));
    }

    @Test
    public void sentOnANewerDatedBuild() throws AppException {
        Assert.assertTrue(queryStringPassThroughIsSent("7.7.20261130"));
    }

    /**
     * The previous implementation stripped the "7.7." prefix and called Integer.parseInt on the remainder, which
     * threw on a service pack suffix and silently dropped the field.
     */
    @Test
    public void sentOnADatedBuildWithAServicePack() throws AppException {
        Assert.assertTrue(queryStringPassThroughIsSent("7.7.20261130 SP1"),
            "queryStringPassThrough must still be sent when the API-Manager reports a service pack");
    }

    @Test
    public void notSentOnAnOlderDatedBuild() throws AppException {
        Assert.assertFalse(queryStringPassThroughIsSent("7.7.20200130"));
    }

    @Test
    public void notSentOnAVersionPredatingTheDatedBuilds() throws AppException {
        Assert.assertFalse(queryStringPassThroughIsSent("7.7.0"));
    }
}
