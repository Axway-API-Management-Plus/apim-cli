package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

public class APIManagerRemoteHostsAdapterTest extends WiremockWrapper {

    private APIManagerAdapter apiManagerAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();
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
    @Override
    public void close() {
        apiManagerAdapter.deleteInstance();
        super.close();
    }


    @Test
    public void getRemoteHosts() throws AppException {
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = apiManagerAdapter.getRemoteHostsAdapter();
        Map<String, RemoteHost> remoteHostMap = apiManagerRemoteHostsAdapter.getRemoteHosts(new RemoteHostFilter.Builder().build());
        Assert.assertNotNull(remoteHostMap);
    }

    @Test
    public void getRemoteHost() throws AppException {
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = apiManagerAdapter.getRemoteHostsAdapter();
        RemoteHost remoteHost = apiManagerRemoteHostsAdapter.getRemoteHost("api.axway.com", 443);
        Assert.assertNotNull(remoteHost);
    }

    @Test
    public void addRemoteHost() throws AppException {
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = apiManagerAdapter.getRemoteHostsAdapter();
        RemoteHost remoteHost = apiManagerRemoteHostsAdapter.getRemoteHost("api.axway.com", 443);
        Assert.assertNotNull(remoteHost);
        apiManagerRemoteHostsAdapter.createOrUpdateRemoteHost(remoteHost, null);
    }

    @Test
    public void updateRemoteHost() throws AppException {
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = apiManagerAdapter.getRemoteHostsAdapter();
        RemoteHost remoteHost = apiManagerRemoteHostsAdapter.getRemoteHost("api.axway.com", 443);
        Assert.assertNotNull(remoteHost);
        RemoteHost updatedRemoteHost = new RemoteHost();
        updatedRemoteHost.setName("api.demoaxway.com");
        updatedRemoteHost.setAlias("api.demoaxway.com");
        updatedRemoteHost.setId(remoteHost.getId());
        apiManagerRemoteHostsAdapter.createOrUpdateRemoteHost(remoteHost, updatedRemoteHost);
    }

    /**
     * Issue #462: The users (createdBy) of ALL remote hosts were loaded one by one, even if only one remote host is requested.
     */
    @Test
    public void usersAreOnlyLoadedForTheRequestedRemoteHosts() throws AppException {
        String user1 = UUID.randomUUID().toString();
        String user2 = UUID.randomUUID().toString();
        String user3 = UUID.randomUUID().toString();
        String remoteHosts = "[" + remoteHost("h1", user1) + "," + remoteHost("h2", user2) + "," + remoteHost("h3", user3) + "]";
        StubMapping remoteHostsStub = getWireMockServer().stubFor(WireMock.get(WireMock.urlEqualTo("/api/portal/v1.4/remotehosts")).atPriority(1)
            .willReturn(WireMock.okJson(remoteHosts)));
        try {
            APIManagerRemoteHostsAdapter remoteHostsAdapter = apiManagerAdapter.getRemoteHostsAdapter();
            getWireMockServer().resetRequests();

            RemoteHost remoteHost = remoteHostsAdapter.getRemoteHost("h2.example.com", 443);
            Assert.assertNotNull(remoteHost);
            Assert.assertEquals(remoteHost.getName(), "h2.example.com");
            Assert.assertNotNull(remoteHost.getCreatedBy().getLoginName(), "The user must be resolved for the requested remote host");
            Assert.assertNotNull(remoteHost.getOrganization().getName(), "The organization must be resolved for the requested remote host");
            Assert.assertEquals(userRequests(user1) + userRequests(user3), 0, "The users of other remote hosts must not be loaded");
            Assert.assertEquals(userRequests(user2), 1);

            // Requesting all remote hosts loads all users, but each only once
            Map<String, RemoteHost> all = remoteHostsAdapter.getRemoteHosts(new RemoteHostFilter.Builder().hasName("h*.example.com").build());
            Assert.assertEquals(all.size(), 3);
            for (RemoteHost host : all.values()) {
                Assert.assertNotNull(host.getCreatedBy().getLoginName(), "User not resolved for " + host.getName());
            }
            Assert.assertEquals(userRequests(user1), 1);
            Assert.assertEquals(userRequests(user2), 1);
            Assert.assertEquals(userRequests(user3), 1);
        } finally {
            getWireMockServer().removeStub(remoteHostsStub);
        }
    }

    @Test
    public void remoteHostsCanBeFilteredByOrganization() throws AppException {
        String remoteHosts = "[" + remoteHost("o1", UUID.randomUUID().toString()) + "," + remoteHost("o2", UUID.randomUUID().toString()) + "]";
        StubMapping remoteHostsStub = getWireMockServer().stubFor(WireMock.get(WireMock.urlEqualTo("/api/portal/v1.4/remotehosts")).atPriority(1)
            .willReturn(WireMock.okJson(remoteHosts)));
        try {
            APIManagerRemoteHostsAdapter remoteHostsAdapter = apiManagerAdapter.getRemoteHostsAdapter();
            Organization organization = apiManagerAdapter.getOrgAdapter().getOrgForId("987b2afc-b027-41fd-a920-bef182eb4a94");
            Assert.assertNotNull(organization);
            // The organization is required to filter, hence it is loaded before filtering
            RemoteHostFilter organizationFilter = new RemoteHostFilter.Builder().hasName("o*.example.com").build();
            organizationFilter.setOrganization(organization);
            Map<String, RemoteHost> found = remoteHostsAdapter.getRemoteHosts(organizationFilter);
            Assert.assertEquals(found.size(), 2);
            for (RemoteHost host : found.values()) {
                Assert.assertEquals(host.getOrganization().getName(), organization.getName());
            }
            Organization otherOrganization = new Organization();
            otherOrganization.setName("unknown-organization");
            RemoteHostFilter otherOrganizationFilter = new RemoteHostFilter.Builder().hasName("o*.example.com").build();
            otherOrganizationFilter.setOrganization(otherOrganization);
            Map<String, RemoteHost> none = remoteHostsAdapter.getRemoteHosts(otherOrganizationFilter);
            Assert.assertTrue(none.isEmpty());
        } finally {
            getWireMockServer().removeStub(remoteHostsStub);
        }
    }

    private String remoteHost(String name, String createdBy) {
        return "{\"id\":\"" + UUID.randomUUID() + "\",\"alias\":\"" + name + "\",\"name\":\"" + name + ".example.com\",\"port\":443,\"createdOn\":1700000000000,"
            + "\"createdBy\":\"" + createdBy + "\",\"organizationId\":\"987b2afc-b027-41fd-a920-bef182eb4a94\"}";
    }

    private int userRequests(String userId) {
        return getWireMockServer().countRequestsMatching(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/portal/v1.4/users/" + userId)).build()).getCount();
    }
}
