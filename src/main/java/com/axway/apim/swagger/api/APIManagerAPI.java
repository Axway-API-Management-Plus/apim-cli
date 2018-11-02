package com.axway.apim.swagger.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.CommandParameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerAPI extends AbstractAPIDefinition implements IAPIDefinition {
	
	static Logger LOG = LoggerFactory.getLogger(APIManagerAPI.class);

	JsonNode apiConfiguration;

	public APIManagerAPI(IAPIDefinition desiredAPI) {
		super();
		initAPIFromAPIManager(desiredAPI);
	}
	
	public void setApiConfiguration(JsonNode apiConfiguration) {
		this.apiConfiguration = apiConfiguration;
	}

	private void initAPIFromAPIManager(IAPIDefinition desiredAPI) {
		URI uri;
		CommandParameters cmd = CommandParameters.getInstance();
		ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/discovery/apis").build();
			RestAPICall getRequest = new GETRequest(uri);
			InputStream response = getRequest.execute();
			
			JsonNode jsonResponse;
			String apiUri;
			String apiId = null;
			try {
				jsonResponse = objectMapper.readTree(response);
				for(JsonNode node : jsonResponse) {
					apiUri = node.get("uri").asText();
					if(apiUri.endsWith("/discovery/swagger/api" + desiredAPI.getApiPath())) {
						LOG.info("Found existing API: " + node);
						apiId = node.get("id").asText();
						break;
					}
				}
				if(apiId==null) {
					this.isValid = false;
					LOG.info("No existing API found exposed on: " + desiredAPI.getApiPath());
					return;
				}
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+apiId).build();
				getRequest = new GETRequest(uri);
				response = getRequest.execute();
				jsonResponse = objectMapper.readTree(response);
				this.apiConfiguration = jsonResponse;
				this.isValid = true; // Mark that we found an existing API
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getApiVersion() {
		return this.apiConfiguration.get("version").asText();
	}

	@Override
	public APIAuthentication getAuthentication() {
		if(authentication==null) {
			this.authentication = new APIAuthentication(this.apiConfiguration.get("securityProfiles").get(0).get("devices"));
		}
		return this.authentication;
	}

	@Override
	public String getApiName() {
		return this.apiConfiguration.get("name").asText();
	}

	@Override
	public String getStatus() {
		return this.apiConfiguration.get("state").asText();
	}
	
	@Override
	public String getApiId() {
		return this.apiConfiguration.get("id").asText();
	}

	@Override
	public String getApiPath() {
		return this.apiConfiguration.get("path").asText();
	}
}
