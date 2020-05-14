package com.axway.apim.api.definition;

import java.io.IOException;
import java.net.URL;

import com.axway.apim.api.API;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OAS3xSpecification extends APISpecification {
	
	JsonNode openAPI = null;
	
	public OAS3xSpecification(byte[] apiSpecificationContent, String backendBasepath) throws AppException {
		super(apiSpecificationContent, backendBasepath);
	}

	@Override
	public int getAPIDefinitionType() throws AppException {
		return API.OPEN_API_30;
	}

	@Override
	protected void configureBasepath() throws AppException {
		if(!CommandParameters.getInstance().replaceHostInSwagger()) return;
		try {
			if(this.backendBasepath!=null) {
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
			if(LOG.isTraceEnabled()) {
				LOG.trace("No OpenAPI 3.0 specification. Doesn't have key \"openapi\" starting with value: \"3.0.\"", e);
			} else {
				LOG.debug("No OpenAPI 3.0 specification. Doesn't have key \"openapi\" starting with value: \"3.0.\"");	
			}
			return false;
		}
	}

	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		return super.equals(other);
	}
	
	
}
