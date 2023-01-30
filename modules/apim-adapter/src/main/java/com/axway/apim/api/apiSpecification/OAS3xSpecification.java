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

import java.net.MalformedURLException;
import java.util.Objects;

public class OAS3xSpecification extends APISpecification {
    private final Logger LOG = LoggerFactory.getLogger(OAS3xSpecification.class);

    private JsonNode openAPI = null;

    public OAS3xSpecification() {
        super();
    }

    @Override
    public APISpecType getAPIDefinitionType() throws AppException {
        if (this.mapper.getFactory() instanceof YAMLFactory) {
            return APISpecType.OPEN_API_30_YAML;
        }
        return APISpecType.OPEN_API_30;
    }

    @Override
    public void filterAPISpecification() {
        if (filterConfig == null) return;
        JsonNodeOpenAPI3SpecFilter.filter(openAPI, filterConfig);
    }

    @Override
    public String getDescription() {
        if (this.openAPI.get("info") != null && this.openAPI.get("info").get("description") != null) {
            return this.openAPI.get("info").get("description").asText();
        } else {
            return "";
        }
    }

    @Override
    public byte[] getApiSpecificationContent() {
        // Return the original given API-Spec if no filters are applied
        if (this.filterConfig == null) return this.apiSpecificationContent;
        try {
            return mapper.writeValueAsBytes(openAPI);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing API-Specification", e);
        }
    }

    @Override
    public void updateBasePath(String basePath, String host) {
        try {
            String url = Utils.handleOpenAPIServerUrl(host, basePath);
            ObjectNode newServer = createObjectNode("url", url);
            ((ObjectNode) openAPI).set("servers", mapper.createArrayNode().add(newServer));
            configureBasePath(basePath, null);
            this.apiSpecificationContent = this.mapper.writeValueAsBytes(openAPI);
        } catch (AppException e) {
            LOG.error("Cannot replace servers in openapi.", e);
        } catch (MalformedURLException e) {
            LOG.error("Unable to parse URL", e);
        } catch (JsonProcessingException e) {
            LOG.error("Cannot replace host in provided Open API. Continue with given host.", e);
        }
    }

    public ObjectNode createObjectNode(String key, String value) {
        ObjectNode newServer = this.mapper.createObjectNode();
        newServer.put(key, value);
        return newServer;
    }

    @Override
    public void configureBasePath(String backendBasePath, API api) throws AppException {
        if (!CoreParameters.getInstance().isReplaceHostInSwagger()) return;
        try {
            if (backendBasePath != null) {
                if (openAPI.has("servers")) {
                    ArrayNode servers = (ArrayNode) openAPI.get("servers");
                    if (!servers.isEmpty()) {
                        // Remove remaining server nodes
                        for (int i = 1; i < servers.size(); i++) {
                            servers.remove(i);
                        }
                        JsonNode server = servers.get(0); // takes the first entity -- currently not handling multiple URLs
                        JsonNode urlJsonNode = server.get("url");
                        if (urlJsonNode != null) {
                            String serverUrl = urlJsonNode.asText();
                            if (!serverUrl.startsWith("http")) {
                                backendBasePath = Utils.handleOpenAPIServerUrl(serverUrl, backendBasePath);
                                LOG.info("Updating openapi Servers url with value : {}", backendBasePath);
                                ObjectNode newServer = createObjectNode("url", backendBasePath);
                                ((ObjectNode) openAPI).set("servers", mapper.createArrayNode().add(newServer));
                            }
                        }
                    }
                }else {
                    ObjectNode newServer = createObjectNode("url", backendBasePath);
                    ((ObjectNode) openAPI).set("servers", mapper.createArrayNode().add(newServer));
                    LOG.warn("Adding openapi Servers url with value : {}", backendBasePath);
                }
                this.apiSpecificationContent = this.mapper.writeValueAsBytes(openAPI);
            }
        } catch (Exception e) {
            LOG.error("Cannot replace host in provided Open API. Continue with given host.", e);
        }
    }

    @Override
    public boolean parse(byte[] apiSpecificationContent) throws AppException {
        try {
            super.parse(apiSpecificationContent);
            setMapperForDataFormat();
            if (this.mapper == null) return false;
            openAPI = this.mapper.readTree(apiSpecificationContent);
            return openAPI.has("openapi") && openAPI.get("openapi").asText().startsWith("3.0.");
        } catch (AppException e) {
            if (e.getError() == ErrorCode.UNSUPPORTED_FEATURE) {
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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), openAPI);
    }
}
