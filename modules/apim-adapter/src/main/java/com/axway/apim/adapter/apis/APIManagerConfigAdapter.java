package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerConfigAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerConfigAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;

	public APIManagerConfigAdapter() {}
	
	Map<Boolean, JsonNode> apiManagerResponse = new HashMap<Boolean, JsonNode>();
	
	private static final Map<String, Boolean> configFieldRequiresAdmin;
    static {
        Map<String, Boolean> temp = new HashMap<String, Boolean>();
        temp.put("apiRoutingKeyEnabled", true);
        configFieldRequiresAdmin = Collections.unmodifiableMap(temp);
    }
	
	private void readConfigFromAPIManager(boolean useAdmin) throws AppException {
		if(apiManagerResponse.get(useAdmin) != null) return;
		URI uri;
		HttpResponse httpResponse = null;
		try {			
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/config").build();
			RestAPICall getRequest = new GETRequest(uri, useAdmin);
			httpResponse = getRequest.execute();

			JsonNode apiManagerConfig;
			apiManagerConfig = mapper.readTree(EntityUtils.toString(httpResponse.getEntity()));
			apiManagerResponse.put(useAdmin, apiManagerConfig);
			
			
		} catch (Exception e) {
			LOG.error("Error cant read all orgs from API-Manager. Can't parse response: " + httpResponse, e);
			throw new AppException("Can't read all orgs from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
		
	/**
	 * Lazy helper method to get the actual API-Manager version. This is used to toggle on/off some 
	 * of the features (such as API-Custom-Properties)
	 * @return the API-Manager version as returned from the API-Manager REST-API /config endpoint
	 * @param configField name of the configField from API-Manager
	 * @throws AppException is something goes wrong.
	 */
	public String getApiManagerConfig(String configField) throws AppException {
		boolean useAdmin = (configFieldRequiresAdmin.containsKey(configField)) ? true : false;
		readConfigFromAPIManager(useAdmin);
		
		JsonNode retrievedConfigField = apiManagerResponse.get(useAdmin).get(configField);
		if(retrievedConfigField==null) {
			LOG.debug("Config field: '"+configField+"' is unsuporrted!");
			return "UnknownConfigField"+configField;
		}
		return retrievedConfigField.asText();
	}
	
	void setAPIManagerTestResponse(JsonNode jsonResponse, boolean useAdmin) {
		if(jsonResponse==null) {
			LOG.error("Test-Response is empty. Ignoring!");
			return;
		}
		this.apiManagerResponse.put(useAdmin, jsonResponse);
	}
	
	void setAPIManagerTestResponse(String response, boolean useAdmin) throws IOException {
		setAPIManagerTestResponse(mapper.readTree(response), useAdmin);
	}
}
