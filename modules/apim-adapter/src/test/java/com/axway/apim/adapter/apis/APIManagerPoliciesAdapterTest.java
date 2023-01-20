package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Policy;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class APIManagerPoliciesAdapterTest extends WiremockWrapper {

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
    public void getAllPolicies() throws AppException {
        setupParameters();
        APIManagerPoliciesAdapter apiManagerPoliciesAdapter = APIManagerAdapter.getInstance().policiesAdapter;
        List<Policy> policies = apiManagerPoliciesAdapter.getAllPolicies();
        Assert.assertNotNull(policies);
    }

    @Test(expectedExceptions = AppException.class)
    public void getPolicyForNameNegative()throws AppException {
        setupParameters();
        APIManagerPoliciesAdapter apiManagerPoliciesAdapter = APIManagerAdapter.getInstance().policiesAdapter;
        Policy policy = apiManagerPoliciesAdapter.getPolicyForName(APIManagerPoliciesAdapter.PolicyType.REQUEST, "test");
        Assert.assertNotNull(policy);
    }

    @Test
    public void getPolicyForName()throws AppException {
        setupParameters();
        APIManagerPoliciesAdapter apiManagerPoliciesAdapter = APIManagerAdapter.getInstance().policiesAdapter;
        Policy policy = apiManagerPoliciesAdapter.getPolicyForName(APIManagerPoliciesAdapter.PolicyType.REQUEST, "Validate Size & Token");
        Assert.assertNotNull(policy);
    }
}
