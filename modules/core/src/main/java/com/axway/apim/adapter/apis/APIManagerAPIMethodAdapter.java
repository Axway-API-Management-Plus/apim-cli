package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerAPIMethodAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAPIMethodAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;

	public APIManagerAPIMethodAdapter() {}
	
	Map<String, String> apiManagerResponse = new HashMap<String, String>();
	
	private void readMethodsFromAPIManager(String apiId) throws AppException {
		if(this.apiManagerResponse.get(apiId)!=null) return;
		String response = null;
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+apiId+"/operations").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			httpResponse = getRequest.execute();
			this.apiManagerResponse.put(apiId,EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			LOG.error("Error cant load API-Methods for API: '"+apiId+"' from API-Manager. Can't parse response: " + response);
			throw new AppException("Error cant load API-Methods for API: '"+apiId+"' from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public List<APIMethod> getAllMethodsForAPI(String apiId) throws AppException {
		readMethodsFromAPIManager(apiId);
		List<APIMethod> apiMethods = new ArrayList<APIMethod>();
		try {
			apiMethods = mapper.readValue(this.apiManagerResponse.get(apiId), new TypeReference<List<APIMethod>>(){});
		} catch (IOException e) {
			throw new AppException("Error cant load API-Methods for API: '"+apiId+"' from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return apiMethods;
	}
	
	public APIMethod getMethodForName(String apiId, String methodName) throws AppException {
		List<APIMethod> apiMethods = getAllMethodsForAPI(apiId);
		if(apiMethods.size()==0) {
			LOG.warn("No operations found for API with id: " + apiId);
			return null;
		}
		for(APIMethod method : apiMethods) {
			String operationName = method.getName();
			if(operationName.equals(methodName)) {
				return method;
			}
		}
		LOG.warn("No operation found with name: '"+methodName+"' for API: '"+apiId+"'");
		return null;
	}
	
	public APIMethod getMethodForId(String apiId, String methodId) throws AppException {
		List<APIMethod> apiMethods = getAllMethodsForAPI(apiId);
		if(apiMethods.size()==0) {
			LOG.warn("No operations found for API with id: " + apiId);
			return null;
		}
		for(APIMethod method : apiMethods) {
			String operationId = method.getId();
			if(operationId.equals(methodId)) {
				return method;
			}
		}
		LOG.warn("No operation found with name: '"+methodId+"' for API: '"+apiId+"'");
		return null;
	}
	
	
	
	void setAPIManagerTestResponse(String apiId, String response) {
		this.apiManagerResponse.put(apiId, response);
	}
}
