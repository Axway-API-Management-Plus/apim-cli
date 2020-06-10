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
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIManagerAdapter.CacheType;
import com.axway.apim.adapter.apis.jackson.JSONViews;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerAPIAccessAdapter {
	
	public static enum Type {
		organizations, 
		applications
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
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/"+type+"/"+id+"/apis").build();
			RestAPICall getRequest = new GETRequest(uri, null, APIManagerAdapter.hasAdminAccount());
			httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			if(response.startsWith("{")) { // Got a single response!
				response = "["+response+"]";
			}
			mappedResponse.put(id, response);
			apiManagerResponse.put(type, mappedResponse);
			putToCache(id, type, response);
		} catch (Exception e) {
			LOG.error("Error cant load API-Access for "+type+" from API-Manager. Can't parse response: " + response);
			throw new AppException("API-Access for "+type+" from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public List<APIAccess> getAPIAccess(String id, Type type) throws AppException {
		return getAPIAccess(id, type, false);
	}
	
	public List<APIAccess> getAPIAccess(String id, Type type, boolean includeAPIName) throws AppException {
		readAPIAccessFromAPIManager(type, id);
		String apiAccessResponse = null;
		try {
			apiAccessResponse = apiManagerResponse.get(type).get(id);
			List<APIAccess> allApiAccess = mapper.readValue(apiAccessResponse, new TypeReference<List<APIAccess>>(){});
			if(includeAPIName) {
				for(APIAccess apiAccess : allApiAccess) {
					API api = APIManagerAdapter.getInstance().apiAdapter.getAPI(new APIFilter.Builder().hasId(apiAccess.getApiId()).build(), false);
					apiAccess.setApiName(api.getName());
					apiAccess.setApiVersion(api.getVersion());
				}
			}
			return allApiAccess;
		} catch (Exception e) {
			LOG.error("Error cant load API-Access for "+type+" from API-Manager. Can't parse response: " + apiAccessResponse);
			throw new AppException("Error loading API-Access for "+type+" from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private String getFromCache(String id, Type type) {
		Cache<String, String> usedCache = caches.get(type);
		if(usedCache!=null && caches.get(type).get(id)!=null) {
			if(LOG.isDebugEnabled())
				LOG.info("Return APIAccess for " + type + ": " + id + " from cache.");
			return caches.get(type).get(id);
		} else {
			if(LOG.isDebugEnabled())
				LOG.info("No cache hit for APIAccess " + type + " " + id);
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
	
	public List<APIAccess> saveAPIAccess(List<APIAccess> apiAccess, String parentId, Type type) throws AppException {
		List<APIAccess> existingAPIAccess = getAPIAccess(parentId, type);
		
		List<APIAccess> toBeRemovedAccesses = getMissingAPIAccesses(existingAPIAccess, apiAccess);
		List<APIAccess> toBeAddeddAccesses = getMissingAPIAccesses(apiAccess, existingAPIAccess);

		for(APIAccess access : toBeRemovedAccesses) {
			deleteAPIAccess(access, parentId, type);
		}
		for(APIAccess access : toBeAddeddAccesses) {
			createAPIAccess(access, parentId, type);
		}
		return apiAccess;
	}
	
	public APIAccess createAPIAccess(APIAccess apiAccess, String parentId, Type type) throws AppException {
		List<APIAccess> existingAPIAccess = getAPIAccess(parentId, type);
		if(existingAPIAccess!=null && existingAPIAccess.contains(apiAccess)) {
			apiAccess.setId(existingAPIAccess.get(0).getId());
			return apiAccess;
		}
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/"+type+"/"+parentId+"/apis").build();
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			String json = mapper.writerWithView(JSONViews.APIAccessForAPIManager.class).writeValueAsString(apiAccess);
			HttpEntity entity = new StringEntity(json);
			// Use an admin account for this request
			RestAPICall request = new POSTRequest(entity, uri, null, APIManagerAdapter.hasAdminAccount());
			request.setContentType("application/json");
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error creating/updating API Access. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
				throw new AppException("Error creating/updating API Access. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			String response = EntityUtils.toString(httpResponse.getEntity());
			apiAccess =  mapper.readValue(response, APIAccess.class);
			// Clean cache for this ID (App/Org) to force reload next time
			removeFromCache(parentId, type);
			return apiAccess;
		} catch (Exception e) {
			throw new AppException("Error creating/updating API Access.", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
	
	public void deleteAPIAccess(APIAccess apiAccess, String parentId, Type type) throws AppException {
		List<APIAccess> existingAPIAccess = getAPIAccess(parentId, type);
		// Nothing to delete
		if(existingAPIAccess!=null && !existingAPIAccess.contains(apiAccess)) {
			return;
		}
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/"+type+"/"+parentId+"/apis/"+apiAccess.getId()).build();
			// Use an admin account for this request
			RestAPICall request = new DELRequest(uri, null, APIManagerAdapter.hasAdminAccount());
			request.setContentType("application/json");
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Can't delete API access requests for application. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
				throw new AppException("Can't delete API access requests for application. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			removeFromCache(parentId, type);
			return;
		} catch (Exception e) {
			throw new AppException("Can't delete API access requests for application.", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
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
