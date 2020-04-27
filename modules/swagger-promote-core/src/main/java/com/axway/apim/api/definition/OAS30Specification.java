package com.axway.apim.api.definition;

import java.io.IOException;
import java.net.URL;

import com.axway.apim.api.IAPI;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OAS30Specification extends APISpecification {
	
	JsonNode openAPI = null;
	
	public OAS30Specification(byte[] apiSpecificationContent, String backendBasepath) throws AppException {
		super(apiSpecificationContent, backendBasepath);
	}

	@Override
	public int getAPIDefinitionType() throws AppException {
		return IAPI.OPEN_API_30;
	}

	@Override
	protected void configureBasepath() throws AppException {
		if(!CommandParameters.getInstance().replaceHostInSwagger()) return;
		try {
			if(this.backendBasepath!=null) {
				boolean backendBasepathAdjusted = false;
				URL backendBasepath = new URL(this.backendBasepath);
				for(JsonNode server : openAPI.get("servers")) {
					String url = server.get("url").asText();
					if(backendBasepath.equals(url)) {
						
					}
					LOG.debug("URL: " + url);
				}
				 ObjectNode newServer = objectMapper.createObjectNode();
				 newServer.put("url", backendBasepath.toString());
				 
				 //newServers.add(objectMapper. new JsonNode  url.getProtocol());
				((ArrayNode) openAPI.get("servers")).removeAll();
				((ArrayNode) openAPI.get("servers")).add(newServer);
				/*
				if(openAPI.get("servers").asText().equals(url.toString())) {
					LOG.debug("Swagger resourcePath: '"+swagger.get("basePath").asText()+"' already matches configured backendBasepath: '"+url.getPath()+"'. Nothing to do.");
				} else {
					LOG.debug("Replacing existing basePath: '"+swagger.get("basePath").asText()+"' in Swagger-File to '"+url.toString()+"' based on configured backendBasepath: '"+this.backendBasepath+"'");
					backendBasepathAdjusted = true;
					((ObjectNode)swagger).put("basePath", url.toString());
				}
				if(backendBasepathAdjusted) {
					LOG.info("Used the configured backendBasepath: '"+this.backendBasepath+"' to adjust the Swagger definition.");
				}*/
				this.apiSpecificationContent = objectMapper.writeValueAsBytes(openAPI);
			}
		} catch (Exception e) {
			LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
		}
	}
	
	@Override
	public boolean configure() throws AppException {
		try {
			openAPI = objectMapper.readTree(apiSpecificationContent);
			if(!(openAPI.has("openapi") && openAPI.get("openapi").asText().startsWith("3.0."))) {
				return false;
			}
			configureBasepath();
			return true;
		} catch (IOException e) {
			LOG.debug("Can't read apiSpecication. Doesn't have key \\\"openapi\\\" start with value: \\\"3.0.\"", e);
			return false;
		}
	}
}
