package com.axway.apim.apiimport.actions;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class ManageClientAppsTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());
    }

    @AfterClass
    public void close() {
        super.close();
    }


    @Test
    public void containsAppNameEqual() throws Exception {
        API desiredState = new API();
        API actualState = new API();

        ManageClientApps manageClientApps = new ManageClientApps(desiredState, actualState, null);
        List<ClientApplication> source = new ArrayList<>();
        Organization organization = new Organization();
        organization.setName("Test Organization");
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setOrganization(organization);
        clientApplication.setName("Test Client Application");
        source.add(clientApplication);
        Assert.assertTrue(manageClientApps.containsAppName(source, clientApplication));

    }

    @Test
    public void containsAppNameNotEqual() throws Exception {
        API desiredState = new API();
        API actualState = new API();

        ManageClientApps manageClientApps = new ManageClientApps(desiredState, actualState, null);
        List<ClientApplication> source = new ArrayList<>();
        Organization organization = new Organization();
        organization.setName("Test Organization");
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setOrganization(organization);
        clientApplication.setName("Test Client Application");
        source.add(clientApplication);

        ClientApplication newClientApplication = new ClientApplication();
        newClientApplication.setOrganization(organization);
        newClientApplication.setName("Test Client Application2");
        source.add(clientApplication);


        Assert.assertFalse(manageClientApps.containsAppName(source, newClientApplication));

    }

    @Test

    public void getMissingApps() throws Exception {
        API desiredState = new API();
        API actualState = new API();

        List<ClientApplication> source = new ArrayList<>();
        Organization organization = new Organization();
        organization.setName("Test Organization");
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setOrganization(organization);
        clientApplication.setName("Test Client Application");
        source.add(clientApplication);
        List<ClientApplication> target = new ArrayList<>();
        ClientApplication clientApplicationTarget = new ClientApplication();
        clientApplicationTarget.setOrganization(organization);
        clientApplicationTarget.setName("Test Client Application Target");
        target.add(clientApplicationTarget);

        ManageClientApps manageClientApps = new ManageClientApps(desiredState, actualState, null);
        List<ClientApplication> missingApps = manageClientApps.getMissingApps(source, target);
        Assert.assertEquals(missingApps.get(0).getName(), "Test Client Application");

    }
}
