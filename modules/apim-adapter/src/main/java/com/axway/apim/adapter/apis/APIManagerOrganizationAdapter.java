package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIManagerAdapter.CacheType;
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
	
	Map<OrgFilter, String> apiManagerResponse = new HashMap<OrgFilter, String>();
	
	Cache<String, String> organizationCache;
	
	public APIManagerOrganizationAdapter() {
		organizationCache = APIManagerAdapter.getCache(CacheType.organizationCache, String.class, String.class);
	}
	
	private void readOrgsFromAPIManager(OrgFilter filter) throws AppException {
		if(apiManagerResponse.get(filter) != null) return;
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Using OrgAdmin only to load all organizations.");
		}
		String orgId = "";
		// In some versions (e.g. 7.6.2 - 7.6.2 SP4 confirmed) an Org-Admin can't provide it's own Org-Id to load it's own org
		// Therefore we need to skip that here
		if(filter.getId()!=null && APIManagerAdapter.hasAdminAccount()) {
			if(organizationCache.containsKey(filter.getId())) {
				apiManagerResponse.put(filter, organizationCache.get(filter.getId()));
				return;
			}
			orgId = "/"+filter.getId();
		}
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/organizations"+orgId)
					.addParameters(filter.getFilters())
					.build();
			RestAPICall getRequest = new GETRequest(uri, null, APIManagerAdapter.hasAdminAccount());
			LOG.debug("Load organizations from API-Manager using filter: " + filter);
			LOG.trace("Load organization with URI: " + uri);
			httpResponse = getRequest.execute();
			if(httpResponse.getStatusLine().getStatusCode()!=HttpStatus.SC_OK) {
				LOG.error("Sent request: " + uri);
				LOG.error("Received Status-Code: " +httpResponse.getStatusLine().getStatusCode()+ ", Response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
				throw new AppException("", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			String response = EntityUtils.toString(httpResponse.getEntity());
			if(!orgId.equals("")) {
				// Store it as an Array
				response = "[" + response+ "]";
				apiManagerResponse.put(filter, response);
				organizationCache.put(orgId, response);
			} else {
				// We got an Array from API-Manager
				apiManagerResponse.put(filter, response);
			}
		} catch (Exception e) {
			LOG.error("Error cant read orgs from API-Manager with filter: "+filter+". Can't parse response: " + httpResponse, e);
			throw new AppException("Error cant read orgs from API-Manager with filter: "+filter, ErrorCode.API_MANAGER_COMMUNICATION, e);
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
			List<Organization> allOrgs = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<Organization>>(){});
			return allOrgs;
		} catch (IOException e) {
			LOG.error("Error cant read orgs from API-Manager with filter: "+filter+". Returned response: " + apiManagerResponse);
			throw new AppException("Error cant read orgs from API-Manager with filter: "+filter, ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public List<Organization> getAllOrgs() throws AppException {
		return getOrgs(new OrgFilter.Builder().build());
	}
	
	public Organization getOrgForName(String orgName) throws AppException {
		Organization org = getOrg(new OrgFilter.Builder().hasName(orgName).build());
		return org;
	}
	
	public Organization getOrgForId(String orgId) throws AppException {
		Organization org = getOrg(new OrgFilter.Builder().hasId(orgId).build());
		return org;
	}
	
	public Organization getOrg(OrgFilter filter) throws AppException {
		List<Organization> orgs = getOrgs(filter);
		if(orgs.size()>1) {
			throw new AppException("No unique Organization found", ErrorCode.UNKNOWN_API);
		}
		if(orgs.size()==0) {
			LOG.info("No organization found using filter: " + filter);
			return null;
		}
		return orgs.get(0);
	}
	
	public void setAPIManagerTestResponse(OrgFilter key, String response) {
		this.apiManagerResponse.put(key, response);
	}
}
