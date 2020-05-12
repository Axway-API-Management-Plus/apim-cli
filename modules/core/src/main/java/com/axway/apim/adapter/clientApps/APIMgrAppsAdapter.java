package com.axway.apim.adapter.clientApps;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIMgrAppsAdapter extends ClientAppAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIMgrAppsAdapter.class);

	List<ClientApplication> apps = null;
	
	CommandParameters params;
	
	ObjectMapper mapper = APIManagerAdapter.mapper;

	public APIMgrAppsAdapter() {}
	
	@Override
	public boolean readConfig(Object config) throws AppException {
		if(config instanceof APIManagerAdapter && CommandParameters.getInstance()!=null) return true;
		return false;
	}

	/**
	 * Returns a list of applications.
	 * @throws AppException if applications cannot be retrieved
	 */
	void readApplications(ClientAppFilter appFilter) throws AppException {
		if(this.apps !=null) return;
		try {
			URI uri = getRequestUri(appFilter);
			LOG.info("Sending request to find existing applications: " + uri);
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			this.apps = mapper.readValue(response, new TypeReference<List<ClientApplication>>(){});
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		if(appFilter.includeQuota) {
			APIManagerAdapter.getInstance().addExistingClientAppQuotas(apps);
		}
	}
	
	URI getRequestUri(ClientAppFilter appFilter) throws URISyntaxException {
		CommandParameters cmd = CommandParameters.getInstance();
		URI uri;
		List<NameValuePair> usedFilters = new ArrayList<>();
		String searchForAppId = "";
		if(appFilter!=null) {
			if(appFilter != null && appFilter.getFilters().size()!=0) { usedFilters.addAll(appFilter.getFilters()); }
			
			if(appFilter.getApplicationId()!=null) {
				searchForAppId = "/"+appFilter.getApplicationId();
			}
		}
		uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications"+searchForAppId)
				.addParameters(usedFilters)
				.build();
		return uri;
	}

	public ClientApplication getApplication(ClientApplication application) throws AppException {
		readApplications(new ClientAppFilter.Builder().hasName(application.getName()).build());
		return uniqueApplication(application.getName());
	}
	
	public ClientApplication createApplication(ClientApplication app) throws AppException {
		getApplication(app);
		try {
			CommandParameters cmd = CommandParameters.getInstance();
			String orgId = "";
			if(APIManagerAdapter.hasAdminAccount()) {
				orgId = ", \"organizationId\": " + app.getOrganizationId();
			}
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/").build();
			String json = "{\"name\":\"" + app.getName() + "\", \"apis\":[] "+orgId+"}";
			HttpEntity entity = new StringEntity(json);
			
			POSTRequest postRequest = new POSTRequest(entity, uri, null);
			postRequest.setContentType("application/json");
			HttpResponse httpResponse = postRequest.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if( statusCode != 201){
				LOG.error("Error creating application' Response-Code: "+statusCode+"");
				throw new AppException("Error creating application' Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			ClientApplication createApp = mapper.readValue(httpResponse.getEntity().getContent(), ClientApplication.class);
			return createApp;

		} catch (Exception e) {
			throw new AppException("Can't create API-Proxy.", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
	}
	
	private ClientApplication uniqueApplication(String applicationName) throws AppException {
		if(this.apps.size()>1) {
			throw new AppException("No unique application found", ErrorCode.UNKNOWN_API);
		}
		if(this.apps.size()==0) return null;
		return this.apps.get(0);
	}
	
	@Override
	public List<ClientApplication> getApplications() throws AppException {
		return this.getApplications(new ClientAppFilter.Builder().build());
	}

	public List<ClientApplication> getApplications(String requestedApplicationId) throws AppException {
		readApplications(new ClientAppFilter.Builder().hasId(requestedApplicationId).build());
		return apps;
	}
	
	@Override
	public List<ClientApplication> getApplications(ClientAppFilter filter) throws AppException {
		readApplications(filter);
		return apps;
	}
}
