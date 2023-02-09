package com.axway.apim.organization;

import com.axway.apim.WiremockWrapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class OrganizationAppTest extends WiremockWrapper {

    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void exportOrganization() {
        String[] args = {"-h", "localhost", "-name", "orga"};
        int returnCode = OrganizationApp.exportOrgs(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void exportOrganizations() {
        String[] args = {"-h", "localhost"};
        int returnCode = OrganizationApp.exportOrgs(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void importOrganization() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String orgFile = classLoader.getResource("com/axway/apim/organization/orgImport/organization.json").getFile();
        String[] args = {"-h", "localhost", "-c", orgFile};
        int returnCode = OrganizationApp.importOrganization(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void deleteOrganization() {
        String[] args = {"-h", "localhost", "-name", "orga"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = OrganizationApp.delete(args);
        Assert.assertEquals(returnCode, 0);
    }

    @Test
    public void deleteOrganizations() {
        String[] args = {"-h", "localhost"};
        String input = "Y";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        int returnCode = OrganizationApp.delete(args);
        Assert.assertEquals(returnCode, 0);
    }
}

