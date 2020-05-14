package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerOrganizationAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerOrganizationAdapter.class);
	
	public final static String SYSTEM_API_QUOTA 				= "00000000-0000-0000-0000-000000000000";
	public final static String APPLICATION_DEFAULT_QUOTA 		= "00000000-0000-0000-0000-000000000001";
	
	ObjectMapper mapper = APIManagerAdapter.mapper;

	public APIManagerOrganizationAdapter() {}
	
	String apiManagerResponse;
	
	private void readOrgsFromAPIManager(OrgFilter filter) throws AppException {
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.error("Using OrgAdmin only to load all organizations.");
		}
		if(apiManagerResponse != null) return;
		String orgId = "";
		if(filter.id!=null) {
			orgId = "/"+filter.id;
		}
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/organizations"+orgId)
					.addParameters(filter.filters)
					.build();
			RestAPICall getRequest = new GETRequest(uri, null, APIManagerAdapter.hasAdminAccount());
			httpResponse = getRequest.execute();
			apiManagerResponse = EntityUtils.toString(httpResponse.getEntity());
		} catch (Exception e) {
			LOG.error("Error cant read all orgs from API-Manager. Can't parse response: " + httpResponse);
			throw new AppException("Can't read all orgs from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public List<Organization> getOrgs(OrgFilter filter) throws AppException {
		readOrgsFromAPIManager(filter);
		try {
			List<Organization> allOrgs = mapper.readValue(this.apiManagerResponse, new TypeReference<List<Organization>>(){});
			return allOrgs;
		} catch (IOException e) {
			LOG.error("Error cant read all orgs from API-Manager. Can't parse response: " + apiManagerResponse);
			throw new AppException("Can't read all orgs from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public List<Organization> getAllOrgs() throws AppException {
		return getOrgs(new OrgFilter.Builder().build());
	}
	
	public String getOrgId(String orgName) throws AppException {
		Organization org = getOrg(new OrgFilter.Builder().hasName(orgName).build());
		return org.getId();
	}
	
	public Organization getOrg(OrgFilter filter) throws AppException {
		List<Organization> orgs = getOrgs(filter);
		if(orgs.size()>1) {
			throw new AppException("No unique Organization found", ErrorCode.UNKNOWN_API);
		}
		if(orgs.size()==0) return null;
		return orgs.get(0);
	}
	
	public void setAPIManagerTestResponse(String response) {
		this.apiManagerResponse = response;
	}
}
