package com.axway.apim.adapter.clientApps;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIMgrAppsAdapter extends ClientAppAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIMgrAppsAdapter.class);

	List<NameValuePair> filters = new ArrayList<NameValuePair>();

	List<ClientApplication> apps = null;
	
	CommandParameters params;
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	boolean includeQuota = false;

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
	void readApplications() throws AppException {
		if(this.apps !=null) return;
		try {
			URI uri = getRequestUri();
			LOG.info("Sending request to find existing applications: " + uri);
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			this.apps = mapper.readValue(response, new TypeReference<List<ClientApplication>>(){});
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		if(this.includeQuota) {
			APIManagerAdapter.getInstance().addExistingClientAppQuotas(apps);
		}
	}
	
	URI getRequestUri() throws URISyntaxException {
		CommandParameters cmd = CommandParameters.getInstance();
		URI uri;
		List<NameValuePair> usedFilters = new ArrayList<>();
		if(filters != null && filters.size()!=0) { usedFilters.addAll(filters); }
		String searchForAppId = "";
		if(this.applicationId!=null) {
			searchForAppId = "/"+this.applicationId;
		}
		uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications"+searchForAppId)
				.addParameters(usedFilters)
				.build();
		return uri;
	}
	
	@Override
	public void setApplicationName(String applicationName) {
		if(applicationName==null) return;
		filters.add(new BasicNameValuePair("field", "name"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", applicationName));
	}

	@Override
	public void setOrganization(String organization) {
		if(organization==null) return;
		filters.add(new BasicNameValuePair("field", "orgid"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", organization));
	}


	@Override
	public void setState(String state) {
		if(state==null) return;
		filters.add(new BasicNameValuePair("field", "state"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", state));
	}
	
	public void useFilter(List<NameValuePair> filter) {
		this.filters.addAll(filter);
	}

	public ClientApplication getApplication(String requestedApplicationId) throws AppException {
		List<ClientApplication> foundApps = getApplications(requestedApplicationId);
		return uniqueApplication(foundApps);
	}
	
	private ClientApplication uniqueApplication(List<ClientApplication> foundApps) throws AppException {
		if(foundApps.size()>1) {
			throw new AppException("No unique API found", ErrorCode.UNKNOWN_API);
		}
		if(foundApps.size()==0) return null;
		return foundApps.get(0);
	}
	
	public List<ClientApplication> getApplications() throws AppException {
		return this.getApplications(null);
	}

	public List<ClientApplication> getApplications(String requestedApplicationId) throws AppException {
		readApplications();
		return apps;
	}
}
