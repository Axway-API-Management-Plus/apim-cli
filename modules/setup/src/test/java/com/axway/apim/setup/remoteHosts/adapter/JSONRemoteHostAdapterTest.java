package com.axway.apim.setup.remoteHosts.adapter;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.setup.remotehosts.adapter.JSONRemoteHostsAdapter;

public class JSONRemoteHostAdapterTest extends APIManagerMockBase {
	
	private static final String PACKAGE = "com/axway/apim/setup/remotehost/adapter/";
	
	@BeforeClass
	public void setupTestIndicator() throws AppException, IOException {
		setupMockData();
	}
	
	@Test
	public void testNonStagedMultiRemoteHosts() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "two-remotehosts-config.json").getFile();
		importParams.setConfig(configFileName);
		JSONRemoteHostsAdapter adapter = new JSONRemoteHostsAdapter(importParams);
		List<RemoteHost> remoteHosts = adapter.getRemoteHosts();
		
		Assert.assertEquals(remoteHosts.size(), 2, "Two Remote Hosts are expected");
		
		RemoteHost remoteHost1 = remoteHosts.get(0);
		RemoteHost remoteHost2 = remoteHosts.get(1);
		
		Assert.assertEquals(remoteHost1.getName(), "localhostrathna");
		Assert.assertEquals(remoteHost1.getPort(), new Integer(80));
		Assert.assertEquals(remoteHost1.getOrganization().getName(), "API Development");
		Assert.assertEquals(remoteHost1.getCreatedBy().getName(), "API Administrator");
		Assert.assertEquals(remoteHost1.getCreatedBy().getId(), "f60e3e05-cdf3-4b70-affc-4cb61a10f4bb");
		
		
		Assert.assertEquals(remoteHost2.getName(), "samplehost.com");
		Assert.assertEquals(remoteHost2.getPort(), new Integer(80));
		Assert.assertEquals(remoteHost2.getOrganization().getName(), "FHIR");
		Assert.assertEquals(remoteHost2.getCreatedBy().getName(), "Fred Smith");
		Assert.assertEquals(remoteHost2.getCreatedBy().getLoginName(), "fred");
		Assert.assertEquals(remoteHost2.getCreatedBy().getId(), "c888af4e-0728-4e82-880c-7cf490138220");
		
	}
	
	@Test
	public void testValidStaged() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "one-remotehost-config.json").getFile();
		importParams.setConfig(configFileName);
		importParams.setStage("test-stage");
		JSONRemoteHostsAdapter adapter = new JSONRemoteHostsAdapter(importParams);
		List<RemoteHost> remoteHosts = adapter.getRemoteHosts();
		
		Assert.assertEquals(remoteHosts.size(), 1, "One Remote Hosts is expected");
		RemoteHost remoteHost = remoteHosts.get(0);
		Assert.assertEquals(remoteHost.getName(), "staged-remotehost.com");
		Assert.assertEquals(remoteHost.getPort(), new Integer(9999));
	}
	
	@Test
	public void testInvalidStage() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "one-remotehost-config.json").getFile();
		importParams.setConfig(configFileName);
		importParams.setStage("invalid-stage");
		JSONRemoteHostsAdapter adapter = new JSONRemoteHostsAdapter(importParams);
		List<RemoteHost> remoteHosts = adapter.getRemoteHosts();
		
		Assert.assertEquals(remoteHosts.size(), 1, "One Remote Hosts is expected");
		RemoteHost remoteHost = remoteHosts.get(0);
		Assert.assertEquals(remoteHost.getName(), "my-remotehost.com");
		Assert.assertEquals(remoteHost.getPort(), new Integer(8280));
	}
}
