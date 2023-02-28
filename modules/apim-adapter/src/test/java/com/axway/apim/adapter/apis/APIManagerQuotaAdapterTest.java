package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIManagerQuotaAdapterTest extends WiremockWrapper {

    private static final Logger logger = LoggerFactory.getLogger(APIManagerQuotaAdapterTest.class);

    private APIManagerQuotaAdapter apiManagerQuotaAdapter;

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        try {
            APIManagerAdapter.deleteInstance();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("test");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerQuotaAdapter = APIManagerAdapter.getInstance().quotaAdapter;
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void saveQuota() throws AppException {
        APIQuota applicationQuota = apiManagerQuotaAdapter.getQuota(APIManagerQuotaAdapter.Quota.APPLICATION_DEFAULT.getQuotaId(), null, false, false); // Get the Application-Default-Quota
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        APIManagerQuotaAdapter apiManagerQuotaAdapter = apiManagerAdapter.quotaAdapter;
        try {
            apiManagerQuotaAdapter.saveQuota(applicationQuota, "00000000-0000-0000-0000-000000000001");
        } catch (AppException appException) {
            Assert.fail("Unable to save quota", appException);
        }
    }

    @Test
    public void getDefaultQuota() throws AppException {
        APIQuota apiQuota = apiManagerQuotaAdapter.getDefaultQuota(APIManagerQuotaAdapter.Quota.SYSTEM_DEFAULT);
        Assert.assertNotNull(apiQuota);
        Assert.assertEquals(apiQuota.getType(), "API");
        logger.info("API Quota : {}", apiQuota);
        apiQuota = apiManagerQuotaAdapter.getDefaultQuota(APIManagerQuotaAdapter.Quota.APPLICATION_DEFAULT);
        Assert.assertNotNull(apiQuota);
        Assert.assertEquals(apiQuota.getType(), "APPLICATION");
        logger.info("Application API Quota : {}", apiQuota);
    }
}
