package com.axway.apim.api.definition;

import java.net.URL;

import com.axway.apim.api.IAPI;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Swagger20Specification extends APISpecification {
	
	JsonNode swagger = null;
	

	public Swagger20Specification() {
		super();
	}

	public Swagger20Specification(byte[] apiSpecificationContent, String backendBasepath) throws AppException {
		super(apiSpecificationContent, backendBasepath);
	}

	@Override
	public int getAPIDefinitionType() throws AppException {
		return IAPI.SWAGGGER_API_20;
	}

	@Override
	protected void configureBasepath() throws AppException {
		if(!CommandParameters.getInstance().replaceHostInSwagger()) return;
		try {
			if(this.backendBasepath!=null) {
				boolean backendBasepathAdjusted = false;
				URL url = new URL(this.backendBasepath);
				String port = url.getPort()==-1 ? ":"+String.valueOf(url.getDefaultPort()) : ":"+String.valueOf(url.getPort());
				if(port.equals(":443") || port.equals(":80")) port = "";
				
				
				if(swagger.get("host")==null) {
					LOG.debug("Adding new host '"+url.getHost()+port+"' to Swagger-File based on configured backendBasepath: '"+this.backendBasepath+"'");
					backendBasepathAdjusted = true;
					((ObjectNode)swagger).put("host", url.getHost()+port);
				} else {
					if(swagger.get("host").asText().equals(url.getHost()+port)) {
						LOG.debug("Swagger Host: '"+swagger.get("host").asText()+"' already matches configured backendBasepath: '"+this.backendBasepath+"'. Nothing to do.");
					} else {
						LOG.debug("Replacing existing host: '"+swagger.get("host").asText()+"' in Swagger-File to '"+url.getHost()+port+"' based on configured backendBasepath: '"+this.backendBasepath+"'");
						backendBasepathAdjusted = true;
						((ObjectNode)swagger).put("host", url.getHost()+port);
					}
				}
				if(url.getPath()!=null && !url.getPath().equals("")) {
					if(swagger.get("basePath").asText().equals(url.getPath())) {
						LOG.debug("Swagger basePath: '"+swagger.get("basePath").asText()+"' already matches configured backendBasepath: '"+url.getPath()+"'. Nothing to do.");
					} else {
						LOG.debug("Replacing existing basePath: '"+swagger.get("basePath").asText()+"' in Swagger-File to '"+url.getPath()+"' based on configured backendBasepath: '"+this.backendBasepath+"'");
						backendBasepathAdjusted = true;
						((ObjectNode)swagger).put("basePath", url.getPath());
					}
				}
				 ArrayNode newSchemes = objectMapper.createArrayNode();
				 newSchemes.add(url.getProtocol());
				if(swagger.get("schemes")==null) {
					LOG.debug("Adding protocol: '"+url.getProtocol()+"' to Swagger-Definition");
					backendBasepathAdjusted = true;
					((ObjectNode)swagger).set("schemes", newSchemes);
				} else {
					if(swagger.get("schemes").size()!=1 || !swagger.get("schemes").get(0).asText().equals(url.getProtocol())) {
						LOG.debug("Replacing existing protocol(s): '"+swagger.get("schemes")+"' with '"+url.getProtocol()+"' according to configured backendBasePath: '"+this.backendBasepath+"'.");
						backendBasepathAdjusted = true;
						((ObjectNode)swagger).replace("schemes", newSchemes);
					}
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
			if(!(swagger.has("swagger") && swagger.get("swagger").asText().equals("2.0"))) {
				return false;
			}
			configureBasepath();
			return true;
		} catch (Exception e) {
			LOG.debug("Can't read apiSpecication. Doesn't have key \\\"swagger\\\" with value: \\\"2.0\"", e);
			return false;
		}
	}
}
