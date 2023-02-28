package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

public class APIManagerRemoteHostsAdapterTest extends WiremockWrapper {

    private APIManagerAdapter apiManagerAdapter;

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
    public void getRemoteHosts() throws AppException {
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = apiManagerAdapter.remoteHostsAdapter;
        Map<String, RemoteHost> remoteHostMap = apiManagerRemoteHostsAdapter.getRemoteHosts(new RemoteHostFilter.Builder().build());
        Assert.assertNotNull(remoteHostMap);
    }

    @Test
    public void getRemoteHost() throws AppException {
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = apiManagerAdapter.remoteHostsAdapter;
        RemoteHost remoteHost = apiManagerRemoteHostsAdapter.getRemoteHost("api.axway.com", 443);
        Assert.assertNotNull(remoteHost);
    }

    @Test
    public void addRemoteHost() throws AppException {
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = apiManagerAdapter.remoteHostsAdapter;
        RemoteHost remoteHost = apiManagerRemoteHostsAdapter.getRemoteHost("api.axway.com", 443);
        Assert.assertNotNull(remoteHost);
        apiManagerRemoteHostsAdapter.createOrUpdateRemoteHost(remoteHost, null);
    }

    @Test
    public void updateRemoteHost() throws AppException {
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = apiManagerAdapter.remoteHostsAdapter;
        RemoteHost remoteHost = apiManagerRemoteHostsAdapter.getRemoteHost("api.axway.com", 443);
        Assert.assertNotNull(remoteHost);
        RemoteHost updatedRemoteHost = new RemoteHost();
        updatedRemoteHost.setName("api.demoaxway.com");
        updatedRemoteHost.setAlias("api.demoaxway.com");
        updatedRemoteHost.setId(remoteHost.getId());
        apiManagerRemoteHostsAdapter.createOrUpdateRemoteHost(remoteHost, updatedRemoteHost);
    }

}
