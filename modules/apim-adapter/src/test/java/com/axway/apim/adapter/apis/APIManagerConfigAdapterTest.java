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
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("test");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAdapter = APIManagerAdapter.getInstance();
            apiManagerConfigAdapter = apiManagerAdapter.getConfigAdapter();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        super.close();
    }



    @Test
    public void updateConfiguration()  {
        try {
            Config config = apiManagerConfigAdapter.getConfig();
            config.setApiPortalHostname("api.axway.com");
            apiManagerConfigAdapter.updateConfiguration(config);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }

    }
}
