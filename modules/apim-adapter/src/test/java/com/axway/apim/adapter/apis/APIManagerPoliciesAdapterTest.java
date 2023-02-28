package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Policy;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class APIManagerPoliciesAdapterTest extends WiremockWrapper {

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
    public void getAllPolicies() throws AppException {
        APIManagerPoliciesAdapter apiManagerPoliciesAdapter = apiManagerAdapter.policiesAdapter;
        List<Policy> policies = apiManagerPoliciesAdapter.getAllPolicies();
        Assert.assertNotNull(policies);
    }

    @Test(expectedExceptions = AppException.class)
    public void getPolicyForNameNegative()throws AppException {
        APIManagerPoliciesAdapter apiManagerPoliciesAdapter = apiManagerAdapter.policiesAdapter;
        Policy policy = apiManagerPoliciesAdapter.getPolicyForName(APIManagerPoliciesAdapter.PolicyType.REQUEST, "test");
        Assert.assertNotNull(policy);
    }

    @Test
    public void getPolicyForName()throws AppException {
        APIManagerPoliciesAdapter apiManagerPoliciesAdapter = apiManagerAdapter.policiesAdapter;
        Policy policy = apiManagerPoliciesAdapter.getPolicyForName(APIManagerPoliciesAdapter.PolicyType.REQUEST, "Validate Size & Token");
        Assert.assertNotNull(policy);
    }
}
