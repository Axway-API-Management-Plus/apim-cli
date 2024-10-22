package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.ApiOrganizationSubscription;
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
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("test");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAPIAccessAdapter = APIManagerAdapter.getInstance().getAccessAdapter();
            apiManagerOrganizationAdapter = APIManagerAdapter.getInstance().getOrgAdapter();
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

    @Test
    public void getMissingAPIAccessesEmpty(){
        List<APIAccess> apiAccess = new ArrayList<>();
        List<APIAccess> otherApiAccess = new ArrayList<>();
        List<APIAccess> missingApiAccesses = apiManagerAPIAccessAdapter.getMissingAPIAccesses(apiAccess, otherApiAccess);
        Assert.assertTrue(missingApiAccesses.isEmpty());
    }


    @Test
    public void getMissingAPIAccessesSame(){
        List<APIAccess> apiAccesses = new ArrayList<>();
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiId("1235");
        apiAccesses.add(apiAccess);
        List<APIAccess> otherApiAccess = new ArrayList<>();
        otherApiAccess.add(apiAccess);
        List<APIAccess> missingApiAccesses = apiManagerAPIAccessAdapter.getMissingAPIAccesses(apiAccesses, otherApiAccess);
        Assert.assertTrue(missingApiAccesses.isEmpty());
    }


    @Test
    public void getMissingAPIAccessesSourceEmpty(){
        List<APIAccess> apiAccesses = new ArrayList<>();
        List<APIAccess> otherApiAccess = new ArrayList<>();
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiId("1235");
        otherApiAccess.add(apiAccess);
        List<APIAccess> missingApiAccesses = apiManagerAPIAccessAdapter.getMissingAPIAccesses(apiAccesses, otherApiAccess);
        Assert.assertTrue(missingApiAccesses.isEmpty());
    }

    @Test
    public void getMissingAPIAccessesTargetEmpty(){
        List<APIAccess> apiAccesses = new ArrayList<>();
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiId("1235");
        List<APIAccess> otherApiAccess = new ArrayList<>();
        otherApiAccess.add(apiAccess);
        List<APIAccess> missingApiAccesses = apiManagerAPIAccessAdapter.getMissingAPIAccesses(apiAccesses, otherApiAccess);
        Assert.assertTrue(missingApiAccesses.isEmpty());
    }

    @Test
    public void getMissingAPIAccessesWithDuplicates(){
        List<APIAccess> apiAccesses = new ArrayList<>();
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiId("1235");
        apiAccesses.add(apiAccess);
        List<APIAccess> otherApiAccess = new ArrayList<>();
        otherApiAccess.add(apiAccess);
        List<APIAccess> missingApiAccesses = apiManagerAPIAccessAdapter.getMissingAPIAccesses(apiAccesses, otherApiAccess);
        Assert.assertEquals(missingApiAccesses.size(), 0);
    }

    @Test
    public void getMissingAPIAccessesWithUnique(){
        List<APIAccess> apiAccesses = new ArrayList<>();
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiName("1235");
        apiAccesses.add(apiAccess);
        List<APIAccess> otherApiAccess = new ArrayList<>();
        APIAccess apiAccess2 = new APIAccess();
        apiAccess2.setApiName("12345");
        otherApiAccess.add(apiAccess2);
        List<APIAccess> missingApiAccesses = apiManagerAPIAccessAdapter.getMissingAPIAccesses(apiAccesses, otherApiAccess);
        Assert.assertEquals(missingApiAccesses.get(0).getApiName(), "1235");
    }

    @Test
    public void getMissingAPIAccessesWithUniqueReverse(){
        List<APIAccess> apiAccesses = new ArrayList<>();
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiName("1235");
        apiAccesses.add(apiAccess);
        List<APIAccess> otherApiAccess = new ArrayList<>();
        APIAccess apiAccess2 = new APIAccess();
        apiAccess2.setApiName("12345");
        otherApiAccess.add(apiAccess2);
        List<APIAccess> missingApiAccesses = apiManagerAPIAccessAdapter.getMissingAPIAccesses(otherApiAccess, apiAccesses);
        Assert.assertEquals(missingApiAccesses.get(0).getApiName(), "12345");
    }

    @Test
    public void getApiAccess() throws AppException {
        List<ApiOrganizationSubscription> apiOrganizationSubscriptions = apiManagerAPIAccessAdapter.getSubscribedOrganizationsAndApplications("1f4263ca-7f03-41d9-9d34-9eff79d29bd8");
        Assert.assertNotNull(apiOrganizationSubscriptions);
        Assert.assertEquals(4, apiOrganizationSubscriptions.size());
    }

    @Test
    public void createAPIAccessForApplication() throws AppException {
        String applicationId = "40dd53a4-0b13-4485-82e8-63c687404c2f";
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiId("40dd53a4-0b13-4485-82e8-63c687404c2g");
        APIAccess response = apiManagerAPIAccessAdapter.createAPIAccessForApplication(apiAccess, applicationId);
        Assert.assertNotNull(response);
    }

}
