package com.axway.apim.api.definition;

import java.net.URL;

import com.axway.apim.api.IAPI;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Swagger1xSpecification extends APISpecification {
	
	JsonNode swagger = null;
	
	public Swagger1xSpecification(byte[] apiSpecificationContent, String backendBasepath) throws AppException {
		super(apiSpecificationContent, backendBasepath);
	}

	@Override
	public int getAPIDefinitionType() throws AppException {
		return IAPI.SWAGGGER_API_12;
	}

	@Override
	protected void configureBasepath() throws AppException {
		if(!CommandParameters.getInstance().replaceHostInSwagger()) return;
		try {
			if(this.backendBasepath!=null) {
				boolean backendBasepathAdjusted = false;
				URL url = new URL(this.backendBasepath);
				
				if(swagger.get("basePath").asText().equals(url.toString())) {
					LOG.debug("Swagger resourcePath: '"+swagger.get("basePath").asText()+"' already matches configured backendBasepath: '"+url.getPath()+"'. Nothing to do.");
				} else {
					LOG.debug("Replacing existing basePath: '"+swagger.get("basePath").asText()+"' in Swagger-File to '"+url.toString()+"' based on configured backendBasepath: '"+this.backendBasepath+"'");
					backendBasepathAdjusted = true;
					((ObjectNode)swagger).put("basePath", url.toString());
				}
				if(backendBasepathAdjusted) {
					LOG.info("Used the configured backendBasepath: '"+this.backendBasepath+"' to adjust the Swagger definition.");
				}
				this.apiSpecificationContent = objectMapper.writeValueAsBytes(swagger);
			}
		} catch (Exception e) {
			LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
		}
	}
	
	@Override
	public boolean configure() throws AppException {
		try {
			swagger = objectMapper.readTree(apiSpecificationContent);
			if(!(swagger.has("swaggerVersion") && swagger.get("swaggerVersion").asText().startsWith("1."))) {
				return false;
			}
			configureBasepath();
			return true;
		} catch (Exception e) {
			if(LOG.isTraceEnabled()) {
				LOG.trace("No Swager 1.x specification. Doesn't have key \"swaggerVersion\" starting with value: \"1.\"", e);
			} else {
				LOG.debug("No Swager 1.x specification. Doesn't have key \"swaggerVersion\" starting with value: \"1.\"");	
			}
			return false;
		}
	}
}
