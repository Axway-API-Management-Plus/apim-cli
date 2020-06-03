package com.axway.apim.adapter.apis;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.jackson.JSONViews;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
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

	public APIManagerAPIAccessAdapter() {}
	
	Map<Type, Map<String, String>> apiManagerResponse = new HashMap<Type, Map<String,String>>();
	
	private void readAPIAccessFromAPIManager(Type type, String id) throws AppException {
		if(apiManagerResponse.get(type)!=null && apiManagerResponse.get(type).get(id)!=null) return;
		String response = null;
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/"+type+"/"+id+"/apis").build();
			RestAPICall getRequest = new GETRequest(uri, null, APIManagerAdapter.hasAdminAccount());
			httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			Map<String, String> mappedResponse = new HashMap<String, String>();
			mappedResponse.put(id, response);
			apiManagerResponse.put(type, mappedResponse);
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
	
	public APIAccess saveOrUpdateAPIAccess(APIAccess apiAccess, String parentId, Type type) throws AppException {
		List<APIAccess> existingAPIAccess = getAPIAccess(parentId, type);
		if(existingAPIAccess!=null && existingAPIAccess.contains(apiAccess)) return existingAPIAccess.get(0);
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/"+type+"/"+parentId+"/apis").build();
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			String json = mapper.writerWithView(JSONViews.APIAccessBase.class).writeValueAsString(apiAccess);
			HttpEntity entity = new StringEntity(json);
			// Use an admin account for this request
			RestAPICall request = new POSTRequest(entity, uri, null, true);
			request.setContentType("application/json");
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error creating/updating API Access. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
				throw new AppException("Error creating/updating API Access. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			return mapper.readValue(httpResponse.getEntity().getContent(), APIAccess.class);
		} catch (Exception e) {
			throw new AppException("Error creating/updating API Access.", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
	
	void setAPIManagerTestResponse(Type type, String id, String response) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(id, response);
		this.apiManagerResponse.put(type, map);
	}
}
