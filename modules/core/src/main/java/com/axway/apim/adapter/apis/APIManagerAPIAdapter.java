package com.axway.apim.adapter.apis;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
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

public class APIManagerAPIAdapter extends APIAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAPIAdapter.class);

	JsonNode apiManagerResponse;
	
	CommandParameters params = CommandParameters.getInstance();

	public APIManagerAPIAdapter() {

	}

	/**
	 * Returns a list of requested proxies (Front-End APIs).
	 * @throws AppException if the API representation cannot be created
	 */
	private void readAPIsFromAPIManager(APIFilter filter) throws AppException {
		CommandParameters cmd = CommandParameters.getInstance();
		ObjectMapper mapper = new ObjectMapper();
		URI uri;
		try {
			List<NameValuePair> usedFilters = new ArrayList<>();
			if(APIManagerAdapter.hasAPIManagerVersion("7.7") && filter.apiPath != null) { // Since 7.7 we can query the API-PATH directly if given
				usedFilters.add(new BasicNameValuePair("field", "path"));
				usedFilters.add(new BasicNameValuePair("op", "eq"));
				usedFilters.add(new BasicNameValuePair("value", filter.apiPath));
			} 
			if(filter != null) { usedFilters.addAll(filter.filters); } 
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/"+filter.type)
					.addParameters(usedFilters)
					.build();
			LOG.info("Sending request to find existing APIs: " + uri);
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();

			apiManagerResponse = mapper.readTree(response);
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public JsonNode getAPI(APIFilter filter, boolean logMessage) throws AppException {
		List<JsonNode> foundAPIs = getAPIs(filter, logMessage);
		return uniqueAPI(foundAPIs);
	}
	
	private JsonNode uniqueAPI(List<JsonNode> foundAPIs) throws AppException {
		if(foundAPIs.size()>1) {
			throw new AppException("No unique API found", ErrorCode.UNKNOWN_API);
		}
		if(foundAPIs.size()==0) return null;
		return foundAPIs.get(0);
	}

	@Override
	public List<JsonNode> getAPIs(APIFilter filter, boolean logMessage) throws AppException {
		if(this.apiManagerResponse==null) readAPIsFromAPIManager(filter);
		String path;
		List<JsonNode> foundAPIs = new ArrayList<JsonNode>();
		if(filter.apiPath==null && filter.vhost==null && filter.queryStringVersion==null && apiManagerResponse.size()==1) {
			foundAPIs.add(apiManagerResponse.get(0));
			return foundAPIs;
		}
			for(JsonNode api : apiManagerResponse) {
				if(filter.apiPath==null && filter.vhost==null && filter.queryStringVersion==null) { // Nothing given to filter out.
					foundAPIs.add(api);
					continue;
				}
				if(filter.apiPath!=null && !filter.apiPath.equals(api.get("path").asText())) continue;
				if(filter.type.equals(APIManagerAdapter.TYPE_FRONT_END)) {
					if(filter.vhost!=null && !filter.vhost.equals(api.get("vhost").asText())) continue;
					if(filter.queryStringVersion!=null && !filter.queryStringVersion.equals(api.get("apiRoutingKey").asText())) continue;
				}
				path = api.get("path").asText();
				if(filter.type.equals(APIManagerAdapter.TYPE_BACK_END)) {
					String name = api.get("name").asText();
					if(logMessage) 
						LOG.info("Found existing Backend-API with name: '"+name+"' (ID: '" + api.get("id").asText()+"')");														
				} else {
					if(logMessage)
						LOG.info("Found existing API on path: '"+path+"' ("+api.get("state").asText()+") (ID: '" + api.get("id").asText()+"')");
				}
				foundAPIs.add(api);
			}
			if(foundAPIs.size()!=0) {
				String dbgCrit = "";
				if(foundAPIs.size()>1) 
					dbgCrit = " (apiPath: '"+filter.apiPath+"', filter: "+filter+", vhost: '"+filter.vhost+"', requestedType: "+filter.type+")";
				LOG.info("Found: "+foundAPIs.size()+" exposed API(s)" + dbgCrit);
				return foundAPIs;
			}
			LOG.info("No existing API found based on filter: " + getFilterFields(filter));
			return foundAPIs;
	}
	
	private String getFilterFields(APIFilter filter) {
		String filterFields = "[";
		if(filter.apiPath!=null) filterFields += "apiPath=" + filter.apiPath;
		if(filter.vhost!=null) filterFields += " vHost=" + filter.vhost;
		if(filter.queryStringVersion!=null) filterFields += " queryString=" + filter.queryStringVersion;
		if(filter!=null) filterFields += " filter=" + filter;
		filterFields += "]";
		return filterFields;
	}
	
	void setTestAPIManagerResponse(JsonNode apiManagerResponse) {
		this.apiManagerResponse = apiManagerResponse;
	}
}
