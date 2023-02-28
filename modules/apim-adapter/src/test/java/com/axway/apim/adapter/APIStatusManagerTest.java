package com.axway.apim.adapter;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIStatusManagerTest extends WiremockWrapper {

    private APIManagerAdapter apiManagerAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();
            APIManagerAdapter.deleteInstance();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAdapter = APIManagerAdapter.getInstance();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void updateStateUnpublished() throws AppException {
        APIStatusManager apiStatusManager = new APIStatusManager();
        APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
        APIFilter apiFilter = new APIFilter.Builder().hasName("petstore").build();
        API api = apiManagerAPIAdapter.getAPI(apiFilter, false);
        apiStatusManager.update(api, "unpublished", true);
    }

    @Test
    public void updateStateUnpublishedAndVhost() throws AppException {
        APIStatusManager apiStatusManager = new APIStatusManager();
        APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
        APIFilter apiFilter = new APIFilter.Builder().hasName("petstore").build();
        API api = apiManagerAPIAdapter.getAPI(apiFilter, false);
        apiStatusManager.update(api, "published", "api.axway.com", true);
    }
}
