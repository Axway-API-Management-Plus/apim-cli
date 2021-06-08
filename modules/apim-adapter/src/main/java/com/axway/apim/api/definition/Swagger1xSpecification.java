package com.axway.apim.api.definition;

import java.net.MalformedURLException;
import java.net.URL;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Swagger1xSpecification extends APISpecification {
	
	JsonNode swagger = null;
	
	public Swagger1xSpecification(byte[] apiSpecificationContent) throws AppException {
		super(apiSpecificationContent);
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		if(this.mapper.getFactory() instanceof YAMLFactory) {
			return APISpecType.SWAGGGER_API_1x_YAML;
		}
		return APISpecType.SWAGGGER_API_1x;
	}

	@Override
	public void configureBasepath(String backendBasepath) throws AppException {
		if(!CoreParameters.getInstance().isReplaceHostInSwagger()) return;
		try {
			if(backendBasepath!=null) {
				boolean backendBasepathAdjusted = false;
				URL url = new URL(backendBasepath);
				
				if(swagger.get("basePath").asText().equals(url.toString())) {
					LOG.debug("Swagger resourcePath: '"+swagger.get("basePath").asText()+"' already matches configured backendBasepath: '"+url.getPath()+"'. Nothing to do.");
				} else {
					LOG.debug("Replacing existing basePath: '"+swagger.get("basePath").asText()+"' in Swagger-File to '"+url.toString()+"' based on configured backendBasepath: '"+backendBasepath+"'");
					backendBasepathAdjusted = true;
					((ObjectNode)swagger).put("basePath", url.toString());
				}
				if(backendBasepathAdjusted) {
					LOG.info("Used the configured backendBasepath: '"+backendBasepath+"' to adjust the Swagger definition.");
				}
				this.apiSpecificationContent = this.mapper.writeValueAsBytes(swagger);
			}
		} catch (MalformedURLException e) {
			throw new AppException("The backendBasepath: '"+backendBasepath+"' is invalid.", ErrorCode.CANT_READ_CONFIG_FILE, e);
		} catch (Exception e) {
			LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
		}
	}
	
	@Override
	public boolean configure() throws AppException {
		try {
			setMapperForDataFormat();
			if(this.mapper==null) return false;
			swagger = this.mapper.readTree(apiSpecificationContent);
			if(!(swagger.has("swaggerVersion") && swagger.get("swaggerVersion").asText().startsWith("1."))) {
				return false;
			}
			return true;
		} catch (Exception e) {
			LOG.trace("No Swager 1.x specification.", e);
			return false;
		}
	}
}
