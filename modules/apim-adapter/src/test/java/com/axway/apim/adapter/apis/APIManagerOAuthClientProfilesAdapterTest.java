package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.OAuthClientProfile;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class APIManagerOAuthClientProfilesAdapterTest extends WiremockWrapper {

    private static final Logger logger = LoggerFactory.getLogger(APIManagerOAuthClientProfilesAdapterTest.class);

    private APIManagerOAuthClientProfilesAdapter apiManagerOAuthClientProfilesAdapter;

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        try {
            APIManagerAdapter.deleteInstance();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("test");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerOAuthClientProfilesAdapter = APIManagerAdapter.getInstance().oauthClientAdapter;
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void getOAuthClientProfiles() throws AppException {
        List<OAuthClientProfile> oAuthClientProfiles = apiManagerOAuthClientProfilesAdapter.getOAuthClientProfiles();
        Assert.assertNotNull(oAuthClientProfiles);
        logger.info("Oauth Client profiles: {}", oAuthClientProfiles);
        if(!oAuthClientProfiles.isEmpty()){
            OAuthClientProfile oAuthClientProfile = oAuthClientProfiles.get(0);
            Assert.assertEquals(oAuthClientProfile.getName(), "Sample OAuth Client Profile");
        }
    }

    @Test
    public void getOAuthClientProfile() throws AppException {
        String oauthProfileName = "Sample OAuth Client Profile";
        OAuthClientProfile oAuthClientProfile = apiManagerOAuthClientProfilesAdapter.getOAuthClientProfile(oauthProfileName);
        Assert.assertNotNull(oAuthClientProfile);
        Assert.assertEquals(oAuthClientProfile.getName(), oauthProfileName);
    }
}
