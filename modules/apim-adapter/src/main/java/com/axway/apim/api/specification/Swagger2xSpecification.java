package com.axway.apim.api.specification;

import com.axway.apim.api.API;
import com.axway.apim.api.specification.filter.JsonNodeOpenAPI3SpecFilter;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.InternalException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class Swagger2xSpecification extends APISpecification {
    public static final String BASE_PATH = "basePath";
    public static final String SCHEMES = "schemes";
    private static final Logger LOG = LoggerFactory.getLogger(Swagger2xSpecification.class);
    private JsonNode swagger = null;

    public Swagger2xSpecification() {
        super();
    }

    @Override
    public APISpecType getAPIDefinitionType() throws AppException {
        if (mapper.getFactory() instanceof YAMLFactory) {
            return APISpecType.SWAGGER_API_20_YAML;
        }
        return APISpecType.SWAGGER_API_20;
    }

    @Override
    public byte[] getApiSpecificationContent() {
        // Return the original given API-Spec if no filters are applied
        if (this.filterConfig == null) return this.apiSpecificationContent;
        try {
            return mapper.writeValueAsBytes(swagger);
        } catch (JsonProcessingException e) {
            throw new InternalException("Error parsing API-Specification", e);
        }
    }

    @Override
    public void updateBasePath(String basePath, String host) {
        try {
            if (basePath != null) {
                URL url = new URL(host);
                String port = Utils.formatPort(url);
                ((ObjectNode) swagger).put(BASE_PATH, basePath);
                ((ObjectNode) swagger).put("host", url.getHost() + port);
                ArrayNode newSchemes = this.mapper.createArrayNode();
                newSchemes.add(url.getProtocol());
                ((ObjectNode) swagger).set(SCHEMES, newSchemes);
                this.apiSpecificationContent = this.mapper.writeValueAsBytes(swagger);
            }
        } catch (JsonProcessingException e) {
            LOG.error("Cannot replace basePath in swagger.", e);
        } catch (MalformedURLException e) {
            LOG.error("Unable to parse URL", e);
        }

    }

    @Override
    public void filterAPISpecification() {
        if (this.filterConfig == null) return;
        JsonNodeOpenAPI3SpecFilter.filter(swagger, filterConfig);
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
        if (backendBasePath == null && swagger.get("host") == null) {
            throw new AppException("The API specification doesn't contain a host and no backend basePath is given.", ErrorCode.CANT_READ_API_DEFINITION_FILE);
        }
        try {
            if (backendBasePath != null) {
                if (backendBasePath.contains("${env")) { // issue #332
                    return;
                }
                URL url = new URL(backendBasePath);
                String port = Utils.formatPort(url);
                if (swagger.get("host") == null) {
                    LOG.debug("Adding new host {}{} to Swagger-File based on backendBasePath: {}", url.getHost(), port, backendBasePath);
                    ((ObjectNode) swagger).put("host", url.getHost() + port);
                    LOG.info("Used the backendBasePath: {} to adjust host the API-Specification.", backendBasePath);
                }
                if (swagger.get(SCHEMES) == null) {
                    ArrayNode newSchemes = this.mapper.createArrayNode();
                    newSchemes.add(url.getProtocol());
                    LOG.debug("Adding protocol: {} to Swagger-Definition", url.getProtocol());
                    ((ObjectNode) swagger).set(SCHEMES, newSchemes);
                }
                if (swagger.get(BASE_PATH) == null) {
                    LOG.info("Adding default basePath / to swagger");
                    ((ObjectNode) swagger).put(BASE_PATH, "/"); // to adhere the spec - if basePath is empty, serve the traffic on / - Ref -> https://swagger.io/specification/v2/
                }
                overridePath(url, port, backendBasePath);
                this.apiSpecificationContent = this.mapper.writeValueAsBytes(swagger);
            }
        } catch (MalformedURLException e) {
            throw new AppException("The configured backendBasePath: '" + backendBasePath + "' is invalid.", ErrorCode.BACKEND_BASEPATH_IS_INVALID, e);
        } catch (Exception e) {
            LOG.error("Cannot replace host in provided Swagger-File. Continue with given host.", e);
        }
    }

    private void overridePath( URL url, String port, String backendBasePath){
        if (CoreParameters.getInstance().isOverrideSpecBasePath()) {
            LOG.info("Overriding host scheme and basePath with value : {}", backendBasePath);
            String basePath = url.getPath();
            if (StringUtils.isNotEmpty(basePath)) {
                LOG.debug("Overriding Swagger basePath with value : {}", basePath);
                ((ObjectNode) swagger).put(BASE_PATH, basePath);
            } else {
                LOG.debug("Not updating basePath value in swagger 2 as BackendBasePath : {}  has empty basePath", backendBasePath);
            }
            ((ObjectNode) swagger).put("host", url.getHost() + port);
            ArrayNode newSchemes = this.mapper.createArrayNode();
            newSchemes.add(url.getProtocol());
            ((ObjectNode) swagger).set(SCHEMES, newSchemes);
        }
    }

    @Override
    public boolean parse(byte[] apiSpecificationContent) {
        try {
            this.apiSpecificationContent = apiSpecificationContent;
            setMapperForDataFormat();
            if (this.mapper == null) return false;
            swagger = this.mapper.readTree(apiSpecificationContent);
            return swagger.has("swagger") && swagger.get("swagger").asText().startsWith("2.");
        } catch (Exception e) {
            LOG.trace("Could load specification as Swagger 2.0", e);
            return false;
        }
    }
}
