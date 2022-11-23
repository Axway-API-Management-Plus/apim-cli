package com.axway.apim.api.apiSpecification;

import com.axway.apim.api.API;
import com.axway.apim.api.apiSpecification.filter.JsonNodeOpenAPI3SpecFilter;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAS3xSpecification extends APISpecification {
	private final Logger LOG = LoggerFactory.getLogger(OAS3xSpecification.class);

	private JsonNode openAPI = null;

	public OAS3xSpecification() {
		super();
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		if(this.mapper.getFactory() instanceof YAMLFactory) {
			return APISpecType.OPEN_API_30_YAML;
		}
		return APISpecType.OPEN_API_30;
	}

	@Override
	public void filterAPISpecification() {
		if(filterConfig == null) return;
		JsonNodeOpenAPI3SpecFilter.filter(openAPI, filterConfig);
	}

	@Override
	public String getDescription() {
		if(this.openAPI.get("info")!=null && this.openAPI.get("info").get("description")!=null) {
			return this.openAPI.get("info").get("description").asText();
		} else {
			return "";
		}
	}

	@Override
	public byte[] getApiSpecificationContent() {
		// Return the original given API-Spec if no filters are applied
		if(this.filterConfig == null) return this.apiSpecificationContent;
		try {
			return mapper.writeValueAsBytes(openAPI);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error parsing API-Specification", e);
		}
	}

	@Override
	public void configureBasePath(String backendBasePath, API api) throws AppException {
		if(!CoreParameters.getInstance().isReplaceHostInSwagger()) return;
		try {
			if(backendBasePath!=null) {
				if(openAPI.has("servers")) {
					JsonNode server =  openAPI.get("servers").get(0); // takes the first entity -- currently not handling multiple URLs
					JsonNode urlJsonNode = server.get("url");
					if(urlJsonNode != null){
						String serverUrl = urlJsonNode.asText();
						if(!serverUrl.startsWith("http")) {
							backendBasePath = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
						}
					}
					((ArrayNode) openAPI.get("servers")).removeAll();
				}
				LOG.info("Backend BasePath of API "+backendBasePath);
				ObjectNode newServer = this.mapper.createObjectNode();
				newServer.put("url", backendBasePath);
				((ObjectNode)openAPI).set("servers", mapper.createArrayNode().add(newServer));
				this.apiSpecificationContent = this.mapper.writeValueAsBytes(openAPI);
			}
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
			return openAPI.has("openapi") && openAPI.get("openapi").asText().startsWith("3.0.");
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
