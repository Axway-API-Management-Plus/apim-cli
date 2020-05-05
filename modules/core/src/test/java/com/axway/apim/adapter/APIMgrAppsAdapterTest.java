package com.axway.apim.adapter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;

public class APIMgrAppsAdapterTest {
	
	private String testHostname = "api-manager-host";
	private String testPort = "8088";
	
	@BeforeClass
	private void initTestIndicator() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("host", testHostname);
		params.put("port", testPort);
		new CommandParameters(params);
	}
	
	@Test
	public void withoutAnyFilter() throws AppException, IOException, URISyntaxException {
		APIMgrAppsAdapter adapter = new APIMgrAppsAdapter.Builder().build();
		Assert.assertNotNull(adapter, "APIMgrAppsAdapter is null");
		Assert.assertNotNull(adapter.getRequestUri(), "RequestUri is null");
		Assert.assertEquals(adapter.getRequestUri().toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.3/applications");
	}
	
	@Test
	public void usingApplicationId() throws AppException, IOException, URISyntaxException {
		APIMgrAppsAdapter adapter = new APIMgrAppsAdapter.Builder("5893475934875934").build();
		Assert.assertNotNull(adapter, "APIMgrAppsAdapter is null");
		Assert.assertNotNull(adapter.getRequestUri(), "RequestUri is null");
		Assert.assertEquals(adapter.getRequestUri().toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.3/applications/5893475934875934");
	}
	
	@Test
	public void filterForAppName() throws AppException, IOException, URISyntaxException {
		APIMgrAppsAdapter adapter = new APIMgrAppsAdapter.Builder()
				.hasName("MyTestApp")
				.build();
		Assert.assertNotNull(adapter, "APIMgrAppsAdapter is null");
		Assert.assertNotNull(adapter.getRequestUri(), "RequestUri is null");
		Assert.assertEquals(adapter.getRequestUri().toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.3/applications?field=name&op=eq&value=MyTestApp");
	}
	
	@Test
	public void filterForOrgId() throws AppException, IOException, URISyntaxException {
		APIMgrAppsAdapter adapter = new APIMgrAppsAdapter.Builder()
				.hasOrgId("42342342342343223")
				.build();
		Assert.assertNotNull(adapter, "APIMgrAppsAdapter is null");
		Assert.assertNotNull(adapter.getRequestUri(), "RequestUri is null");
		Assert.assertEquals(adapter.getRequestUri().toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.3/applications?field=orgid&op=eq&value=42342342342343223");
	}
	
	@Test
	public void filterStatePending() throws AppException, IOException, URISyntaxException {
		APIMgrAppsAdapter adapter = new APIMgrAppsAdapter.Builder()
				.hasState("pending")
				.build();
		Assert.assertNotNull(adapter, "APIMgrAppsAdapter is null");
		Assert.assertNotNull(adapter.getRequestUri(), "RequestUri is null");
		Assert.assertEquals(adapter.getRequestUri().toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.3/applications?field=state&op=eq&value=pending");
	}
	
	@Test
	public void filterStatePendingAndAppName() throws AppException, IOException, URISyntaxException {
		APIMgrAppsAdapter adapter = new APIMgrAppsAdapter.Builder()
				.hasState("pending")
				.hasName("AnotherPendingApp")
				.build();
		Assert.assertNotNull(adapter, "APIMgrAppsAdapter is null");
		Assert.assertNotNull(adapter.getRequestUri(), "RequestUri is null");
		Assert.assertEquals(adapter.getRequestUri().toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.3/applications?field=state&op=eq&value=pending&field=name&op=eq&value=AnotherPendingApp");
	}
	
	@Test
	public void filterCustomFieldAndName() throws AppException, IOException, URISyntaxException {
		List<NameValuePair> customFilters = new ArrayList<NameValuePair>();
		customFilters.add(new BasicNameValuePair("field", "email"));
		customFilters.add(new BasicNameValuePair("op", "eq"));
		customFilters.add(new BasicNameValuePair("value", "this@there.com"));
		APIMgrAppsAdapter adapter = new APIMgrAppsAdapter.Builder()
				.hasName("AnotherPendingApp")
				.useFilter(customFilters)
				.build();
		Assert.assertNotNull(adapter, "APIMgrAppsAdapter is null");
		Assert.assertNotNull(adapter.getRequestUri(), "RequestUri is null");
		Assert.assertEquals(adapter.getRequestUri().toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.3/applications?field=name&op=eq&value=AnotherPendingApp&field=email&op=eq&value=this%40there.com");
	}
	
	@Test
	public void filterNullValues() throws AppException, IOException, URISyntaxException {
		APIMgrAppsAdapter adapter = new APIMgrAppsAdapter.Builder()
				.hasState(null)
				.hasName(null)
				.hasOrgId(null)
				.build();
		Assert.assertNotNull(adapter, "APIMgrAppsAdapter is null");
		Assert.assertNotNull(adapter.getRequestUri(), "RequestUri is null");
		Assert.assertEquals(adapter.getRequestUri().toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.3/applications");
	}
	
}
