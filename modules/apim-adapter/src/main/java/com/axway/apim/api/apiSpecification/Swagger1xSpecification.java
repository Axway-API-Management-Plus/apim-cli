package com.axway.apim.api.apiSpecification;

import java.net.MalformedURLException;
import java.net.URL;

import com.axway.apim.api.API;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Swagger1xSpecification extends APISpecification {
	
	JsonNode swagger = null;

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		if(this.mapper.getFactory() instanceof YAMLFactory) {
			return APISpecType.SWAGGGER_API_1x_YAML;
		}
		return APISpecType.SWAGGGER_API_1x;
	}

	@Override
	public byte[] getApiSpecificationContent() {
		return this.apiSpecificationContent;
	}

	@Override
	public void configureBasepath(String backendBasepath, API api) throws AppException {
		if(!CoreParameters.getInstance().isReplaceHostInSwagger()) return;
		try {
			if(backendBasepath!=null) {
				boolean backendBasepathAdjusted = false;
				URL url = new URL(backendBasepath);
				if(url.getPath()!=null && !url.getPath().equals("") && !backendBasepath.endsWith("/")) { // See issue #178
					backendBasepath += "/";
					url = new URL(backendBasepath);
				}
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
			throw new AppException("The configured backendBasepath: '"+backendBasepath+"' is invalid.", ErrorCode.CANT_READ_CONFIG_FILE, e);
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
			swagger = this.mapper.readTree(apiSpecificationContent);
			if(!(swagger.has("swaggerVersion") && swagger.get("swaggerVersion").asText().startsWith("1."))) {
				return false;
			}
			return true;
		} catch (AppException e) {
			if(e.getError()==ErrorCode.UNSUPPORTED_FEATURE) {
				throw e;
			}
			return false;
		} catch (Exception e) {
			LOG.trace("No Swager 1.x specification.", e);
			return false;
		}
	}
}
