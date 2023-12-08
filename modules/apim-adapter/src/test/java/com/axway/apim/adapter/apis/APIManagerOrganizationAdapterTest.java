package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIManagerOrganizationAdapterTest extends WiremockWrapper {

    private APIManagerOrganizationAdapter organizationAdapter;
    private APIManagerAdapter apiManagerAdapter;
    String orgName = "orga";

    @BeforeClass
    public void init() {
        try {
            initWiremock();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAdapter = APIManagerAdapter.getInstance();
            organizationAdapter = apiManagerAdapter.getOrgAdapter();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }


    @AfterClass
    public void close() {
        Utils.deleteInstance(apiManagerAdapter);
        super.close();
    }


    @Test
    public void getOrgForName() throws AppException {
        Organization organization = organizationAdapter.getOrgForName(orgName);
        Assert.assertEquals(organization.getName(), orgName);
    }

    @Test
    public void deleteOrganization() throws AppException {
        Organization organization = organizationAdapter.getOrgForName(orgName);
        try {
            organizationAdapter.deleteOrganization(organization);
        } catch (AppException appException) {
            Assert.fail("unable to delete organization", appException);
        }
    }

    @Test
    public void createOrganization() {
        Organization organization = new Organization();
        organization.setName(orgName);
        organization.setDevelopment(true);
        organization.setEmail("orga@axway.com");
        try {
            organizationAdapter.createOrganization(organization);
        } catch (AppException appException) {
            Assert.fail("unable to Create organization", appException);
        }
    }

    @Test
    public void updateOrganization() throws AppException {
        OrgFilter orgFilter = new OrgFilter.Builder().hasName(orgName).build();
        Organization organization = organizationAdapter.getOrg(orgFilter);
        Organization updateOrganization = organizationAdapter.getOrg(orgFilter);
        organization.setImageUrl("com/axway/apim/images/API-Logo.jpg");
        organizationAdapter.createOrUpdateOrganization(updateOrganization, organization);
    }




    @Test
    public void addAPIAccess() throws AppException {
        OrgFilter orgFilter = new OrgFilter.Builder().hasName(orgName).build();
        Organization organization = organizationAdapter.getOrg(orgFilter);
        organizationAdapter.addAPIAccess(organization, true);
       Assert.assertEquals( organization.getApiAccess().size(), 1);
    }



    @Test
    public void addImage() throws AppException {
        OrgFilter orgFilter = new OrgFilter.Builder().hasName(orgName).build();
        Organization organization = organizationAdapter.getOrg(orgFilter);
        organization.setImageUrl("com/axway/apim/images/API-Logo.jpg");
        try {
            organizationAdapter.addImage(organization, true);
        } catch (Exception appException) {
            Assert.fail("unable to add Image", appException);
        }
    }
}
