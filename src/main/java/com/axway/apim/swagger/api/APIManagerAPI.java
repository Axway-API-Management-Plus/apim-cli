package com.axway.apim.swagger.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.APIAuthentication;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APIManagerAPI extends AbstractAPIDefinition implements IAPIDefinition {
	
	static Logger LOG = LoggerFactory.getLogger(APIManagerAPI.class);

	JsonNode apiConfiguration;

	public APIManagerAPI(IAPIDefinition desiredAPI) throws AppException {
		super();
		initAPIFromAPIManager(desiredAPI);
		// Only try to load a Backend-API if we found an API in API-Manager
		if(this.isValid) {
			this.swaggerDefinition = new APISwaggerDefinion(getOriginalSwaggerFromAPIM());
		}
	}
	public APIManagerAPI(JsonNode apiConfiguration) {
		this.apiConfiguration = apiConfiguration;
	}
	
	
	public void setApiConfiguration(JsonNode apiConfiguration) {
		this.apiConfiguration = apiConfiguration;
	}

	private void initAPIFromAPIManager(IAPIDefinition desiredAPI) throws AppException {
		URI uri;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			
			JsonNode jsonResponse;
			String path;
			String apiId = null;
			try {
				jsonResponse = objectMapper.readTree(response);
				for(JsonNode node : jsonResponse) {
					path = node.get("path").asText();
					if(path.equals(desiredAPI.getApiPath())) {
						LOG.info("Found existing API on path: '"+path+"' / "+node.get("state").asText()+" ('" + node.get("id").asText()+"')");
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
				getRequest = new GETRequest(uri, null);
				response = getRequest.execute().getEntity().getContent();
				jsonResponse = objectMapper.readTree(response);
				this.apiConfiguration = jsonResponse;
				this.isValid = true; // Mark that we found an existing API
			} catch (IOException e) {
				throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			}
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private byte[] getOriginalSwaggerFromAPIM() throws AppException {
		URI uri;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/apirepo/"+getBackendApiId()+"/download")
					.setParameter("original", "true").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			return IOUtils.toByteArray(response);
		} catch (Exception e) {
			throw new AppException("Can't read Swagger-File.", ErrorCode.CANT_READ_SWAGGER_FILE, e);
		}
	}

	@Override
	public String getApiVersion() {
		return this.apiConfiguration.get("version").asText();
	}

	@Override
	public APIAuthentication getAuthentication() throws AppException {
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
	public String getApiSummary() {
		JsonNode node = this.apiConfiguration.get("summary");
		if(node instanceof MissingNode || node.asText().equals("null")) return null;
		return node.asText();
	}

	@Override
	public String getStatus() {
		return this.apiConfiguration.get("state").asText();
	}
	
	@Override
	public void setStatus(String status) {
		((ObjectNode)this.apiConfiguration).put("state", status);
	}

	@Override
	public String getApiId() {
		return this.apiConfiguration.get("id").asText();
	}
	
	public String getBackendApiId() {
		return this.apiConfiguration.get("apiId").asText();
	}

	@Override
	public String getApiPath() {
		return this.apiConfiguration.get("path").asText();
	}
}
