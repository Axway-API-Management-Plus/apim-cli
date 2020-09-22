package com.axway.apim.adapter.apis;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIManagerAdapter.CacheType;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class APIManagerOrganizationAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerOrganizationAdapter.class);
	
	CoreParameters cmd = CoreParameters.getInstance();
	
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
			uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/organizations"+orgId)
					.addParameters(filter.getFilters())
					.build();
			RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
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
	
	public Organization updateOrganization(Organization desiredOrg, Organization actualOrg) throws AppException {
		return createOrUpdateOrganization(desiredOrg, actualOrg);
	}
	
	public Organization createOrganization(Organization desiredOrg) throws AppException {
		return createOrUpdateOrganization(desiredOrg, null);
	}
	
	public Organization createOrUpdateOrganization(Organization desiredOrg, Organization actualOrg) throws AppException {
		HttpResponse httpResponse = null;
		Organization createdOrg;
		try {
			URI uri;
			if(actualOrg==null) {
				if(!APIManagerAdapter.hasAdminAccount()) {
					throw new AppException("Admin account is required to create a new organization", ErrorCode.NO_ADMIN_ROLE_USER);
				}
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/organizations").build();
			} else {
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/organizations/"+actualOrg.getId()).build();
			}
			FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
					SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"image", "createdOn"}));
			mapper.setFilterProvider(filter);
			mapper.setSerializationInclusion(Include.NON_NULL);
			try {
				RestAPICall request;
				if(actualOrg==null) {
					String json = mapper.writeValueAsString(desiredOrg);
					HttpEntity entity = new StringEntity(json);
					request = new POSTRequest(entity, uri, true);
				} else {
					desiredOrg.setId(actualOrg.getId());
					if (desiredOrg.getDn()==null) desiredOrg.setDn(actualOrg.getDn());
					String json = mapper.writeValueAsString(desiredOrg);
					HttpEntity entity = new StringEntity(json);
					request = new PUTRequest(entity, uri, true);
				}
				request.setContentType("application/json");
				httpResponse = request.execute();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if(statusCode < 200 || statusCode > 299){
					LOG.error("Error creating/updating organization. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
					throw new AppException("Error creating/updating organization. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
				}
				createdOrg = mapper.readValue(httpResponse.getEntity().getContent(), Organization.class);
			} catch (Exception e) {
				throw new AppException("Error creating/updating organization.", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
			} finally {
				try {
					((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) { }
			}
			desiredOrg.setId(createdOrg.getId());
			saveImage(desiredOrg, actualOrg);
			return createdOrg;

		} catch (Exception e) {
			throw new AppException("Error creating/updating organization", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
	}
	
	public void deleteOrganization(Organization org) throws AppException {
		HttpResponse httpResponse = null;
		URI uri;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/organizations/"+org.getId()).build();
			RestAPICall request = new DELRequest(uri, true);
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode != 204){
				LOG.error("Error deleting organization. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
				throw new AppException("Error deleting organization. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
		} catch (Exception e) {
			throw new AppException("Error deleting organization", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
	
	private void saveImage(Organization org, Organization actualOrg) throws URISyntaxException, AppException {
		if(org.getImage()==null) return;
		if(actualOrg!=null && org.getImage().equals(actualOrg.getImage())) return;
		HttpResponse httpResponse = null;
		URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/organizations/"+org.getId()+"/image").build();
		HttpEntity entity = MultipartEntityBuilder.create()
			.addBinaryBody("file", org.getImage().getInputStream(), ContentType.create("image/jpeg"), org.getImage().getBaseFilename())
			.build();
		try {
			RestAPICall apiCall = new POSTRequest(entity, uri);
			apiCall.setContentType(null);
			httpResponse = apiCall.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error saving/updating organization image. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
			}
		} catch (Exception e) {
			throw new AppException("Error uploading organization image", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
	
	public List<Organization> getOrgs(OrgFilter filter) throws AppException {
		readOrgsFromAPIManager(filter);
		try {
			List<Organization> allOrgs = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<Organization>>(){});
			allOrgs.removeIf(org -> filter.filter(org));
			for(int i=0; i<allOrgs.size();i++) {
				Organization org = allOrgs.get(i);
				addImage(org, filter.isIncludeImage());
			}
			return allOrgs;
		} catch (Exception e) {
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
	
	void addImage(Organization org, boolean addImage) throws Exception {
		if(!addImage) return;
		URI uri;
		if(org.getImageUrl()==null) return;
		uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/organizations/"+org.getId()+"/image")
				.build();
		Image image = APIManagerAdapter.getImageFromAPIM(uri, "org-image");
		org.setImage(image);
	}
	
	public void setAPIManagerTestResponse(OrgFilter key, String response) {
		this.apiManagerResponse.put(key, response);
	}
}
