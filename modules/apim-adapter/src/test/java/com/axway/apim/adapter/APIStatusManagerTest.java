package com.axway.apim.adapter;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIStatusManagerTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }
    public void setupParameters() throws AppException {
        APIManagerAdapter.deleteInstance();
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());

    }
    @Test
    public void updateStateUnpublished() throws AppException {
        setupParameters();
        APIStatusManager apiStatusManager = new APIStatusManager();
        APIManagerAPIAdapter apiManagerAPIAdapter = APIManagerAdapter.getInstance().apiAdapter;
        APIFilter apiFilter = new APIFilter.Builder().hasName("petstore").build();
        API api = apiManagerAPIAdapter.getAPI(apiFilter, false);
        apiStatusManager.update(api, "unpublished", true);
    }

    @Test
    public void updateStateUnpublishedAndVhost() throws AppException {
        setupParameters();
        APIStatusManager apiStatusManager = new APIStatusManager();
        APIManagerAPIAdapter apiManagerAPIAdapter = APIManagerAdapter.getInstance().apiAdapter;
        APIFilter apiFilter = new APIFilter.Builder().hasName("petstore").build();
        API api = apiManagerAPIAdapter.getAPI(apiFilter, false);
        apiStatusManager.update(api, "published",  "api.axway.com", true);
    }
}
