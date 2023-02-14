package com.axway.apim.setup.adapter;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

public class JSONRemoteHostAdapterTest  extends WiremockWrapper {

	@BeforeClass
	public void init() {
		initWiremock();
	}

	@AfterClass
	public void stop() {
		close();
	}
	
	private static final String PACKAGE = "com/axway/apim/setup/adapter/";
	
//	@BeforeClass
//	public void setupTestIndicator() throws AppException, IOException {
//		setupMockData();
//	}
	
	@Test
	public void testNonStagedMultiRemoteHosts() throws AppException {
		StandardImportParams importParams = new StandardImportParams();

		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "two-remotehosts-config.json").getFile();
		importParams.setConfig(configFileName);
		importParams.setUsername("orgadmin");
		importParams.setPassword(Utils.getEncryptedPassword());
		importParams.setHostname("localhost");

		JSONAPIManagerConfigAdapter adapter = new JSONAPIManagerConfigAdapter(importParams);
		Map<String, RemoteHost> remoteHosts = adapter.getManagerConfig().getRemoteHosts();
		
		Assert.assertEquals(remoteHosts.size(), 2, "Two Remote Hosts are expected");
		
		RemoteHost remoteHost1 = remoteHosts.get("My-First-Remote-Host");
		RemoteHost remoteHost2 = remoteHosts.get("My-Second-Remote-Host");
		
		Assert.assertEquals(remoteHost1.getName(), "localhostrathna");
		Assert.assertEquals(remoteHost1.getPort(), new Integer(80));
		Assert.assertEquals(remoteHost1.getOrganization().getName(), "orga");
		Assert.assertEquals(remoteHost1.getCreatedBy().getName(), "usera");
		Assert.assertEquals(remoteHost1.getCreatedBy().getId(), "2f126140-db10-4ccb-be9d-e430d9fe9c45");
		
		Assert.assertEquals(remoteHost2.getName(), "samplehost.com");
		Assert.assertEquals(remoteHost2.getPort(), new Integer(80));
		Assert.assertEquals(remoteHost2.getOrganization().getName(), "orga");
		Assert.assertEquals(remoteHost2.getCreatedBy().getName(), "usera");
		Assert.assertEquals(remoteHost2.getCreatedBy().getLoginName(), "usera");
		Assert.assertEquals(remoteHost2.getCreatedBy().getId(), "2f126140-db10-4ccb-be9d-e430d9fe9c45");
	}
	
	@Test
	public void testStagedMultiRemoteHosts() throws AppException {
		StandardImportParams importParams = new StandardImportParams();
		String configFileName = this.getClass().getClassLoader().getResource(PACKAGE + "two-remotehosts-config.json").getFile();
		importParams.setConfig(configFileName);
		importParams.setStage("test-stage");
		importParams.setUsername("orgadmin");
		importParams.setPassword(Utils.getEncryptedPassword());
		importParams.setHostname("localhost");
		JSONAPIManagerConfigAdapter adapter = new JSONAPIManagerConfigAdapter(importParams);
		Map<String, RemoteHost> remoteHosts = adapter.getManagerConfig().getRemoteHosts();
		
		Assert.assertEquals(remoteHosts.size(), 2, "Two Remote Hosts are expected");
		
		RemoteHost remoteHost1 = remoteHosts.get("My-First-Remote-Host");
		RemoteHost remoteHost2 = remoteHosts.get("My-Second-Remote-Host");
		
		Assert.assertEquals(remoteHost1.getName(), "staged-remotehost.com");
		Assert.assertEquals(remoteHost1.getPort(), new Integer(9999));
		Assert.assertEquals(remoteHost1.getOrganization().getName(), "orga");
		Assert.assertEquals(remoteHost1.getCreatedBy().getName(), "usera");
		Assert.assertEquals(remoteHost1.getCreatedBy().getId(), "2f126140-db10-4ccb-be9d-e430d9fe9c45");
		
		// Second remote host is NOT staged
		Assert.assertEquals(remoteHost2.getName(), "samplehost.com");
		Assert.assertEquals(remoteHost2.getPort(), new Integer(80));
	}
}
