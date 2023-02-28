package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class APIManagerAPIAccessAdapterTest extends WiremockWrapper {

    private APIManagerAPIAccessAdapter apiManagerAPIAccessAdapter;
    private APIManagerOrganizationAdapter apiManagerOrganizationAdapter;
    String orgName = "orga";
    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        try {
            APIManagerAdapter.deleteInstance();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("test");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAPIAccessAdapter = APIManagerAdapter.getInstance().accessAdapter;
            apiManagerOrganizationAdapter = APIManagerAdapter.getInstance().orgAdapter;
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void getAPIAccess() throws AppException {
        Organization organization = apiManagerOrganizationAdapter.getOrgForName(orgName);
        Assert.assertEquals(organization.getName(), orgName);
        List<APIAccess> apiAccesses = apiManagerAPIAccessAdapter.getAPIAccess(organization, APIManagerAPIAccessAdapter.Type.organizations);
        Assert.assertNotNull(apiAccesses);
        if(!apiAccesses.isEmpty()){
            APIAccess apiAccess = apiAccesses.get(0);
            Assert.assertNull(apiAccess.getApiName());
        }

    }

    @Test
    public void getAPIAccessIncludeAPIName() throws AppException {
        Organization organization = apiManagerOrganizationAdapter.getOrgForName(orgName);
        Assert.assertEquals(organization.getName(), orgName);
        List<APIAccess> apiAccesses = apiManagerAPIAccessAdapter.getAPIAccess(organization, APIManagerAPIAccessAdapter.Type.organizations, true);
        Assert.assertNotNull(apiAccesses);

        if(!apiAccesses.isEmpty()){
            APIAccess apiAccess = apiAccesses.get(0);
            Assert.assertEquals(apiAccess.getApiName(), "petstore3");
        }
    }

    @Test
    public void deleteAPIAccess() throws AppException {
        Organization organization = apiManagerOrganizationAdapter.getOrgForName(orgName);
        Assert.assertEquals(organization.getName(), orgName);
        List<APIAccess> apiAccesses = apiManagerAPIAccessAdapter.getAPIAccess(organization, APIManagerAPIAccessAdapter.Type.organizations, false);
        Assert.assertNotNull(apiAccesses);
        APIAccess apiAccess = apiAccesses.get(0);
        apiManagerAPIAccessAdapter.deleteAPIAccess(apiAccess, organization, APIManagerAPIAccessAdapter.Type.organizations);
    }

    @Test
    public void removeClientOrganization() throws AppException {
        Organization organization = apiManagerOrganizationAdapter.getOrgForName(orgName);
        Assert.assertEquals(organization.getName(), orgName);
        List<APIAccess> apiAccesses = apiManagerAPIAccessAdapter.getAPIAccess(organization, APIManagerAPIAccessAdapter.Type.organizations, true);
        Assert.assertNotNull(apiAccesses);
        APIAccess apiAccess = apiAccesses.get(0);
        List<Organization> organizations = new ArrayList<>();
        organizations.add(organization);
        try {
            apiManagerAPIAccessAdapter.removeClientOrganization(organizations, apiAccess.getApiId());
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createAPIAccess() throws AppException {
        Organization organization = apiManagerOrganizationAdapter.getOrgForName(orgName);
        Assert.assertEquals(organization.getName(), orgName);
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiName("test");
        apiAccess.setApiVersion("1.0");
        apiAccess.setId(UUID.randomUUID().toString());
        apiAccess.setEnabled(true);
        apiAccess.setState("approved");
        apiAccess.setCreatedBy("40dd53a4-0b13-4485-82e8-63c687404c2f");
        try {
            apiManagerAPIAccessAdapter.createAPIAccess(apiAccess, organization, APIManagerAPIAccessAdapter.Type.organizations);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createAPIAccessWithExistingApi() throws AppException {
        Organization organization = apiManagerOrganizationAdapter.getOrgForName(orgName);
        Assert.assertEquals(organization.getName(), orgName);
        List<APIAccess> apiAccesses = apiManagerAPIAccessAdapter.getAPIAccess(organization, APIManagerAPIAccessAdapter.Type.organizations, true);
        Assert.assertNotNull(apiAccesses);
        APIAccess apiAccess = apiAccesses.get(0);
        try {
            apiManagerAPIAccessAdapter.createAPIAccess(apiAccess, organization, APIManagerAPIAccessAdapter.Type.organizations);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

}
