package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Alerts;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIManagerAlertsAdapterTest extends WiremockWrapper {

    private APIManagerAlertsAdapter apiManagerAlertsAdapter;

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        try {
            APIManagerAdapter.deleteInstance();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("test");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAlertsAdapter = APIManagerAdapter.getInstance().alertsAdapter;
        } catch (AppException e) {
            throw new RuntimeException(e);
        }

    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void getAlerts() throws AppException {
        Alerts alerts = apiManagerAlertsAdapter.getAlerts();
        Assert.assertNotNull(alerts);
    }

    @Test
    public void updateAlerts() throws AppException {
        Alerts alerts = apiManagerAlertsAdapter.getAlerts();
        Assert.assertNotNull(alerts);
        alerts.setApicatalogDisable(true);
        try {
            apiManagerAlertsAdapter.updateAlerts(alerts);
        }catch (AppException appException){
            Assert.fail("Unable to update alerts", appException);
        }

    }
}
