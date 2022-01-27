package com.axway.apim.api.definition;

import java.net.MalformedURLException;
import java.net.URL;

import com.axway.apim.api.API;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class OAS3xSpecification extends APISpecification {
	
	JsonNode openAPI = null;

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		if(this.mapper.getFactory() instanceof YAMLFactory) {
			return APISpecType.OPEN_API_30_YAML;
		}
		return APISpecType.OPEN_API_30;
	}

	@Override
	public void configureBasepath(String backendBasepath, API api) throws AppException {
		if(!CoreParameters.getInstance().isReplaceHostInSwagger()) return;
		try {
			if(backendBasepath!=null) {
				URL url = new URL(backendBasepath); // Parse it to make sure it is valid
				if(url.getPath()!=null && !url.getPath().equals("") && !backendBasepath.endsWith("/")) { // See issue #178
					backendBasepath += "/";
				}
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
	public boolean parse(byte[] apiSpecificationContent) throws AppException {
		try {
			super.parse(apiSpecificationContent); 
			setMapperForDataFormat();
			if(this.mapper==null) return false;
			openAPI = this.mapper.readTree(apiSpecificationContent);
			if(!(openAPI.has("openapi") && openAPI.get("openapi").asText().startsWith("3.0."))) {
				return false;
			}
			return true;
		} catch (AppException e) {
			if(e.getError()==ErrorCode.UNSUPPORTED_FEATURE) {
				throw e;
			}
			return false;
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
