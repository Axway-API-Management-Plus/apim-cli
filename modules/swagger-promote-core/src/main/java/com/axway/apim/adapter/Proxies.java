package com.axway.apim.adapter;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Proxies {
	
	private static Logger LOG = LoggerFactory.getLogger(Proxies.class);

	String requestedVhost;
	String requestedApiPath;
	String requestedQueryStringVersion;

	String requestedType;

	List<NameValuePair> filter;

	JsonNode apiManagerResponse;
	
	CommandParameters params = CommandParameters.getInstance();

	private Proxies() {

	}

	/**
	 * Returns a list of requested proxies (Front-End APIs).
	 */
	public void getProxies() throws AppException {
		CommandParameters cmd = CommandParameters.getInstance();
		ObjectMapper mapper = new ObjectMapper();
		URI uri;
		try {
			List<NameValuePair> usedFilters = new ArrayList<>();
			if(APIManagerAdapter.hasAPIManagerVersion("7.7") && requestedApiPath != null) { // Since 7.7 we can query the API-PATH directly if given
				usedFilters.add(new BasicNameValuePair("field", "path"));
				usedFilters.add(new BasicNameValuePair("op", "eq"));
				usedFilters.add(new BasicNameValuePair("value", requestedApiPath));
			} 
			if(filter != null) { usedFilters.addAll(filter); } 
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/"+requestedType)
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
	
	public JsonNode getAPI(boolean logMessage) throws AppException {
		List<JsonNode> foundAPIs = getAPIs(logMessage);
		return uniqueAPI(foundAPIs);
	}
	
	private JsonNode uniqueAPI(List<JsonNode> foundAPIs) throws AppException {
		if(foundAPIs.size()>1) {
			throw new AppException("No unique API found", ErrorCode.UNKNOWN_API);
		}
		if(foundAPIs.size()==0) return null;
		return foundAPIs.get(0);
	}

	public List<JsonNode> getAPIs(boolean logMessage) throws AppException {
		if(this.apiManagerResponse==null) getProxies();
		String path;
		List<JsonNode> foundAPIs = new ArrayList<JsonNode>();
		if(requestedApiPath==null && requestedVhost==null && requestedQueryStringVersion==null && apiManagerResponse.size()==1) {
			foundAPIs.add(apiManagerResponse.get(0));
			return foundAPIs;
		}
			for(JsonNode api : apiManagerResponse) {
				if(requestedApiPath==null && requestedVhost==null && requestedQueryStringVersion==null) { // Nothing given to filter out.
					foundAPIs.add(api);
					continue;
				}
				if(this.requestedApiPath!=null && !this.requestedApiPath.equals(api.get("path").asText())) continue;
				if(requestedType.equals(APIManagerAdapter.TYPE_FRONT_END)) {
					if(this.requestedVhost!=null && !this.requestedVhost.equals(api.get("vhost").asText())) continue;
					if(this.requestedQueryStringVersion!=null && !this.requestedQueryStringVersion.equals(api.get("apiRoutingKey").asText())) continue;
				}
				path = api.get("path").asText();
				if(requestedType.equals(APIManagerAdapter.TYPE_BACK_END)) {
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
					dbgCrit = " (apiPath: '"+requestedApiPath+"', filter: "+filter+", vhost: '"+requestedVhost+"', requestedType: "+requestedType+")";
				LOG.info("Found: "+foundAPIs.size()+" exposed API(s)" + dbgCrit);
				return foundAPIs;
			}
			LOG.info("No existing API found based on filter: " + getFilterFields());
			return foundAPIs;
	}
	
	private String getFilterFields() {
		String filterFields = "[";
		if(this.requestedApiPath!=null) filterFields += "apiPath=" + this.requestedApiPath;
		if(this.requestedVhost!=null) filterFields += " vHost=" + this.requestedVhost;
		if(this.requestedQueryStringVersion!=null) filterFields += " queryString=" + this.requestedQueryStringVersion;
		if(this.filter!=null) filterFields += " filter=" + this.filter;
		filterFields += "]";
		return filterFields;
	}

	public static class Builder {

		String requestedVhost;
		String requestedApiPath;
		String requestedQueryStringVersion;

		String requestedType;
		
		boolean uniqueAPI;

		List<NameValuePair> filter;

		JsonNode apiManagerResponse;
		
		public Builder(String requestedType) {
			super();
			this.requestedType = requestedType;
		}
		
		public Proxies build() {
			Proxies existingAPIs = new Proxies();
			existingAPIs.apiManagerResponse = this.apiManagerResponse;
			existingAPIs.filter = this.filter;
			existingAPIs.requestedApiPath = this.requestedApiPath;
			existingAPIs.requestedQueryStringVersion = this.requestedQueryStringVersion;
			existingAPIs.requestedType = this.requestedType;
			existingAPIs.requestedVhost = this.requestedVhost;
			return existingAPIs;
		}

		public Builder hasVHost(String requestedVhost) {
			this.requestedVhost = requestedVhost;
			return this;
		}

		public Builder hasApiPath(String requestedApiPath) {
			this.requestedApiPath = requestedApiPath;
			return this;
		}

		public Builder hasQueryStringVersion(String requestedQueryStringVersion) {
			this.requestedQueryStringVersion = requestedQueryStringVersion;
			return this;
		}

		public Builder useFilter(List<NameValuePair> filter) {
			this.filter = filter;
			return this;
		}

		public Builder setApiManagerResponse(JsonNode apiManagerResponse) {
			this.apiManagerResponse = apiManagerResponse;
			return this;
		}
	}
}
