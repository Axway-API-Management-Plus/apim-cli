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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Objects;

public abstract class OASSpecification extends APISpecification {
    private static final Logger LOG = LoggerFactory.getLogger(OASSpecification.class);
    public static final String SERVERS = "servers";
    public static final String OPENAPI = "openapi";

    protected JsonNode openApiNode = null;

    public OASSpecification() {
        super();
    }

    @Override
    public void filterAPISpecification() {
        if (filterConfig == null) return;
        JsonNodeOpenAPI3SpecFilter.filter(openApiNode, filterConfig);
    }

    @Override
    public String getDescription() {
        if (this.openApiNode.get("info") != null && this.openApiNode.get("info").get("description") != null) {
            return this.openApiNode.get("info").get("description").asText();
        } else {
            return "";
        }
    }

    @Override
    public byte[] getApiSpecificationContent() {
        // Return the original given API-Spec if no filters are applied
        if (this.filterConfig == null) return this.apiSpecificationContent;
        try {
            return mapper.writeValueAsBytes(openApiNode);
        } catch (JsonProcessingException e) {
            throw new InternalException("Error parsing API-Specification", e);
        }
    }

    @Override
    public void updateBasePath(String basePath, String host) {
        try {
            String url = Utils.handleOpenAPIServerUrl(host, basePath);
            ObjectNode newServer = createObjectNode("url", url);
            ((ObjectNode) openApiNode).set(SERVERS, mapper.createArrayNode().add(newServer));
            this.apiSpecificationContent = this.mapper.writeValueAsBytes(openApiNode);
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
        if (backendBasePath == null && !openApiNode.has(SERVERS)) {
            throw new AppException("The open API specification doesn't contain a servers section and no backend basePath is given", ErrorCode.CANT_READ_API_DEFINITION_FILE);
        }
        try {
            if (openApiNode.has(SERVERS)) {
                ArrayNode servers = (ArrayNode) openApiNode.get(SERVERS);
                if (backendBasePath != null && !backendBasePath.contains("${env")) { // issue #332
                    JsonNode server = servers.get(0);
                    JsonNode urlJsonNode = server.get("url");
                    updateServer(urlJsonNode, backendBasePath);
                }
            } else {
                updateServer(backendBasePath);
            }
            this.apiSpecificationContent = this.mapper.writeValueAsBytes(openApiNode);
        } catch (Exception e) {
            LOG.error("Cannot replace host in provided Open API. Continue with given host.", e);
        }
    }

    private void updateServer(JsonNode urlJsonNode, String backendBasePath) throws MalformedURLException {
        if (urlJsonNode != null) {
            String serverUrl = urlJsonNode.asText();
            if (CoreParameters.getInstance().isOverrideSpecBasePath()) {
                overrideServerSection(backendBasePath); // override openapi url with backendBaseapath
            } else if (!serverUrl.startsWith("http")) { // If url does not have hostname, add hostname from backendBasepath
                LOG.info("servers.url does not contain host name hence updating host value from backendBasepath : {}", backendBasePath);
                updateServerSection(backendBasePath, serverUrl);
            }
        }
    }

    private void updateServer(String backendBasePath) throws MalformedURLException {
        LOG.info("Server element not found");
        if (CoreParameters.getInstance().isOverrideSpecBasePath()) {
            if (backendBasePath != null)
                overrideServerSection(backendBasePath); // override openapi url to fix issue #412
        } else {
            LOG.info("Setting up server element as /");
            updateServerSection(backendBasePath, "/");
        }
    }

    public void updateServerSection(String backendBasePath, String serverUrl) throws MalformedURLException {
        String ignoreBasePath = Utils.ignoreBasePath(backendBasePath);
        backendBasePath = Utils.handleOpenAPIServerUrl(serverUrl, ignoreBasePath);
        LOG.info("Updating openapi Servers url with value : {}", backendBasePath);
        ObjectNode newServer = createObjectNode("url", backendBasePath);
        ((ObjectNode) openApiNode).set(SERVERS, mapper.createArrayNode().add(newServer));
    }

    public void overrideServerSection(String backendBasePath) {
        if (backendBasePath.endsWith("/"))
            backendBasePath = backendBasePath.substring(0, backendBasePath.length() - 1);
        LOG.info("overriding openapi Servers url with value : {}", backendBasePath);
        ObjectNode newServer = createObjectNode("url", backendBasePath);
        ((ObjectNode) openApiNode).set(SERVERS, mapper.createArrayNode().add(newServer));
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), openApiNode);
    }
}
