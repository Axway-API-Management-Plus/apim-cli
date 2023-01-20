package com.axway.apim.adapter.clientApps;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import com.axway.apim.lib.utils.Utils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class APIMgrAppsAdapterTest extends WiremockWrapper {

	private final String testHostname = "localhost";
	private final int testPort = 8075;

	@BeforeClass
	public void initWiremock() {
		TestIndicator.getInstance().setTestRunning(true);
		super.initWiremock();
		CoreParameters coreParameters = new CoreParameters();
		coreParameters.setHostname(testHostname);
		coreParameters.setPort(testPort);
	}

	public void setupParams() throws AppException {
		TestIndicator.getInstance().setTestRunning(true);
		APIManagerAdapter.deleteInstance();
		CoreParameters coreParameters = new CoreParameters();
		coreParameters.setHostname(testHostname);
		coreParameters.setPort(testPort);
		coreParameters.setUsername("test");
		coreParameters.setPassword(Utils.getEncryptedPassword());
	}

	@AfterClass
	public void close() {
		super.close();
	}


	@Test
	public void queryForUniqueApplication() throws IOException, URISyntaxException {
		APIMgrAppsAdapter clientAppAdapter = new APIMgrAppsAdapter();
		ClientAppFilter filter = new ClientAppFilter.Builder().hasName("Application 123").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);
		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=name&op=eq&value=Application+123");
	}
	
	@Test
	public void withoutAnyFilter() throws IOException, URISyntaxException {
		APIMgrAppsAdapter clientAppAdapter = new APIMgrAppsAdapter();
		URI requestUri = clientAppAdapter.getApplicationsUri(null);
		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications");
	}
	
	@Test
	public void usingApplicationId() throws IOException, URISyntaxException {
		APIMgrAppsAdapter clientAppAdapter = new APIMgrAppsAdapter();
		ClientAppFilter filter = new ClientAppFilter.Builder().hasId("5893475934875934").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);
		
		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications/5893475934875934");
	}
	
	@Test
	public void filterForAppName() throws IOException, URISyntaxException {
		APIMgrAppsAdapter clientAppAdapter = new APIMgrAppsAdapter();
		ClientAppFilter filter = new ClientAppFilter.Builder().hasName("MyTestApp").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);
		
		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=name&op=eq&value=MyTestApp");
	}
	
	@Test
	public void filterForOrgId() throws IOException, URISyntaxException {
		APIMgrAppsAdapter clientAppAdapter = new APIMgrAppsAdapter();
		ClientAppFilter filter = new ClientAppFilter.Builder().hasOrganizationId("42342342342343223").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);
		
		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=orgid&op=eq&value=42342342342343223");
	}
	
	@Test
	public void filterStatePending() throws IOException, URISyntaxException {
		APIMgrAppsAdapter clientAppAdapter = new APIMgrAppsAdapter();
		ClientAppFilter filter = new ClientAppFilter.Builder().hasState("pending").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);
		
		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=state&op=eq&value=pending");
	}
	
	@Test
	public void filterStatePendingAndAppName() throws IOException, URISyntaxException {
		APIMgrAppsAdapter clientAppAdapter = new APIMgrAppsAdapter();
		
		ClientAppFilter filter = new ClientAppFilter.Builder().hasState("pending").hasName("AnotherPendingApp").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);

		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=name&op=eq&value=AnotherPendingApp&field=state&op=eq&value=pending");
	}
	
	@Test
	public void filterCustomFieldAndName() throws IOException, URISyntaxException {
		List<NameValuePair> customFilters = new ArrayList<>();
		customFilters.add(new BasicNameValuePair("field", "email"));
		customFilters.add(new BasicNameValuePair("op", "eq"));
		customFilters.add(new BasicNameValuePair("value", "this@there.com"));
		
		APIMgrAppsAdapter clientAppAdapter = new APIMgrAppsAdapter();
		
		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasName("AnotherPendingApp")
				.build();
		filter.useFilter(customFilters);
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);
		
		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=name&op=eq&value=AnotherPendingApp&field=email&op=eq&value=this%40there.com");
	}
	
	@Test
	public void filterNullValues() throws IOException, URISyntaxException {
		APIMgrAppsAdapter clientAppAdapter = new APIMgrAppsAdapter();
		
		ClientAppFilter filter = new ClientAppFilter.Builder().hasState(null).hasName(null).hasOrganizationId(null).build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);

		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications");
	}


	@Test
	public void getApplications() throws AppException {
		setupParams();
		APIMgrAppsAdapter appAdapter = APIManagerAdapter.getInstance().appAdapter;
		List<ClientApplication> clientApplications = appAdapter.getAllApplications(false);
		Assert.assertNotNull(clientApplications);
	}

	@Test
	public void getAppsSubscribedWithAPI() throws AppException {
		setupParams();
		APIMgrAppsAdapter appAdapter = APIManagerAdapter.getInstance().appAdapter;
		List<ClientApplication> clientApplications = appAdapter.getAppsSubscribedWithAPI("e4ded8c8-0a40-4b50-bc13-552fb7209150");
		Assert.assertNotNull(clientApplications);
	}

	@Test
	public void getApplication() throws AppException {
		setupParams();
		APIMgrAppsAdapter appAdapter = APIManagerAdapter.getInstance().appAdapter;
		ClientApplication clientApplication = appAdapter.getApplication(new ClientAppFilter.Builder().hasName("Test App 2008").build());
		Assert.assertEquals(clientApplication.getName(), "Test App 2008");
	}


	@Test
	public void deleteApplication() throws AppException {
		setupParams();
		APIMgrAppsAdapter appAdapter = APIManagerAdapter.getInstance().appAdapter;
		ClientApplication clientApplication = appAdapter.getApplication(new ClientAppFilter.Builder().hasName("Test App 2008").build());
		try {
			appAdapter.deleteApplication(clientApplication);

		} catch (AppException appException) {
			Assert.fail("unable to delete application", appException);
		}
	}

	@Test
	public void updateApplication() throws AppException {
		setupParams();
		APIMgrAppsAdapter appAdapter = APIManagerAdapter.getInstance().appAdapter;
		ClientApplication clientApplication = appAdapter.getApplication(new ClientAppFilter.Builder().hasName("Test App 2008").build());

		ClientApplication updatedApplication = new ClientApplication();
		updatedApplication.setName("test");
		updatedApplication.setId(clientApplication.getId());
		try {
			appAdapter.createOrUpdateApplication(updatedApplication, clientApplication);

		} catch (AppException appException) {
			Assert.fail("unable to update application", appException);
		}
	}

	@Test
	public void createApplication() throws AppException {
		setupParams();
		APIMgrAppsAdapter appAdapter = APIManagerAdapter.getInstance().appAdapter;
		ClientApplication clientApplication = new ClientApplication();
		clientApplication.setName("test");
		try {
			appAdapter.createApplication(clientApplication);

		} catch (AppException appException) {
			appException.printStackTrace();
			Assert.fail("unable to create application", appException);
		}
	}
}
