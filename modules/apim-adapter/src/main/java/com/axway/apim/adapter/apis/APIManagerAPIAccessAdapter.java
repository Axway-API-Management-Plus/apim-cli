package com.axway.apim.adapter.apis;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIManagerAdapter.CacheType;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.AbstractEntity;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class APIManagerAPIAccessAdapter {
	
	public static enum Type {
		organizations("Organization"), 
		applications("Application");
		
		String niceName;

		private Type(String niceName) {
			this.niceName = niceName;
		}
	}
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAPIAccessAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	private Map<Type, Cache<String, String>> caches = new HashMap<Type, Cache<String, String>>();

	public APIManagerAPIAccessAdapter() {
		caches.put(Type.applications, APIManagerAdapter.getCache(CacheType.applicationAPIAccessCache, String.class, String.class));
		caches.put(Type.organizations, APIManagerAdapter.getCache(CacheType.organizationAPIAccessCache, String.class, String.class));
	}
	
	Map<Type, Map<String, String>> apiManagerResponse = new HashMap<Type, Map<String,String>>();
	
	private void readAPIAccessFromAPIManager(Type type, String id) throws AppException {
		if(apiManagerResponse.get(type)!=null && apiManagerResponse.get(type).get(id)!=null) return;
		Map<String, String> mappedResponse = new HashMap<String, String>();
		
		String cachedResponse = getFromCache(id, type);
		if(cachedResponse!=null) {
			mappedResponse.put(id, cachedResponse);
			if(cachedResponse!=null) apiManagerResponse.put(type, mappedResponse);
			return;
		}
		String response = null;
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/"+type+"/"+id+"/apis").build();
			RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
			httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error loading API-Access from API-Manager for "+type+". Response-Code: "+statusCode+". Got response: '"+response+"'");
				throw new AppException("Error loading API-Access from API-Manager for "+type+". Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			if(response.startsWith("{")) { // Got a single response!
				response = "["+response+"]";
			}
			mappedResponse.put(id, response);
			apiManagerResponse.put(type, mappedResponse);
			putToCache(id, type, response);
		} catch (Exception e) {
			LOG.error("Error loading API-Access from API-Manager for "+type+" from API-Manager: " + response, e);
			throw new AppException("Error loading API-Access from API-Manager for "+type+" from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public List<APIAccess> getAPIAccess(AbstractEntity entity, Type type) throws AppException {
		return getAPIAccess(entity, type, false);
	}
	
	public List<APIAccess> getAPIAccess(AbstractEntity entity, Type type, boolean includeAPIName) throws AppException {
		readAPIAccessFromAPIManager(type, entity.getId());
		String apiAccessResponse = null;
		try {
			apiAccessResponse = apiManagerResponse.get(type).get(entity.getId());
			List<APIAccess> allApiAccess = mapper.readValue(apiAccessResponse, new TypeReference<List<APIAccess>>(){});
			if(includeAPIName) {
				for(APIAccess apiAccess : allApiAccess) {
					API api = APIManagerAdapter.getInstance().apiAdapter.getAPI(new APIFilter.Builder().hasId(apiAccess.getApiId()).build(), false);
					if(api==null) {
						throw new AppException("Unable to find API with ID: " + apiAccess.getApiId() + " referenced by "+type.niceName+": " + entity.getName(), ErrorCode.UNKNOWN_API);
					}
					apiAccess.setApiName(api.getName());
					apiAccess.setApiVersion(api.getVersion());
				}
			}
			return allApiAccess;
		} catch (Exception e) {
			LOG.error("Error loading API-Access for "+type+" from API-Manager. Can't process response: " + apiAccessResponse, e);
			throw new AppException("Error loading API-Access for "+type+" from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private String getFromCache(String id, Type type) {
		Cache<String, String> usedCache = caches.get(type);
		if(usedCache!=null && caches.get(type).get(id)!=null) {
			LOG.trace("Return APIAccess for " + type + ": " + id + " from cache.");
			return caches.get(type).get(id);
		} else {
			LOG.trace("No cache hit for APIAccess " + type + " " + id);
			return null;
		}
	}
	
	private void putToCache(String id, Type type, String allApiAccess) {
		Cache<String, String> usedCache = caches.get(type);
		if(usedCache!=null) {
			usedCache.put(id, allApiAccess);
		}
	}
	
	private void removeFromCache(String id, Type type) {
		Cache<String, String> usedCache = caches.get(type);
		if(usedCache!=null) {
			usedCache.remove(id);
		}
	}
	
	public List<APIAccess> saveAPIAccess(List<APIAccess> apiAccess, AbstractEntity entity, Type type) throws AppException {
		List<APIAccess> existingAPIAccess = getAPIAccess(entity, type);
		
		List<APIAccess> toBeRemovedAccesses = getMissingAPIAccesses(existingAPIAccess, apiAccess);
		List<APIAccess> toBeAddeddAccesses = getMissingAPIAccesses(apiAccess, existingAPIAccess);

		for(APIAccess access : toBeRemovedAccesses) {
			deleteAPIAccess(access, entity, type);
		}
		for(APIAccess access : toBeAddeddAccesses) {
			createAPIAccess(access, entity, type);
		}
		return apiAccess;
	}
	
	public APIAccess createAPIAccess(APIAccess apiAccess, AbstractEntity parentEntity, Type type) throws AppException {
		List<APIAccess> existingAPIAccess = getAPIAccess(parentEntity, type);
		if(existingAPIAccess!=null && existingAPIAccess.contains(apiAccess)) {
			apiAccess.setId(existingAPIAccess.get(0).getId());
			return apiAccess;
		}
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/"+type+"/"+parentEntity.getId()+"/apis").build();
			mapper.setSerializationInclusion(Include.NON_NULL);
			FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
					SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"apiName"}));
			mapper.setFilterProvider(filter);
			String json = mapper.writeValueAsString(apiAccess);
			HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
			RestAPICall request = new POSTRequest(entity, uri, APIManagerAdapter.hasAdminAccount());
			request.setContentType("application/json");
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String response = EntityUtils.toString(httpResponse.getEntity());
			if(statusCode < 200 || statusCode > 299){
				if(statusCode==403 && response.contains("Unknown API")) {
					LOG.warn("Got unexpected error: 'Unknown API' while creating API-Access ... Try again in 1 second.");
					Thread.sleep(1000);
					httpResponse = request.execute();
					response = EntityUtils.toString(httpResponse.getEntity());
					statusCode = httpResponse.getStatusLine().getStatusCode();
					if(statusCode < 200 || statusCode > 299){
						LOG.error("Error creating/updating API Access: "+apiAccess+". Response-Code: "+statusCode+". Got response: '"+response+"'");
						throw new AppException("Error creating/updating API Access. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
					} else {
						LOG.info("Successfully created API-Access on retry. Received Status-Code: " +statusCode );
					}
				} else if(statusCode==409 && response.contains("resource already exists")) {
					LOG.debug("Unexpected response while creating/updating API Access: "+apiAccess+". Response-Code: "+statusCode+". Got response: '"+response+"'. Ignoring this error.");
					return apiAccess;
				} else {
					LOG.error("Error creating/updating API Access: "+apiAccess+". Response-Code: "+statusCode+". Got response: '"+response+"'");
					throw new AppException("Error creating/updating API Access. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
				}
			}
			apiAccess =  mapper.readValue(response, APIAccess.class);
			// Clean cache for this ID (App/Org) to force reload next time
			removeFromCache(parentEntity.getId(), type);
			return apiAccess;
		} catch (Exception e) {
			throw new AppException("Error creating/updating API Access.", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
	
	public void deleteAPIAccess(APIAccess apiAccess, AbstractEntity parentEntity, Type type) throws AppException {
		List<APIAccess> existingAPIAccess = getAPIAccess(parentEntity, type);
		// Nothing to delete
		if(existingAPIAccess!=null && !existingAPIAccess.contains(apiAccess)) {
			return;
		}
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/"+type+"/"+parentEntity.getId()+"/apis/"+apiAccess.getId()).build();
			// Use an admin account for this request
			RestAPICall request = new DELRequest(uri, APIManagerAdapter.hasAdminAccount());
			request.setContentType("application/json");
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Can't delete API access requests for application. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
				throw new AppException("Can't delete API access requests for application. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			removeFromCache(parentEntity.getId(), type);
			return;
		} catch (Exception e) {
			throw new AppException("Can't delete API access requests for application.", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
	
	public void removeClientOrganization(List<Organization> removingActualOrgs, String apiId) throws AppException {
		for(Organization org : removingActualOrgs) {
			List<APIAccess> orgsApis = getAPIAccess(org, Type.organizations);
			for(APIAccess apiAccess : orgsApis) {
				if(apiAccess.getApiId().equals(apiId)) {
					try {
						deleteAPIAccess(apiAccess, org, Type.organizations);
					} catch (Exception e) {
						LOG.error("Can't delete API-Access for organization. ");
						throw new AppException("Can't delete API-Access for organization.", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
					}	
				}
			}
		}
	}
	
	private List<APIAccess> getMissingAPIAccesses(List<APIAccess> apiAccess, List<APIAccess> otherApiAccess) throws AppException {
		List<APIAccess> missingAccess = new ArrayList<APIAccess>();
		if(otherApiAccess == null) otherApiAccess = new ArrayList<APIAccess>();
		if(apiAccess == null) apiAccess = new ArrayList<APIAccess>();
		for(APIAccess access : apiAccess) {
			if(otherApiAccess.contains(access)) {
				continue;
			}
			missingAccess.add(access);
		}
		return missingAccess;
	}
	
	void setAPIManagerTestResponse(Type type, String id, String response) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(id, response);
		this.apiManagerResponse.put(type, map);
	}
}
