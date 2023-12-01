package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.client.apps.ClientAppFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.*;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.specification.APISpecification;
import com.axway.apim.api.specification.APISpecificationFactory;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class APIManagerAPIAdapterTest extends WiremockWrapper {

    APIManagerAPIAdapter apiManagerAPIAdapter;

    private APIManagerAdapter apiManagerAdapter;
    private APIManagerOrganizationAdapter orgAdapter;



    @BeforeClass
    public void init() {
        try {
            initWiremock();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAdapter = APIManagerAdapter.getInstance();
            apiManagerAPIAdapter = apiManagerAdapter.getApiAdapter();
            orgAdapter = apiManagerAdapter.getOrgAdapter();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        apiManagerAdapter.deleteInstance();
        super.close();
    }

    @Test
    public void testUniqueAPIWithAPIPath() throws AppException {
        API noVHostAPI = createTestAPI("/api/v1/resource", null, null);
        API vhostAPI = createTestAPI("/api/v1/other", "api.customer.com", null);
        API anotherVHostAPI = createTestAPI("/api/v1/other", "otherapi.customer.com", null);
        List<API> testAPIs = new ArrayList<>();
        testAPIs.add(noVHostAPI);
        testAPIs.add(vhostAPI);
        testAPIs.add(anotherVHostAPI);

        APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").build();

        // Must return the default API (testAPI1) as we don't filter specifically for the V-Host
        API uniqueAPI = apiManagerAPIAdapter.getUniqueAPI(testAPIs, filter);
        Assert.assertEquals(uniqueAPI, noVHostAPI);
    }

    @Test
    public void testUniqueAPISamePathDiffQueryString() throws AppException {
        API queryVersionAPI = createTestAPI("/api/v1/resource", null, "1.0");
        API noQueryVersionAPI = createTestAPI("/api/v1/resource", null, null);
        API anotherQueryVersionAPI = createTestAPI("/api/v1/resource", null, "2.0");
        API nextTextAPI = createTestAPI("/api/v1/resource", "api.customer.com", "3.0");
        List<API> testAPIs = new ArrayList<>();
        testAPIs.add(queryVersionAPI);
        testAPIs.add(noQueryVersionAPI);
        testAPIs.add(anotherQueryVersionAPI);
        testAPIs.add(nextTextAPI);

        APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").build();

        // Must return testAPI2 as the default, because no queryStringVersion is given in the filter
        API uniqueAPI = apiManagerAPIAdapter.getUniqueAPI(testAPIs, filter);
        Assert.assertEquals(uniqueAPI, noQueryVersionAPI);
    }

    @Test
    public void testUniqueAPIVHostDefault() throws AppException {
        API queryVersionAPI = createTestAPI("/api/v1/resource", "api.customer.com", "1.0");
        API noQueryVersionAPI = createTestAPI("/api/v1/resource", "api.customer.com", null);
        API anotherQueryVersionAPI = createTestAPI("/api/v1/resource", null, "2.0");
        List<API> testAPIs = new ArrayList<>();
        testAPIs.add(queryVersionAPI);
        testAPIs.add(noQueryVersionAPI);
        testAPIs.add(anotherQueryVersionAPI);

        APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasVHost("api.customer.com").build();

        // Must return testAPI2 which is the default for the requested VHost as this API has not QueryVersion
        API uniqueAPI = apiManagerAPIAdapter.getUniqueAPI(testAPIs, filter);
        Assert.assertEquals(uniqueAPI, noQueryVersionAPI);
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "No unique API found.*")
    public void testNoUniqueFoundWithQueryVersion() throws AppException {
        API testAPI1 = createTestAPI("/api/v1/resource", null, "1.0");
        API testAPI2 = createTestAPI("/api/v1/resource", null, "1.0");
        List<API> testAPIs = new ArrayList<>();
        testAPIs.add(testAPI1);
        testAPIs.add(testAPI2);

        APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasQueryStringVersion("1.0").build();

        // Must fail (throw an Exception) as the API is really not unique, even if we filter with the QueryVersion
        API uniqueAPI = apiManagerAPIAdapter.getUniqueAPI(testAPIs, filter);
        Assert.assertEquals(uniqueAPI, testAPI2);
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "No unique API found.*")
    public void testNoUniqueFoundWithQueryVersionAndVHost() throws AppException {
        API testAPI1 = createTestAPI("/api/v1/resource", "host.customer.com", "1.0");
        API testAPI2 = createTestAPI("/api/v1/resource", "host.customer.com", "1.0");
        List<API> testAPIs = new ArrayList<>();
        testAPIs.add(testAPI1);
        testAPIs.add(testAPI2);

        APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasQueryStringVersion("1.0").hasVHost("host.customer.com").build();

        // Must fail (throw an Exception) as the API is really not unique, even if we filter with the QueryVersion and VHost
        API uniqueAPI = apiManagerAPIAdapter.getUniqueAPI(testAPIs, filter);
        Assert.assertEquals(uniqueAPI, testAPI2);
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "No unique API found.*")
    public void testNoUniqueFoundMixedVHost() throws AppException {
        API testAPI1 = createTestAPI("/api/v1/resource", "host.customer.com", "1.0");
        API testAPI2 = createTestAPI("/api/v1/resource", "other.customer.com", "1.0");
        List<API> testAPIs = new ArrayList<>();
        testAPIs.add(testAPI1);
        testAPIs.add(testAPI2);

        APIFilter filter = new APIFilter.Builder().hasApiPath("/api/v1/resource").hasQueryStringVersion("1.0").build();

        // Must fail (throw an Exception) as the API is really not unique, if we filter with the QueryVersion only
        API uniqueAPI = apiManagerAPIAdapter.getUniqueAPI(testAPIs, filter);
        Assert.assertEquals(uniqueAPI, testAPI2);
    }

    private static API createTestAPI(String apiPath, String vhost, String queryVersion) {
        API testAPI = new API();
        testAPI.setPath(apiPath);
        testAPI.setVhost(vhost);
        testAPI.setApiRoutingKey(queryVersion);
        return testAPI;
    }

    @Test
    public void createAPIProxy() throws AppException {
        APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.getApiAdapter();
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("published");
        api.setOrganization(organization);
        API createdAPI = apiManagerAPIAdapter.createAPIProxy(api);
        Assert.assertNotNull(createdAPI);
    }

    @Test
    public void updateAPIProxy() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("published");
        api.setOrganization(organization);
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        API updateAPI = apiManagerAPIAdapter.updateAPIProxy(api);
        Assert.assertEquals(updateAPI.getState(), "published");
    }

    @Test
    public void deleteAPIProxy() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("published");
        api.setOrganization(organization);
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        try {
            apiManagerAPIAdapter.deleteAPIProxy(api);
        } catch (AppException e) {
            Assert.fail("Unable to delete API", e);
        }
    }

    @Test
    public void deleteBackendAPI() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("published");
        api.setOrganization(organization);
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        api.setApiId("1f4263ca-7f03-41d9-9d34-9eff79d29bd8");
        try {
            apiManagerAPIAdapter.deleteBackendAPI(api);
        } catch (AppException e) {
            Assert.fail("Unable to delete backend API", e);
        }
    }

    @Test
    public void publishAPIDoNothing() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("published");
        api.setOrganization(organization);
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        try {
            apiManagerAPIAdapter.publishAPI(api, "api.axway.com");
        } catch (AppException e) {
            Assert.fail("Unable to publish API", e);
        }
    }

    @Test
    public void publishAPI() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("unpublished");
        api.setOrganization(organization);
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");

        try {
            apiManagerAPIAdapter.publishAPI(api, "api.axway.com");
        } catch (AppException e) {
            Assert.fail("Unable to publish API", e);
        }
    }

    @Test
    public void getAPIDatFile() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("published");
        api.setOrganization(organization);
        byte[] apiExportDat = apiManagerAPIAdapter.getAPIDatFile(api, Utils.getEncryptedPassword());
        Assert.assertNotNull(apiExportDat);
    }

    @Test
    public void updateAPIStatus() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("unpublished");
        api.setOrganization(organization);
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");

        try {
            apiManagerAPIAdapter.updateAPIStatus(api, "published", "api.axway.com");
        } catch (AppException e) {
            Assert.fail("Unable to update API status", e);
        }
    }

    @Test
    public void updateRetirementDateDoNothing() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("unpublished");
        api.setOrganization(organization);
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 30);

        try {
            apiManagerAPIAdapter.updateRetirementDate(api, calendar.getTimeInMillis());
        } catch (AppException e) {
            Assert.fail("Unable to update Retirement status", e);
        }
    }

    @Test
    public void updateRetirementDate() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("deprecated");
        api.setOrganization(organization);
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 30);

        try {
            apiManagerAPIAdapter.updateRetirementDate(api, calendar.getTimeInMillis());
        } catch (AppException e) {
            Assert.fail("Unable to update Retirement status", e);
        }
    }

    @Test
    public void importBackendAPI() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("unpublished");
        api.setOrganization(organization);
        ClassLoader classLoader = this.getClass().getClassLoader();
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec/").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("openapi.json", specDirPath, "petstore");
        api.setApiDefinition(apiSpecification);
        API importedAPI = apiManagerAPIAdapter.importBackendAPI(api, null);
        Assert.assertNotNull(importedAPI);
    }

    @Test
    public void importBackendAPIWsdl() throws AppException {
        Organization organization = orgAdapter.getOrgForName("orga");
        API api = new API();
        api.setName("petstore");
        api.setPath("/api/v3");
        api.setVersion("1.1");
        api.setDescriptionType("original");
        api.setSummary("Petstore api");
        api.setState("unpublished");
        api.setOrganization(organization);
        ClassLoader classLoader = this.getClass().getClassLoader();
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec/").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("sample.wsdl", specDirPath, "wsdl");
        api.setApiDefinition(apiSpecification);
        API importedAPI = apiManagerAPIAdapter.importBackendAPI(api, null);
        Assert.assertNotNull(importedAPI);
    }

    @Test
    public void getAPIWithId() throws AppException {
        API api = apiManagerAPIAdapter.getAPIWithId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        Assert.assertNotNull(api);
    }

    @Test
    public void updateAPIImage(){
        try {
            API api = apiManagerAPIAdapter.getAPIWithId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
            String filePath = this.getClass().getClassLoader().getResource("com/axway/apim/images/API-Logo.jpg").getFile();
            System.out.println(filePath);
            Image image = Image.createImageFromFile(new File(filePath));
            apiManagerAPIAdapter.updateAPIImage(api, image);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void addClientApplications(){
        try {
            API api = apiManagerAPIAdapter.getAPIWithId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
            apiManagerAPIAdapter.addClientApplications(api);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void grantClientOrganizationAll(){
        try {
            Organization organization = orgAdapter.getOrgForName("orga");
            List<Organization> organizations = new ArrayList<>();
            organizations.add(organization);
            API api = apiManagerAPIAdapter.getAPIWithId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
            apiManagerAPIAdapter.grantClientOrganization(organizations, api, true);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void grantClientApplication(){
        try {
            ClientAppFilter clientAppFilter = new ClientAppFilter.Builder()
                .hasName("Test App 2008")
                .build();
            ClientApplication clientApplication = apiManagerAdapter.getAppAdapter().getApplication(clientAppFilter);
            API api = apiManagerAPIAdapter.getAPIWithId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
            apiManagerAPIAdapter.grantClientApplication(clientApplication, api);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void revokeClientOrganization(){
        try {
            Organization organization = orgAdapter.getOrgForName("orga");
            List<Organization> organizations = new ArrayList<>();
            organizations.add(organization);
            API api = apiManagerAPIAdapter.getAPIWithId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
            apiManagerAPIAdapter.revokeClientOrganization(organizations, api);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void revokeClientApplication(){
        try {
            ClientAppFilter clientAppFilter = new ClientAppFilter.Builder()
                .hasName("Test App 2008")
                .build();
            ClientApplication clientApplication = apiManagerAdapter.getAppAdapter().getApplication(clientAppFilter);
            API api = apiManagerAPIAdapter.getAPIWithId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
            apiManagerAPIAdapter.revokeClientApplication(clientApplication, api);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void grantClientOrganization(){
        try {
            Organization organization = orgAdapter.getOrgForName("orga");
            List<Organization> organizations = new ArrayList<>();
            organizations.add(organization);
            API api = apiManagerAPIAdapter.getAPIWithId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
            apiManagerAPIAdapter.grantClientOrganization(organizations, api, false);
        }catch (AppException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void upgradeAccessToNewerAPI() throws AppException {
        APIFilter filter = new APIFilter.Builder()
            .hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
            .build();
        API api = apiManagerAPIAdapter.getAPI(filter, true);
        api.setApplications(new ArrayList<>());
        apiManagerAPIAdapter.upgradeAccessToNewerAPI(api, api);
    }

    @Test
    public void loadActualAPI() throws IOException {
        APIFilter filter = new APIFilter.Builder()
                .hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
                .build();
        API api = apiManagerAPIAdapter.getAPI(filter, true);
        Assert.assertNotNull(api.getOrganization(), "API should have an organization");
    }

    @Test
    public void testTranslateMethodToName() throws IOException {
        APIFilter filter = new APIFilter.Builder()
                .translateMethods(APIFilter.METHOD_TRANSLATION.AS_NAME)
                .hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
                .build();
        API api = apiManagerAPIAdapter.getAPI(filter, true);

        Assert.assertEquals(api.getOutboundProfiles().size(), 2);
        Map<String, OutboundProfile> outboundProfiles = api.getOutboundProfiles();
        Assert.assertNotNull(outboundProfiles.get("_default"), "Expected to find default outbound profile");
        Assert.assertNotNull(outboundProfiles.get("deletePet").getApiMethodId(), "deletePet");
        Assert.assertEquals(outboundProfiles.get("deletePet").getApiId(), "1f4263ca-7f03-41d9-9d34-9eff79d29bd8");
        Assert.assertEquals(outboundProfiles.get("deletePet").getApiMethodId(), "7a0c6ce1-187b-47bc-b4dc-cfa1f12aa042");
        Assert.assertEquals(outboundProfiles.get("deletePet").getApiMethodName(), "deletePet");

        Assert.assertEquals(api.getInboundProfiles().size(), 2);
        Map<String, InboundProfile> inboundProfiles = api.getInboundProfiles();
        Assert.assertNotNull(inboundProfiles.get("_default"), "Expected to find default outbound profile");
        Assert.assertNotNull(inboundProfiles.get("createUser"), "Expected to find Inbound profile named createdUser");
    }

    @Test
    public void testTranslateMethodToId() throws IOException {
        APIFilter filter = new APIFilter.Builder()
                .hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
                .build();
        API api = apiManagerAPIAdapter.getAPI(filter, true);

        Assert.assertEquals(api.getOutboundProfiles().size(), 2);
        Map<String, OutboundProfile> outboundProfiles = api.getOutboundProfiles();
        Assert.assertNotNull(outboundProfiles.get("_default"), "Expected to find default outbound profile");
        Assert.assertNotNull(outboundProfiles.get("bf742930-5fdd-4e49-b9ae-b8fdecd37ffa"), "Expected to get deletePet based on the method per ID");

        Assert.assertEquals(api.getInboundProfiles().size(), 2);
        Map<String, InboundProfile> inboundProfiles = api.getInboundProfiles();
        Assert.assertNotNull(inboundProfiles.get("_default"), "Expected to find default outbound profile");
        Assert.assertNotNull(inboundProfiles.get("a544b66c-6f2b-4c00-999c-6816d482bcde"), "Expected to get createUser based on the method per ID");
    }

    @Test
    public void testTranslatePolicyToExternalName() throws IOException {
        // Get the API to test with
        APIFilter filter = new APIFilter.Builder()
                .translatePolicies(APIFilter.POLICY_TRANSLATION.TO_NAME)
                .hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
                .build();

        API api = apiManagerAPIAdapter.getAPI(filter, true);

        Assert.assertEquals(api.getOutboundProfiles().get("_default").getRequestPolicy().getName(), "Validate Size & Token");
        Assert.assertEquals(api.getOutboundProfiles().get("_default").getRoutePolicy().getName(), "Http Proxy Router");
        Assert.assertEquals(api.getOutboundProfiles().get("_default").getResponsePolicy().getName(), "Remove Header & Audit data");
        Assert.assertEquals(api.getOutboundProfiles().get("_default").getFaultHandlerPolicy().getName(), "Default Fault Handler");
    }

    @Test
    public void loadAPIIncludingQuota() throws IOException {
        APIFilter filter = new APIFilter.Builder()
                .includeQuotas(true)
                .includeClientApplications(true)
                .hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
                .build();

        API api = apiManagerAPIAdapter.getAPI(filter, true);

        Assert.assertNotNull(api.getSystemQuota(), "Should have a system quota");
        Assert.assertEquals(api.getSystemQuota().getRestrictions().size(), 1, "Expected one system quota restrictions");
        Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getType(), QuotaRestrictionType.throttle);
        Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getConfig().get("messages"), "1000");
        Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getConfig().get("period"), "second");
        Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getConfig().get("per"), "2");

        Assert.assertNotNull(api.getApplicationQuota(), "Should have an application default quota");
        Assert.assertEquals(api.getApplicationQuota().getRestrictions().size(), 1, "Expected one system quota restrictions");
    }

    @Test
    public void loadAPIIncludingClientOrgs() throws IOException {
        APIFilter filter = new APIFilter.Builder()
                .includeClientOrganizations(true)
                .hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
                .build();

        API api = apiManagerAPIAdapter.getAPI(filter, true);

        Assert.assertNotNull(api.getClientOrganizations(), "Should have a some client organizations");
        Assert.assertEquals(api.getClientOrganizations().size(), 1, "Expected client organization");
    }

    @Test
    public void loadAPIIncludingClientApps() throws IOException {
        APIFilter filter = new APIFilter.Builder()
                .includeClientApplications(true)
                .includeQuotas(true)
                .hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
                .build();
        API api = apiManagerAPIAdapter.getAPI(filter, true);

        Assert.assertNotNull(api.getApplications(), "Should have a some client applications");
        Assert.assertEquals(api.getApplications().size(), 1, "Should have a some client applications");
        Assert.assertEquals(api.getApplications().get(0).getId(), "5feb31fe-fb8b-4c21-8132-39f01e2c6440", "We should have a an API-Access for the test api");

        Assert.assertNotNull(api.getApplications(), "should have a subscribed application");
        Assert.assertNotNull(api.getApplications().get(0).getAppQuota(), "Subscribed application should have a quota");
    }

    @Test
    public void pollCatalog() throws AppException {
        boolean status = apiManagerAPIAdapter.pollCatalogForPublishedState("e4ded8c8-0a40-4b50-bc13-552fb7209150", "Test-App-API2-4618", "published");
        Assert.assertTrue(status);
    }

    @Test
    public void pollCatalogUnpublished() throws AppException {
        boolean status = apiManagerAPIAdapter.pollCatalogForPublishedState("e4ded8c8-0a40-4b50-bc13-552fb7209150", "Test-App-API2-4618", "unpublished");
        Assert.assertTrue(status);
    }

    @Test(expectedExceptions = AppException.class)
    public void pollCatalogException() throws AppException {
        boolean status = apiManagerAPIAdapter.pollCatalogForPublishedState("d90122a7-9f47-420c-85ca-926125ea7bf6-invalid", "Test-App-API2-4618", "published");
        Assert.assertFalse(status);
    }

    @Test
    public void isBackendApiExists(){
        API api = new API();
        api.setApiId("1f4263ca-7f03-41d9-9d34-9eff79d29bd8");
        Assert.assertTrue(apiManagerAPIAdapter.isBackendApiExists(api));
    }
    @Test
    public void isBackendApiNotExists(){
        API api = new API();
        api.setApiId("1f4263ca-7f03-41d9-9d34-9eff79d29bd8-not");
        Assert.assertFalse(apiManagerAPIAdapter.isBackendApiExists(api));
    }

    @Test
    public void isFrontendApiExists(){
        API api = new API();
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        Assert.assertTrue(apiManagerAPIAdapter.isFrontendApiExists(api));
    }
    @Test
    public void isFrontendApiNotExists(){
        API api = new API();
        api.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150-not");
        Assert.assertFalse(apiManagerAPIAdapter.isFrontendApiExists(api));
    }


    @Test
    public void importFromWSDL() throws IOException {
        API api = new API();
        api.setName("wsdl");
        api.setApiDefinition(new APISpecification() {

            public String getApiSpecificationFile() {
                return "https://localhost/services/test.wsdl";
            }

            @Override
            public byte[] getApiSpecificationContent() {
                return  "test".getBytes();
            }

            @Override
            public void updateBasePath(String basePath, String host) {

            }

            @Override
            public void configureBasePath(String backendBasePath, API api) throws AppException {

            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public APISpecType getAPIDefinitionType() throws AppException {
                return null;
            }

            @Override
            public boolean parse(byte[] apiSpecificationContent) throws AppException {
                return false;
            }
        });
        Organization organization = new Organization();
        organization.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        api.setOrganization(organization);
        JsonNode jsonNode = apiManagerAPIAdapter.importFromWSDL(api);
        System.out.println(jsonNode);
        Assert.assertEquals("Test-App-API1-2285", jsonNode.get("name").asText());
    }

    @Test
    public void importFromSwagger() throws AppException {
        API api = new API();
        api.setName("Test-App-API1-2285");
        api.setApiDefinition(new APISpecification() {
            @Override
            public byte[] getApiSpecificationContent() {
                return  "test".getBytes();
            }

            @Override
            public void updateBasePath(String basePath, String host) {

            }

            @Override
            public void configureBasePath(String backendBasePath, API api) throws AppException {

            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public APISpecType getAPIDefinitionType() throws AppException {
                return null;
            }

            @Override
            public boolean parse(byte[] apiSpecificationContent) throws AppException {
                return false;
            }
        });
        Organization organization = new Organization();
        organization.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        api.setOrganization(organization);
        JsonNode jsonNode = apiManagerAPIAdapter.importFromSwagger(api);
        Assert.assertEquals("Test-App-API1-2285", jsonNode.get("name").asText());
    }


    @Test
    public void importGraphql() throws AppException {
        API api = new API();
        api.setName("graph");
        api.setApiDefinition(new APISpecification() {
            @Override
            public byte[] getApiSpecificationContent() {
                return  "test".getBytes();
            }

            @Override
            public void updateBasePath(String basePath, String host) {

            }

            @Override
            public void configureBasePath(String backendBasePath, API api) throws AppException {

            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public APISpecType getAPIDefinitionType() throws AppException {
                return null;
            }

            @Override
            public boolean parse(byte[] apiSpecificationContent) throws AppException {
                return false;
            }
        });
        Organization organization = new Organization();
        organization.setId("e4ded8c8-0a40-4b50-bc13-552fb7209150");
        api.setOrganization(organization);
        JsonNode jsonNode = apiManagerAPIAdapter.importGraphql(api, "https://localhost/graphql");
        Assert.assertEquals("graph", jsonNode.get("name").asText());
    }


}
