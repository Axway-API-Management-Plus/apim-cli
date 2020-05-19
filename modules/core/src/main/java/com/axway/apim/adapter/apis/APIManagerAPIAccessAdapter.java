package com.axway.apim.adapter.apis;

import java.net.URI;
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
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
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
		readAPIAccessFromAPIManager(type, id);
		String apiAccessResponse = null;
		try {
			apiAccessResponse = apiManagerResponse.get(type).get(id);
			List<APIAccess> allApiAccess = mapper.readValue(apiAccessResponse, new TypeReference<List<APIAccess>>(){});
			return allApiAccess;
		} catch (Exception e) {
			LOG.error("Error cant load API-Access for "+type+" from API-Manager. Can't parse response: " + apiAccessResponse);
			throw new AppException("API-Access for "+type+" from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	void setAPIManagerTestResponse(Type type, String id, String response) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(id, response);
		this.apiManagerResponse.put(type, map);
	}
}
