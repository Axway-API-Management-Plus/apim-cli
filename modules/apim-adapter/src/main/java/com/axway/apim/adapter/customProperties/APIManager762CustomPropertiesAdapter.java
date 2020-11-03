package com.axway.apim.adapter.customProperties;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.CustomProperties;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManager762CustomPropertiesAdapter extends APIManagerCustomPropertiesAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManager762CustomPropertiesAdapter.class);

	public APIManager762CustomPropertiesAdapter() {
		super();
	}

	@Override
	public CustomProperties getCustomProperties() throws AppException {
		if(customProperties!=null) return customProperties;
		readCustomPropertiesFromAPIManager();
		
		customProperties = parseAppConfig(apiManagerResponse);

		return customProperties;
	}
	
	private void readCustomPropertiesFromAPIManager() throws AppException {
		if(apiManagerResponse != null) return;
		URI uri;
		HttpResponse httpResponse = null;
		try {			
			uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath("/vordel/apiportal/app/app.config").build();
			RestAPICall getRequest = new GETRequest(uri);
			httpResponse = getRequest.execute();
			String response = EntityUtils.toString(httpResponse.getEntity());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error loading custom-properties from API-Manager 7.6.2. Response-Code: "+statusCode+". Got response: '"+response+"'");
				throw new AppException("Error loading custom-properties from API-Manager 7.6.2. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			apiManagerResponse = response;
		} catch (Exception e) {
			LOG.error("Error cant read custom-properties from API-Manager. Can't parse response: " + httpResponse, e);
			throw new AppException("Can't read custom-properties from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	/*public static Map<String, String> getAllConfiguredCustomProperties(CUSTOM_PROP_TYPE type) {
		Map<String, String> allCustomProps = new HashMap<String, String>();
		try {
			JsonNode appConfig = readCustomPropertiesFromAPIManager();
			JsonNode apiCustomProps = appConfig.get(type.name());
			if(apiCustomProps==null) return null;
			Iterator<Entry <String, JsonNode>> it = apiCustomProps.fields();
			while(it.hasNext()) {
				Entry<String, JsonNode> entry = it.next();
				allCustomProps.put(entry.getKey(), null);
			}
			return allCustomProps;
		} catch (Exception e) {
			LOG.error("Error loading configured custom properties from API-Manager", e);
			return null;
		}
	}*/
	
	/**
	 * Helper method to validate that configured Custom-Properties are really configured 
	 * in the API-Manager configuration.<br>
	 * Will become obsolete sine the API-Manager REST-API provides an endpoint for that.
	 * @param appConfig from the API-Manager (which isn't JSON)
	 * @return JSON-Configuration with the custom-properties section
	 * @throws AppException if the app.config can't be parsed
	 */
	public static CustomProperties parseAppConfig(String appConfig) throws AppException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			appConfig = appConfig.substring(appConfig.indexOf("customPropertiesConfig:")+23, appConfig.indexOf("wizardModels"));
			//appConfig = appConfig.substring(0, appConfig.length()-1); // Remove the tail comma
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
			mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			return mapper.readValue(appConfig, CustomProperties.class);
		} catch (Exception e) {
			throw new AppException("Can't parse API-Manager app.config.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
}
