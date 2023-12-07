package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.beust.ah.A;
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
        System.out.println(missingApiAccesses);
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
        System.out.println(missingApiAccesses);
        Assert.assertEquals(0,missingApiAccesses.size());
    }

    @Test
    public void getMissingAPIAccessesWithUnique(){
        List<APIAccess> apiAccesses = new ArrayList<>();
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiId("1235");
        apiAccesses.add(apiAccess);
        List<APIAccess> otherApiAccess = new ArrayList<>();
        APIAccess apiAccess2 = new APIAccess();
        apiAccess2.setApiId("12345");
        otherApiAccess.add(apiAccess2);
        List<APIAccess> missingApiAccesses = apiManagerAPIAccessAdapter.getMissingAPIAccesses(apiAccesses, otherApiAccess);
        Assert.assertEquals("1235",missingApiAccesses.get(0).getApiId());
    }

    @Test
    public void getMissingAPIAccessesWithUniqueReverse(){
        List<APIAccess> apiAccesses = new ArrayList<>();
        APIAccess apiAccess = new APIAccess();
        apiAccess.setApiId("1235");
        apiAccesses.add(apiAccess);
        List<APIAccess> otherApiAccess = new ArrayList<>();
        APIAccess apiAccess2 = new APIAccess();
        apiAccess2.setApiId("12345");
        otherApiAccess.add(apiAccess2);
        List<APIAccess> missingApiAccesses = apiManagerAPIAccessAdapter.getMissingAPIAccesses(otherApiAccess, apiAccesses);
        Assert.assertEquals("12345",missingApiAccesses.get(0).getApiId());
    }




}
