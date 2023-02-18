package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class APIManagerOrganizationAdapterTest extends WiremockWrapper {

    private APIManagerAdapter apiManagerAdapter;
    String orgName = "orga";

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
    public void getOrgForName() throws AppException {
        APIManagerOrganizationAdapter apiManagerOrganizationAdapter = apiManagerAdapter.orgAdapter;
        Organization organization = apiManagerOrganizationAdapter.getOrgForName(orgName);
        Assert.assertEquals(organization.getName(), orgName);
    }

    @Test
    public void deleteOrganization() throws AppException {
        APIManagerOrganizationAdapter apiManagerOrganizationAdapter = apiManagerAdapter.orgAdapter;
        Organization organization = apiManagerOrganizationAdapter.getOrgForName(orgName);
        try {
            apiManagerOrganizationAdapter.deleteOrganization(organization);
        } catch (AppException appException) {
            Assert.fail("unable to delete organization", appException);
        }
    }

    @Test
    public void createOrganization() {

        APIManagerOrganizationAdapter apiManagerOrganizationAdapter = apiManagerAdapter.orgAdapter;
        Organization organization = new Organization();
        organization.setName(orgName);
        organization.setDevelopment(true);
        organization.setEmail("orga@axway.com");
        try {
            apiManagerOrganizationAdapter.createOrganization(organization);
        } catch (AppException appException) {
            Assert.fail("unable to Create organization", appException);
        }
    }

    @Test
    public void updateOrganization() {

    }

    //    @Test
//    public void updateUserCreateNewUserFlow() throws AppException {
//        setupParameters();
//        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
//        APIManagerUserAdapter apiManagerUserAdapter = apiManagerAdapter.userAdapter;
//        User user = new User();
//        user.setEmail("updated@axway.com");
//        user.setName("usera");
//        user.setLoginName("usera");
//        User newUser = apiManagerUserAdapter.updateUser(user, null);
//        Assert.assertEquals(newUser.getEmail(), "updated@axway.com");
//    }
//
//    @Test
//    public void changePassword() throws AppException {
//        setupParameters();
//        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
//        APIManagerUserAdapter apiManagerUserAdapter = apiManagerAdapter.userAdapter;
//        UserFilter userFilter = new UserFilter.Builder().hasLoginName(loginName).build();
//        User user = apiManagerUserAdapter.getUser(userFilter);
//        try {
//            apiManagerUserAdapter.changePassword(Utils.getEncryptedPassword(), user);
//        } catch (AppException appException) {
//            Assert.fail("unable to change user password", appException);
//        }
//    }
//
    @Test
    public void addImage() throws AppException {
        APIManagerOrganizationAdapter apiManagerOrganizationAdapter = apiManagerAdapter.orgAdapter;
        OrgFilter orgFilter = new OrgFilter.Builder().hasName(orgName).build();
        Organization organization = apiManagerOrganizationAdapter.getOrg(orgFilter);
        organization.setImageUrl("https://axway.com/favicon.ico");
        try {
            apiManagerOrganizationAdapter.addImage(organization, true);
        } catch (Exception appException) {
            Assert.fail("unable to add Image", appException);
        }
    }
}