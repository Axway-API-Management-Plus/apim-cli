package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Config;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.IOException;

public class APIManagerConfigAdapterTest extends WiremockWrapper {

    private APIManagerConfigAdapter apiManagerConfigAdapter;
    private APIManagerAdapter apiManagerAdapter;

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        try {
            APIManagerAdapter.deleteInstance();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("test");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAdapter = APIManagerAdapter.getInstance();
            apiManagerConfigAdapter = apiManagerAdapter.configAdapter;
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void testCorrectFieldsAreIgnored() throws AppException {
        apiManagerAdapter.setApiManagerVersion("7.7.20200930");
        String[] ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
        // No field should be ignored for the Sept-Release
        Assert.assertNotNull(ignoreFields);
        Assert.assertEquals(ignoreFields.length, 0);

        apiManagerAdapter.setApiManagerVersion("7.7.20200730");
        ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
        // Same for July, as fields have been added in January release
        Assert.assertNotNull(ignoreFields);
        Assert.assertEquals(ignoreFields.length, 0);

        apiManagerAdapter.setApiManagerVersion("7.7.20200331");
        ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
        // March release doesn't support new parameters introduced with the May release
        Assert.assertNotNull(ignoreFields);
        Assert.assertEquals(ignoreFields.length, 4);

        apiManagerAdapter.setApiManagerVersion("7.7.0");
        ignoreFields = APIManagerConfigAdapter.ConfigFields.getIgnoredFields();
        // 7.7.0 plain already supports some new settings
        Assert.assertNotNull(ignoreFields);
        Assert.assertEquals(ignoreFields.length, 4); // Only brand new fields are returned to be ignored
    }

    @Test
    public void updateConfiguration()  {
        try {
            Config config = apiManagerConfigAdapter.getConfig(true);
            config.setApiPortalHostname("api.axway.com");
            apiManagerConfigAdapter.updateConfiguration(config);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }

    }
}
