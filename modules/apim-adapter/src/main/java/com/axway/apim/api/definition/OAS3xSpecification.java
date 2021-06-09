package com.axway.apim.api.definition;

import java.net.MalformedURLException;
import java.net.URL;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class OAS3xSpecification extends APISpecification {
	
	JsonNode openAPI = null;
	
	public OAS3xSpecification(byte[] apiSpecificationContent) throws AppException {
		super(apiSpecificationContent);
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		if(this.mapper.getFactory() instanceof YAMLFactory) {
			return APISpecType.OPEN_API_30_YAML;
		}
		return APISpecType.OPEN_API_30;
	}

	@Override
	public void configureBasepath(String backendBasepath) throws AppException {
		if(!CoreParameters.getInstance().isReplaceHostInSwagger()) return;
		try {
			if(backendBasepath!=null) {
				new URL(backendBasepath); // Parse it to make sure it is valid
				ObjectNode newServer = this.mapper.createObjectNode();
				newServer.put("url", backendBasepath);
				if(openAPI.has("servers")) {
					((ArrayNode) openAPI.get("servers")).removeAll();
				}
				((ObjectNode)openAPI).set("servers", mapper.createArrayNode().add(newServer));
				this.apiSpecificationContent = this.mapper.writeValueAsBytes(openAPI);
			}
		} catch (MalformedURLException e) {
			throw new AppException("The configured backendBasepath: '"+backendBasepath+"' is invalid.", ErrorCode.BACKEND_BASEPATH_IS_INVALID, e);
		} catch (Exception e) {
			LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
		}
	}
	
	@Override
	public boolean configure() throws AppException {
		try {
			setMapperForDataFormat();
			if(this.mapper==null) return false;
			openAPI = this.mapper.readTree(apiSpecificationContent);
			if(!(openAPI.has("openapi") && openAPI.get("openapi").asText().startsWith("3.0."))) {
				return false;
			}
			return true;
		} catch (Exception e) {
			LOG.trace("No OpenAPI 3.0 specification.", e);
			return false;
		}
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}
	
	
}
