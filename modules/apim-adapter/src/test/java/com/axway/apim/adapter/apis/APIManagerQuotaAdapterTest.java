package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIManagerQuotaAdapterTest extends WiremockWrapper {

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
    public void saveQuota() throws AppException {
        setupParameters();
        APIQuota applicationQuota = APIManagerAdapter.getInstance().quotaAdapter.getQuota(APIManagerQuotaAdapter.Quota.APPLICATION_DEFAULT.getQuotaId(), null, false, false); // Get the Application-Default-Quota
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        APIManagerQuotaAdapter apiManagerQuotaAdapter = apiManagerAdapter.quotaAdapter;
        try {
            apiManagerQuotaAdapter.saveQuota(applicationQuota, "00000000-0000-0000-0000-000000000001");
        }catch (AppException appException){
            Assert.fail("Unable to save quota", appException);
        }
    }
}
