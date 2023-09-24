package com.axway.apim.api.specification;

import com.axway.apim.api.API;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Swagger1xSpecification extends APISpecification {

    private static final Logger LOG = LoggerFactory.getLogger(Swagger1xSpecification.class);

    private JsonNode swagger = null;

    @Override
    public APISpecType getAPIDefinitionType() throws AppException {
        if (this.mapper.getFactory() instanceof YAMLFactory) {
            return APISpecType.SWAGGER_API_1x_YAML;
        }
        return APISpecType.SWAGGER_API_1x;
    }

    @Override
    public byte[] getApiSpecificationContent() {
        return this.apiSpecificationContent;
    }

    @Override
    public void updateBasePath(String basePath, String host) {
        try {
            String url = Utils.handleOpenAPIServerUrl(host, basePath);
            ((ObjectNode)swagger).put("basePath", url);
            this.apiSpecificationContent = this.mapper.writeValueAsBytes(swagger);
        } catch (Exception e) {
            LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
        }
    }

    @Override
    public String getDescription() {
        if (this.swagger.get("info") != null && this.swagger.get("info").get("description") != null) {
            return this.swagger.get("info").get("description").asText();
        } else {
            return "";
        }
    }

    @Override
    public void configureBasePath(String backendBasePath, API api) throws AppException {
        // Not required
    }

    @Override
    public boolean parse(byte[] apiSpecificationContent) throws AppException {
        try {
            super.parse(apiSpecificationContent);
            setMapperForDataFormat();
            if (this.mapper == null) return false;
            swagger = this.mapper.readTree(apiSpecificationContent);
            return swagger.has("swaggerVersion") && swagger.get("swaggerVersion").asText().startsWith("1.");
        } catch (AppException e) {
            if (e.getError() == ErrorCode.UNSUPPORTED_FEATURE) {
                throw e;
            }
            return false;
        } catch (Exception e) {
            LOG.trace("No Swagger 1.x specification.", e);
            return false;
        }
    }
}
