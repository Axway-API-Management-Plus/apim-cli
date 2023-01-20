package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

public class APIManagerRemoteHostsAdapterTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    public void setupParameters() throws AppException {
        APIManagerAdapter.deleteInstance();
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());
    }

    @Test
    public void getRemoteHosts() throws AppException {
        setupParameters();
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = APIManagerAdapter.getInstance().remoteHostsAdapter;
        Map<String, RemoteHost> remoteHostMap = apiManagerRemoteHostsAdapter.getRemoteHosts(new RemoteHostFilter.Builder().build());
        Assert.assertNotNull(remoteHostMap);
    }

    @Test
    public void getRemoteHost() throws AppException {
        setupParameters();
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = APIManagerAdapter.getInstance().remoteHostsAdapter;
        RemoteHost remoteHost = apiManagerRemoteHostsAdapter.getRemoteHost("api.axway.com", 443);
        Assert.assertNotNull(remoteHost);
    }

    @Test
    public void addRemoteHost() throws AppException {
        setupParameters();
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = APIManagerAdapter.getInstance().remoteHostsAdapter;
        RemoteHost remoteHost = apiManagerRemoteHostsAdapter.getRemoteHost("api.axway.com", 443);
        Assert.assertNotNull(remoteHost);
        apiManagerRemoteHostsAdapter.createOrUpdateRemoteHost(remoteHost, null);
    }

    @Test
    public void updateRemoteHost() throws AppException {
        setupParameters();
        APIManagerRemoteHostsAdapter apiManagerRemoteHostsAdapter = APIManagerAdapter.getInstance().remoteHostsAdapter;
        RemoteHost remoteHost = apiManagerRemoteHostsAdapter.getRemoteHost("api.axway.com", 443);
        Assert.assertNotNull(remoteHost);
        RemoteHost updatedRemoteHost = new RemoteHost();
        updatedRemoteHost.setName("api.demoaxway.com");
        updatedRemoteHost.setAlias("api.demoaxway.com");
        updatedRemoteHost.setId(remoteHost.getId());
        apiManagerRemoteHostsAdapter.createOrUpdateRemoteHost(remoteHost, updatedRemoteHost);
    }

}
