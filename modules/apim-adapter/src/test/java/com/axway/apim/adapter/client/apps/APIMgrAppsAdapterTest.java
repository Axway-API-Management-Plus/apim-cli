package com.axway.apim.adapter.client.apps;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.apps.*;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
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
import java.util.Optional;
import java.util.UUID;

public class APIMgrAppsAdapterTest extends WiremockWrapper {

	private final String testHostname = "localhost";
	private final int testPort = 8075;

    private APIMgrAppsAdapter clientAppAdapter;

	@BeforeClass
	public void init() {
		try {
			initWiremock();
			CoreParameters coreParameters = new CoreParameters();
			coreParameters.setHostname("localhost");
			coreParameters.setUsername("apiadmin");
			coreParameters.setPassword(Utils.getEncryptedPassword());
            clientAppAdapter = APIManagerAdapter.getInstance().getAppAdapter();
		} catch (AppException e) {
			throw new RuntimeException(e);
		}
	}

	@AfterClass
	public void close() {
		super.close();
	}


	@Test
	public void queryForUniqueApplication() throws IOException, URISyntaxException {
		ClientAppFilter filter = new ClientAppFilter.Builder().hasName("Application 123").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);
		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=name&op=eq&value=Application+123");
	}

	@Test
	public void withoutAnyFilter() throws IOException, URISyntaxException {
		URI requestUri = clientAppAdapter.getApplicationsUri(null);
		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications");
	}

	@Test
	public void usingApplicationId() throws IOException, URISyntaxException {
		ClientAppFilter filter = new ClientAppFilter.Builder().hasId("5893475934875934").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);

		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications/5893475934875934");
	}

	@Test
	public void filterForAppName() throws IOException, URISyntaxException {
		ClientAppFilter filter = new ClientAppFilter.Builder().hasName("MyTestApp").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);

		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=name&op=eq&value=MyTestApp");
	}

	@Test
	public void filterForOrgId() throws IOException, URISyntaxException {
		ClientAppFilter filter = new ClientAppFilter.Builder().hasOrganizationId("42342342342343223").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);

		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=orgid&op=eq&value=42342342342343223");
	}

	@Test
	public void filterStatePending() throws IOException, URISyntaxException {
		ClientAppFilter filter = new ClientAppFilter.Builder().hasState("pending").build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);

		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications?field=state&op=eq&value=pending");
	}

	@Test
	public void filterStatePendingAndAppName() throws IOException, URISyntaxException {

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

		ClientAppFilter filter = new ClientAppFilter.Builder().hasState(null).hasName(null).hasOrganizationId(null).build();
		URI requestUri = clientAppAdapter.getApplicationsUri(filter);

		Assert.assertNotNull(requestUri, "RequestUri is null");
		Assert.assertEquals(requestUri.toString(), "https://"+testHostname+":"+testPort+"/api/portal/v1.4/applications");
	}


	@Test
	public void getApplications() throws AppException {
		List<ClientApplication> clientApplications = clientAppAdapter.getAllApplications(false);
		Assert.assertNotNull(clientApplications);
	}

	@Test
	public void getAppsSubscribedWithAPI() throws AppException {
		List<ClientApplication> clientApplications = clientAppAdapter.getAppsSubscribedWithAPI("e4ded8c8-0a40-4b50-bc13-552fb7209150");
		Assert.assertNotNull(clientApplications);
	}

	@Test
	public void getApplication() throws AppException {
		ClientApplication clientApplication = clientAppAdapter.getApplication(new ClientAppFilter.Builder().hasName("Test App 2008").build());
		Assert.assertEquals(clientApplication.getName(), "Test App 2008");
	}


	@Test
	public void deleteApplication() throws AppException {
		ClientApplication clientApplication = clientAppAdapter.getApplication(new ClientAppFilter.Builder().hasName("Test App 2008").build());
		try {
            clientAppAdapter.deleteApplication(clientApplication);

		} catch (AppException appException) {
			Assert.fail("unable to delete application", appException);
		}
	}

	@Test
	public void updateApplication() throws AppException {
		ClientApplication clientApplication = clientAppAdapter.getApplication(new ClientAppFilter.Builder().hasName("Test App 2008").build());
		ClientApplication updatedApplication = new ClientApplication();
		updatedApplication.setName("test");
		updatedApplication.setId(clientApplication.getId());
		try {
            clientAppAdapter.createOrUpdateApplication(updatedApplication, clientApplication);

		} catch (AppException appException) {
			Assert.fail("unable to update application", appException);
		}
	}

	@Test
	public void createApplication() {
		ClientApplication clientApplication = new ClientApplication();
		clientApplication.setName("test");
		try {
            clientAppAdapter.createApplication(clientApplication);

		} catch (AppException appException) {
			appException.printStackTrace();
			//Assert.fail("unable to create application", appException);
		}
	}

    @Test
    public void saveQuota(){
        ClientApplication clientApplicationNew = new ClientApplication();
        clientApplicationNew.setName("test");

        ClientApplication clientApplicationExisting = new ClientApplication();
        clientApplicationExisting.setName("test");
        clientApplicationNew.setId(UUID.randomUUID().toString());
        try {
            clientAppAdapter.saveQuota(clientApplicationNew, clientApplicationExisting);
        } catch (AppException e) {
            Assert.fail("unable to update application", e);
        }
    }

    @Test
    public void createUpsertUriPost() throws AppException, URISyntaxException {
        String json ="";
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        CoreParameters cmd = CoreParameters.getInstance();
        URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/1d2aeeca-2716-449e-a7a0-5d7213dbcbaf" + "/quota").build();
        RestAPICall request = clientAppAdapter.createUpsertUri(entity, uri, null);
        Assert.assertNotNull(request);
        Assert.assertTrue(request instanceof POSTRequest);
    }

    @Test
    public void createUpsertUriPutWithExistingQuota() throws AppException, URISyntaxException {
        String json ="";
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        CoreParameters cmd = CoreParameters.getInstance();
        URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/1d2aeeca-2716-449e-a7a0-5d7213dbcbaf" + "/quota").build();
        ClientApplication actualApp = new ClientApplication();
        actualApp.setName("testapp");
        APIQuota apiQuota = new APIQuota();
        apiQuota.setName("quota");
        actualApp.setAppQuota(apiQuota);
        RestAPICall request = clientAppAdapter.createUpsertUri(entity, uri, actualApp);
        Assert.assertNotNull(request);
        Assert.assertTrue(request instanceof PUTRequest);
    }

    @Test
    public void createUpsertUriPostWithNoQuota() throws AppException, URISyntaxException {
        String json ="";
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        CoreParameters cmd = CoreParameters.getInstance();
        URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/1d2aeeca-2716-449e-a7a0-5d7213dbcbaf" + "/quota").build();
        ClientApplication actualApp = new ClientApplication();
        actualApp.setName("testapp");
        RestAPICall request = clientAppAdapter.createUpsertUri(entity, uri, actualApp);
        Assert.assertNotNull(request);
        Assert.assertTrue(request instanceof POSTRequest);
    }

    @Test
    public void deleteApplicationQuota() throws AppException, URISyntaxException {
        CoreParameters cmd = CoreParameters.getInstance();
        URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/1d2aeeca-2716-449e-a7a0-5d7213dbcbaf" + "/quota").build();
        clientAppAdapter.deleteApplicationQuota(uri);
    }

    @Test
    public void searchForExistingCredential() {
        String credentialId = "1234";
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setName("test");
        clientApplication.setId("1d2aeeca-2716-449e-a7a0-5d7213dbcbaf");
        ClientAppCredential clientAppCredential = new OAuth();
        clientAppCredential.setId(credentialId);
        List<ClientAppCredential> credentials = new ArrayList<>();
        credentials.add(clientAppCredential);
        clientApplication.setCredentials(credentials);
        Optional<ClientAppCredential> optionalClientAppCredential = clientAppAdapter.searchForExistingCredential(clientApplication, credentialId);
        Assert.assertTrue(optionalClientAppCredential.isPresent());
        Assert.assertEquals(clientAppCredential, optionalClientAppCredential.get());
    }

    @Test
    public void searchForExistingCredentialApikey() {
        String credentialId = "1234";
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setName("test");
        clientApplication.setId("1d2aeeca-2716-449e-a7a0-5d7213dbcbaf");
        ClientAppCredential clientAppCredential = new APIKey();
        clientAppCredential.setId(credentialId);
        List<ClientAppCredential> credentials = new ArrayList<>();
        credentials.add(clientAppCredential);
        clientApplication.setCredentials(credentials);
        Optional<ClientAppCredential> optionalClientAppCredential = clientAppAdapter.searchForExistingCredential(clientApplication, credentialId);
        Assert.assertTrue(optionalClientAppCredential.isPresent());
        Assert.assertEquals(clientAppCredential, optionalClientAppCredential.get());
    }


    @Test
    public void searchForExistingCredentialExtClients() {
        String credentialId = "1234";
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setName("test");
        clientApplication.setId("1d2aeeca-2716-449e-a7a0-5d7213dbcbaf");
        ClientAppCredential clientAppCredential = new ExtClients();
        clientAppCredential.setId(credentialId);
        List<ClientAppCredential> credentials = new ArrayList<>();
        credentials.add(clientAppCredential);
        clientApplication.setCredentials(credentials);
        Optional<ClientAppCredential> optionalClientAppCredential = clientAppAdapter.searchForExistingCredential(clientApplication, credentialId);
        Assert.assertTrue(optionalClientAppCredential.isPresent());
        Assert.assertEquals(clientAppCredential, optionalClientAppCredential.get());
    }

    @Test
    public void searchForExistingCredentialNone() {
        String credentialId = "1234";
        ClientApplication clientApplication = new ClientApplication();
        clientApplication.setName("test");
        clientApplication.setId("1d2aeeca-2716-449e-a7a0-5d7213dbcbaf");

        Optional<ClientAppCredential> optionalClientAppCredential = clientAppAdapter.searchForExistingCredential(clientApplication, credentialId);
        Assert.assertFalse(optionalClientAppCredential.isPresent());
    }
}
