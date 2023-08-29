package com.axway.apim.appimport.adapter;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.appimport.lib.AppImportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ClientAppConfigAdapterTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void readConfigTest() {
        String testFile = ClientAppConfigAdapterTest.class.getResource("/com/axway/apim/appimport/adapter/appwithapis.json").getPath();

        AppImportParams appImportParams = new AppImportParams();
        appImportParams.setUsername("apiadmin");
        appImportParams.setPassword(Utils.getEncryptedPassword());
        appImportParams.setHostname("localhost");
        appImportParams.setConfig(testFile);

        // appImportParams.
        ClientAppConfigAdapter clientAppConfigAdapter = new ClientAppConfigAdapter(appImportParams);
        try {
            clientAppConfigAdapter.readConfig();
        } catch (AppException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
