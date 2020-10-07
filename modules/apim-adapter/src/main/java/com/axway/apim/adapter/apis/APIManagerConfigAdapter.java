package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIManagerConfig;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerConfigAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerConfigAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;

	public APIManagerConfigAdapter() {}
	
	Map<Boolean, String> apiManagerResponse = new HashMap<Boolean, String>();
	
	Map<Boolean, APIManagerConfig> managerConfig = new HashMap<Boolean, APIManagerConfig>();
	
	/*private static final Map<String, Boolean> configFieldRequiresAdmin;
    static {
        Map<String, Boolean> temp = new HashMap<String, Boolean>();
        temp.put("apiRoutingKeyEnabled", true);
        configFieldRequiresAdmin = Collections.unmodifiableMap(temp);
    }*/
	
	private void readConfigFromAPIManager(boolean useAdmin) throws AppException {
		if(apiManagerResponse.get(useAdmin) != null) return;
		URI uri;
		HttpResponse httpResponse = null;
		try {			
			uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/config").build();
			RestAPICall getRequest = new GETRequest(uri, useAdmin);
			httpResponse = getRequest.execute();
			String response = EntityUtils.toString(httpResponse.getEntity());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error loading configuration from API-Manager. Response-Code: "+statusCode+". Got response: '"+response+"'");
				throw new AppException("Error loading configuration from API-Manager. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			apiManagerResponse.put(useAdmin, response);
		} catch (Exception e) {
			LOG.error("Error cant read configuration from API-Manager. Can't parse response: " + httpResponse, e);
			throw new AppException("Can't read configuration from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public APIManagerConfig getConfig(boolean useAdmin) throws AppException {
		if(managerConfig.get(useAdmin)!=null) return managerConfig.get(useAdmin);
		readConfigFromAPIManager(useAdmin);
		try {
			APIManagerConfig config = mapper.readValue(apiManagerResponse.get(useAdmin), APIManagerConfig.class);
			managerConfig.put(useAdmin, config);
			return config;
		} catch (IOException e) {
			throw new AppException("Error parsing API-Manager configuration", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	void setAPIManagerTestResponse(String jsonResponse, boolean useAdmin) {
		if(jsonResponse==null) {
			LOG.error("Test-Response is empty. Ignoring!");
			return;
		}
		this.apiManagerResponse.put(useAdmin, jsonResponse);
	}
}
