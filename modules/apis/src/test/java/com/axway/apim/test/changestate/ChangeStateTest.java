package com.axway.apim.test.changestate;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangeStateTest extends WiremockWrapper {

    private APIManagerAdapter apiManagerAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAdapter =  APIManagerAdapter.getInstance();
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
    public void testOrderMakesNoChange() throws IOException {

        API importAPI = getTestAPI();
        API managerAPI = getTestAPI();

        List<Organization> importOrgs = new ArrayList<>();
        List<Organization> managerOrgs = new ArrayList<>();


        importOrgs.add(new Organization.Builder().hasName("orgA").hasId("123").build());
        importOrgs.add(new Organization.Builder().hasName("orgB").hasId("456").build());
        importOrgs.add(new Organization.Builder().hasName("orgC").hasId("789").build());

        managerOrgs.add(new Organization.Builder().hasName("orgC").hasId("123").build());
        managerOrgs.add(new Organization.Builder().hasName("orgB").hasId("456").build());
        managerOrgs.add(new Organization.Builder().hasName("orgA").hasId("789").build());

        importAPI.setClientOrganizations(importOrgs);
        importAPI.setOrganization(new Organization.Builder().hasName("123").hasId("789").build());

        managerAPI.setClientOrganizations(managerOrgs);
        managerAPI.setOrganization(new Organization.Builder().hasName("123").hasId("789").build());

        APIChangeState changeState = new APIChangeState(managerAPI, importAPI);
        Assert.assertEquals(changeState.hasAnyChanges(), false);
    }

    @Test
    public void isVhostBreaking() throws Exception {
        API importAPI = getTestAPI();
        API managerAPI = getTestAPI();

        importAPI.setVhost("abc.xyz.com");
        managerAPI.setVhost("123.xyz.com");

        APIChangeState changeState = new APIChangeState(managerAPI, importAPI);
        Assert.assertEquals(changeState.isBreaking(), true);
    }

    @Test
    public void isDesiredStateDeleted() throws Exception {
        API importAPI = getTestAPI();
        API managerAPI = getTestAPI();

        importAPI.setState(API.STATE_DELETED);
        importAPI.setDescriptionType("ANY-TYPE");


        managerAPI.setState(API.STATE_PUBLISHED);
        importAPI.setDescriptionType("ANY-OTHER-TYPE");

        APIChangeState changeState = new APIChangeState(managerAPI, importAPI);
        // As the API should be deleted anyway, desiredChanges should be ignored - The API should just be deleted
        Assert.assertEquals(changeState.getAllChanges().size(), 1, "The state should be included");
        Assert.assertEquals(changeState.getAllChanges().get(0), "state", "The state should be included");
    }

    private static API getTestAPI() {
        API testAPI = new API();
        testAPI.setOrganization(new Organization.Builder().hasName("123").hasId("123").build());
        testAPI.setState(API.STATE_PUBLISHED);
        return testAPI;
    }

}
