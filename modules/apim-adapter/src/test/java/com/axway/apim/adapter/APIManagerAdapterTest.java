package com.axway.apim.adapter;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.User;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import wiremock.org.checkerframework.checker.units.qual.A;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class APIManagerAdapterTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }


    @Test
    public void testGetHigherRoleAdmin() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        User user = new User();
        user.setRole("admin");
        Assert.assertEquals("admin", apiManagerAdapter.getHigherRole(user));
    }

    @Test
    public void testGetHigherRoleOadmin() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        User user = new User();
        user.setRole("oadmin");
        Assert.assertEquals("oadmin", apiManagerAdapter.getHigherRole(user));
    }

    @Test
    public void testGetHigherRoleUserOAdmin() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        User user = new User();
        user.setRole("user");
        Map<String, String> orgs2Role = new HashMap<>();
        orgs2Role.put("1038f4db-7453-4d47-9f29-121a057a6e1f", "oadmin");
        user.setOrgs2Role(orgs2Role);
        Assert.assertEquals("oadmin", apiManagerAdapter.getHigherRole(user));
    }

    @Test
    public void testGetHigherRoleUserAdmin() throws AppException {
        TestIndicator.getInstance().setTestRunning(true);
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        User user = new User();
        user.setRole("user");
        Map<String, String> orgs2Role = new HashMap<>();
        orgs2Role.put("1038f4db-7453-4d47-9f29-121a057a6e1f", "oadmin");
        orgs2Role.put("2038f4db-6453-3d47-8f29-221a057a6e1f", "admin");
        user.setOrgs2Role(orgs2Role);
        Assert.assertEquals("admin", apiManagerAdapter.getHigherRole(user));
    }

    public void setupParameters() throws AppException {
        APIManagerAdapter.deleteInstance();
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());

    }

    @Test
    public void loginToAPIManager() throws AppException {
        setupParameters();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        try {
            apiManagerAdapter.loginToAPIManager();
        } catch (AppException appException) {
            Assert.fail("unable to login", appException);
        }
    }

    @Test
    public void logoutFromAPIManager() throws AppException {
        setupParameters();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        try {
            apiManagerAdapter.logoutFromAPIManager();
        } catch (AppException appException) {
            Assert.fail("unable to login", appException);
        }
    }

    @Test
    public void getCurrentUser() throws AppException {
        setupParameters();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        User user = apiManagerAdapter.getCurrentUser();
        Assert.assertNotNull(user);
    }

    @Test
    public void getAppIdForCredential() throws AppException {
        setupParameters();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        ClientApplication clientApplication = apiManagerAdapter.getAppIdForCredential("extclientid", APIManagerAdapter.CREDENTIAL_TYPE_EXT_CLIENTID);
        Assert.assertNotNull(clientApplication);

    }

    @Test
    public void getAppIdForCredentialUnknown() throws AppException {
        setupParameters();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        ClientApplication clientApplication = apiManagerAdapter.getAppIdForCredential("extclientid-unknown", APIManagerAdapter.CREDENTIAL_TYPE_EXT_CLIENTID);
        Assert.assertNull(clientApplication);

    }

    @Test
    public void getCertInfo() throws AppException {
        setupParameters();
        APIManagerAdapter.getInstance();
        try (InputStream inputStream = new ByteArrayInputStream("test".getBytes())) {
            CaCert caCert = new CaCert();
            caCert.setAlias("CN=test");
            caCert.setInbound("true");
            caCert.setOutbound("false");
            JsonNode jsonNode = APIManagerAdapter.getCertInfo(inputStream, "", caCert);
            Assert.assertNotNull(jsonNode);
        } catch (IOException e) {
            Assert.fail("fail to process certificate");
        }
    }
}
