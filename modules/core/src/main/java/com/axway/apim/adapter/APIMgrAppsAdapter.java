package com.axway.apim.adapter;

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

import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIMgrAppsAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIMgrAppsAdapter.class);

	List<NameValuePair> filters;

	List<ClientApplication> apps;
	
	CommandParameters params = CommandParameters.getInstance();
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	String requestedApplicationId;
	
	boolean includeQuota = false;

	private APIMgrAppsAdapter() {}

	/**
	 * Returns a list of applications.
	 * @throws AppException if applications cannot be retrieved
	 */
	void readApplications() throws AppException {
		try {
			URI uri = getRequestUri();
			LOG.info("Sending request to find existing applications: " + uri);
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();

			this.apps = mapper.readValue(response, new TypeReference<List<ClientApplication>>(){});
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	URI getRequestUri() throws URISyntaxException {
		CommandParameters cmd = CommandParameters.getInstance();
		URI uri;
		List<NameValuePair> usedFilters = new ArrayList<>();
		if(filters != null) { usedFilters.addAll(filters); }
		String searchForAppId = "";
		if(requestedApplicationId!=null) {
			searchForAppId = "/"+requestedApplicationId;
		}
		uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications"+searchForAppId)
				.addParameters(usedFilters)
				.build();
		return uri;
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
		if(this.apps==null) readApplications();
		if(this.includeQuota) {
			APIManagerAdapter.getInstance().addExistingClientAppQuotas(apps);
		}
		return apps;
	}

	public static class Builder {
		
		boolean includeQuota;
		
		String applicationId;

		List<NameValuePair> filters = new ArrayList<NameValuePair>();
		
		public Builder() throws AppException {
			this(null);
		}
		
		public Builder(String applicationId) throws AppException {
			super();
			this.applicationId = applicationId;
		}
		
		/**
		 * Method called to build the APIMgrAppsAdapter
		 * @return the initialized adapter
		 */
		public APIMgrAppsAdapter build() {
			APIMgrAppsAdapter applicationAdapter = new APIMgrAppsAdapter();
			applicationAdapter.filters = this.filters;
			applicationAdapter.requestedApplicationId = this.applicationId;
			applicationAdapter.includeQuota = this.includeQuota;
			return applicationAdapter;
		}

		public Builder hasName(String requestedAppName) {
			if(requestedAppName==null) return this;
			filters.add(new BasicNameValuePair("field", "name"));
			filters.add(new BasicNameValuePair("op", "eq"));
			filters.add(new BasicNameValuePair("value", requestedAppName));
			return this;
		}

		public Builder hasOrgId(String requestedOrgId) {
			if(requestedOrgId==null) return this;
			filters.add(new BasicNameValuePair("field", "orgid"));
			filters.add(new BasicNameValuePair("op", "eq"));
			filters.add(new BasicNameValuePair("value", requestedOrgId));
			return this;
		}

		public Builder hasState(String requestedAppState) {
			if(requestedAppState==null) return this;
			filters.add(new BasicNameValuePair("field", "state"));
			filters.add(new BasicNameValuePair("op", "eq"));
			filters.add(new BasicNameValuePair("value", requestedAppState));
			return this;
		}

		public Builder useFilter(List<NameValuePair> filter) {
			this.filters.addAll(filter);
			return this;
		}
		
		public Builder includeQuotas(boolean includeQuota) {
			this.includeQuota = includeQuota;
			return this;
		}
	}
}
