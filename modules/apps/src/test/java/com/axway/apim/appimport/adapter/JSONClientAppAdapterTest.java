package com.axway.apim.appimport.adapter;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictiontype;
import com.axway.apim.api.model.apps.*;
import com.axway.apim.appimport.lib.AppImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static org.testng.Assert.*;

public class JSONClientAppAdapterTest extends WiremockWrapper {
    private static final String testPackage = "/com/axway/apim/appimport/adapter";


    @BeforeClass
    public void init() {
        initWiremock();
        TestIndicator.getInstance().setTestRunning(true);
    }

    @AfterClass
    public void stop() {
        close();
    }

    @Test
    public void testSingleAppAsArray() throws AppException {
        String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/SingleClientAppAsArray.json").getPath();
        assertTrue(new File(testFile).exists(), "Test file doesn't exists");
        AppImportParams importParams = new AppImportParams();
        importParams.setHostname("localhost");
        importParams.setConfig(testFile);
        ClientAppAdapter adapter = new ClientAppConfigAdapter(importParams);
        List<ClientApplication> apps = adapter.getApplications();
        assertEquals(apps.size(), 1, "Expected 1 app returned from the Adapter");
    }

    @Test
    public void testSingleApp() throws AppException {
        String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/OneSingleClientApp.json").getPath();
        assertTrue(new File(testFile).exists(), "Test file doesn't exists");
        AppImportParams importParams = new AppImportParams();
        importParams.setHostname("localhost");
        importParams.setConfig(testFile);
        ClientAppAdapter adapter = new ClientAppConfigAdapter(importParams);
        List<ClientApplication> apps = adapter.getApplications();
        assertEquals(apps.size(), 1, "Expected 1 app returned from the Adapter");
    }

    @Test
    public void testSingleAppWithStage() throws AppException {
        String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/OneSingleClientApp.json").getPath();
        assertTrue(new File(testFile).exists(), "Test file doesn't exists");
        AppImportParams importParams = new AppImportParams();
        importParams.setHostname("localhost");
        importParams.setConfig(testFile);
        importParams.setStage("test-stage");
        ClientAppAdapter adapter = new ClientAppConfigAdapter(importParams);
        List<ClientApplication> apps = adapter.getApplications();
        assertEquals(apps.size(), 1, "Expected 1 app returned from the Adapter");
        ClientApplication app = apps.get(0);
        assertEquals(app.getName(), "Staged Application");

    }

    @Test
    public void testMultipleApps() throws AppException {
        String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/MulitpleTestApplications.json").getPath();
        assertTrue(new File(testFile).exists(), "Test file doesn't exists");
        AppImportParams importParams = new AppImportParams();
        importParams.setHostname("localhost");
        importParams.setConfig(testFile);
        ClientAppAdapter adapter = new ClientAppConfigAdapter(importParams);
        List<ClientApplication> apps = adapter.getApplications();
        assertEquals(apps.size(), 2, "Expected 2 app returned from the Adapter");
    }

    @Test(expectedExceptions = AppException.class)
    public void testMultipleAppsWithStage() throws AppException {
        String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/MulitpleTestApplications.json").getPath();
        assertTrue(new File(testFile).exists(), "Test file doesn't exists");
        AppImportParams importParams = new AppImportParams();
        importParams.setConfig(testFile);
        importParams.setHostname("localhost");
        importParams.setStage("test-stage");
        importParams.setHostname("localhost");

        ClientAppAdapter adapter = new ClientAppConfigAdapter(importParams);
        adapter.getApplications();
    }

    @Test
    public void testCompleteApp() throws AppException {
        String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/CompleteApplication.json").getPath();
        assertTrue(new File(testFile).exists(), "Test file doesn't exists");
        AppImportParams importParams = new AppImportParams();
        importParams.setHostname("localhost");
        importParams.setConfig(testFile);
        ClientAppAdapter adapter = new ClientAppConfigAdapter(importParams);

        List<ClientApplication> apps = adapter.getApplications();
        assertEquals(apps.size(), 1, "Expected 1 app returned from the Adapter");
        ClientApplication app = apps.get(0);
        assertEquals(app.getName(), "Complete application");
        assertEquals(app.getDescription(), "Sample Client Application, registered for use in the Client Demo");
        assertEquals(app.getImageUrl(), "app-image.jpg");
        assertTrue(app.getImage() instanceof Image);
        assertNotNull(app.getImage().getImageContent(), "No image content");
        assertEquals(app.isEnabled(), true);
        assertEquals(app.getEmail(), "sample@sampleapp.com");
        assertEquals(app.getPhone(), "012345678");
        assertNotNull(app.getCredentials(), "getCredentials is null");
        assertEquals(app.getCredentials().size(), 2, "Expected 2 credentials");

        ClientAppCredential oauthCred = app.getCredentials().get(0);
        assertTrue(oauthCred instanceof OAuth, "Expected OAuth credentials");
        assertEquals(oauthCred.getId(), "ClientConfidentialApp");
        assertTrue(((OAuth) oauthCred).getCert().startsWith("-----BEGIN CERTIFICATE-----"), "Expecte OAuth-Cert: '" + ((OAuth) oauthCred).getCert() + "' to start with -----BEGIN CERTIFICATE-----");

        ClientAppCredential apikeyCred = app.getCredentials().get(1);
        assertTrue(apikeyCred instanceof APIKey, "Expected APIKey credentials");
        assertEquals(apikeyCred.getId(), "6cd55c27-675a-444a-9bc7-ae9a7869184d");

        APIQuota appQuota = app.getAppQuota();
        assertNotNull(appQuota, "appQuota is null");
        assertNotNull(appQuota.getRestrictions(), "appQuota restrictions are null");
        assertEquals(appQuota.getRestrictions().size(), 1, "Expected one restriction");
        QuotaRestriction restr = appQuota.getRestrictions().get(0);
        assertEquals(restr.getApiId(), "*");
        assertEquals(restr.getMethod(), "*");
        assertEquals(restr.getType(), QuotaRestrictiontype.throttle);
        assertEquals(restr.getConfig().get("messages"), "9999");
        assertEquals(restr.getConfig().get("period"), "week");
        assertEquals(restr.getConfig().get("per"), "1");

        List<ClientAppOauthResource> oauthResources = app.getOauthResources();
        assertNotNull(oauthResources, "oauthResources is null");
        assertEquals(oauthResources.size(), 2, "Expected two OAuth resources");
        ClientAppOauthResource oauthRes = oauthResources.get(0);
        assertEquals(oauthRes.getScope(), "resource.READ");
        assertEquals(oauthRes.isEnabled(), true);
    }

    @Test
    public void testAppWithQuotaBasedOnAPIName() throws AppException {
        String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/AppWithQuotaPerAPIName.json").getPath();
        assertTrue(new File(testFile).exists(), "Test file doesn't exists");
        AppImportParams importParams = new AppImportParams();
        importParams.setHostname("localhost");
        importParams.setConfig(testFile);
        ClientAppAdapter adapter = new ClientAppConfigAdapter(importParams);

        List<ClientApplication> apps = adapter.getApplications();
        assertEquals(apps.size(), 1, "Expected 1 app returned from the Adapter");
        ClientApplication app = apps.get(0);
        assertEquals(app.getName(), "Application with quota");
        assertEquals(app.getDescription(), "Application that configured quota per API-Name");

        APIQuota appQuota = app.getAppQuota();
        assertNotNull(appQuota, "appQuota is null");
        assertNotNull(appQuota.getRestrictions(), "appQuota restrictions are null");
        assertEquals(appQuota.getRestrictions().size(), 3, "Expected two restrictions");
        QuotaRestriction restr1 = appQuota.getRestrictions().get(0);
        QuotaRestriction restr2 = appQuota.getRestrictions().get(1);
        QuotaRestriction restr3 = appQuota.getRestrictions().get(2);

        assertEquals(restr1.getApiId(), "*");
        assertEquals(restr1.getMethod(), "*");
        assertEquals(restr1.getType(), QuotaRestrictiontype.throttle);
        assertEquals(restr1.getConfig().get("messages"), "9999");
        assertEquals(restr1.getConfig().get("period"), "week");
        assertEquals(restr1.getConfig().get("per"), "1");


        assertEquals(restr2.getApiId(), "e4ded8c8-0a40-4b50-bc13-552fb7209150");
        assertEquals(restr2.getRestrictedAPI().getName(), "petstore3");
        assertEquals(restr2.getMethod(), "*");
        assertEquals(restr3.getMethod(), "dfd61eee-cf2d-4d97-bbb6-f602fd2063bd");

    }

    @Test
    public void testAppWithQuotaBasedOnAPIPath() throws AppException {
        String testFile = JSONClientAppAdapterTest.class.getResource(testPackage + "/AppWithQuotaPerAPIPath.json").getPath();
        assertTrue(new File(testFile).exists(), "Test file doesn't exists");
        AppImportParams importParams = new AppImportParams();
        importParams.setConfig(testFile);
        importParams.setHostname("localhost");
        ClientAppAdapter adapter = new ClientAppConfigAdapter(importParams);

        List<ClientApplication> apps = adapter.getApplications();
        assertEquals(apps.size(), 1, "Expected 1 app returned from the Adapter");
        ClientApplication app = apps.get(0);
        assertEquals(app.getName(), "Application with quota");
        assertEquals(app.getDescription(), "Application that configured quota per API-Path");

        APIQuota appQuota = app.getAppQuota();
        assertNotNull(appQuota, "appQuota is null");
        assertNotNull(appQuota.getRestrictions(), "appQuota restrictions are null");
        assertEquals(appQuota.getRestrictions().size(), 1, "Expected one restriction");
        QuotaRestriction restr1 = appQuota.getRestrictions().get(0);
        assertEquals(restr1.getApiId(), "e4ded8c8-0a40-4b50-bc13-552fb7209150");
        assertEquals(restr1.getRestrictedAPI().getName(), "petstore3");
        assertEquals(restr1.getRestrictedAPI().getPath(), "/api/v3");
        assertEquals(restr1.getMethod(), "*");
        assertEquals(restr1.getType(), QuotaRestrictiontype.throttle);
        assertEquals(restr1.getConfig().get("messages"), "9999");
        assertEquals(restr1.getConfig().get("period"), "week");
        assertEquals(restr1.getConfig().get("per"), "1");
    }
}
